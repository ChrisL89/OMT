package com.offer.controller;

import com.offer.events.OfferEvents;
import com.offer.httpClient.httpClientHelper;
import com.offer.model.*;
import com.offer.model.bonusRequest.BonusAwardStatus;
import com.offer.model.RequestBody.ViewOffer;
import com.offer.model.bonusRequest.BonusPlayer;
import com.offer.repository.ActivationHistoryRepository;
import com.offer.repository.offerRepository.OfferRepository;
import com.offer.repository.offerRepository.OfferRepositoryImpl;
import com.offer.validator.OfferValidator;
import com.offer.helper.Logger;
import com.newrelic.api.agent.Trace;
import org.hibernate.Hibernate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/")
public class OfferController {

    private OfferRepository offerRepository;
    private OfferValidator offerValidator;
    private ActivationHistoryRepository activationHistoryRepository;
    private OfferRepositoryImpl offerRepositoryImpl;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    @Autowired
    public OfferController(OfferValidator offerValidator, OfferRepository offerRepository, ActivationHistoryRepository activationHistoryRepository, OfferRepositoryImpl offerRepositoryImpl) {
        this.offerValidator = offerValidator;
        this.offerRepository = offerRepository;
        this.activationHistoryRepository = activationHistoryRepository;
        this.offerRepositoryImpl = offerRepositoryImpl;

    }

    @InitBinder("offer")
    public void setupBinder(WebDataBinder binder) {
        binder.addValidators(offerValidator);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "offers", method = RequestMethod.GET)
    public List<Offer> list() {
        return offerRepository.findAll();
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "offers", method = RequestMethod.POST)
    public Offer create(@Valid @RequestBody Offer offer) {
        //As this is create, make sure IDs are all removed from the table.
        offer.setOffer_id(null);
        offer.getReward().setId(null);
        //Set create date if it is creating
        offer.setCreateDate(new Date());
        //Set offer status to created if it is creating
        offer.setStatus(Offer.OfferStatus.CREATED.toString());
        //set default allow all games to false, it is only used by notification
        offer.getReward().setAllowAllGames(false);
        //If offer is open to all players, we should not use player list value.
        //This is to fix an UI bug where it can send player list when open to all player is set to true after clone
        if (offer.getAllPlayers()) {
            offer.clearPlayers();
        }
        List<Player> players = offer.getPlayers();
        players.forEach(player -> {
            player.setOffer(offer);
            //Make sure player record is set to null as well, since it is create, we don't want to update any existing player records.
            player.setId(null);
            player.setViewed(false);
            player.setStatus(Player.PlayerStatus.PENDING.toString());
        });
        List<Game> games = offer.getReward().getGames();
        games.forEach(game -> {
            game.setReward(offer.getReward());
        });

        offer.getReward().setOffer(offer);
        return offerRepository.saveAndFlush(offer);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "offers/{id}", method = RequestMethod.GET)
    public Offer get(@PathVariable Long id) {
        return offerRepository.findOne(id);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "offers/{id}", method = RequestMethod.PUT)
    public Offer update(@PathVariable Long id, @RequestBody Offer offer) {
        Offer existingOffer = offerRepository.findOne(id);
        //Update Offer object
        offer.getPlayers().forEach(player -> player.setOffer(offer));
        List<Game> games = offer.getReward().getGames();
        games.forEach(game -> game.setReward(offer.getReward()));
        offer.getReward().setOffer(offer);
        BeanUtils.copyProperties(offer, existingOffer);
        //Set default status for Player records`
        List<Player> players = existingOffer.getPlayers();
        players.forEach(player -> {
            player.setViewed(false);
            player.setStatus(Player.PlayerStatus.PENDING.toString());
        });
        return offerRepository.saveAndFlush(existingOffer);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "offers/{id}", method = RequestMethod.DELETE)
    public Offer delete(@PathVariable Long id) {
        Offer existingOffer = offerRepository.findOne(id);
        offerRepository.delete(existingOffer);

        return existingOffer;
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "getOffersForCustomer",
            params = {"id", "status", "fromNumber", "toNumber"},
            method = RequestMethod.GET)
    public List<BonusDto> getOffersForCustomer(@RequestParam Long id, @RequestParam String status, @RequestParam int fromNumber, @RequestParam int toNumber) {

        return offerRepositoryImpl.findOffersByCustomerId(id, status, fromNumber, toNumber, offerRepository);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "getOffersForCustomer",
            params = {"id"},
            method = RequestMethod.GET)
    public List<BonusDto> getOffersForCustomer(@RequestParam Long id) {
        return offerRepositoryImpl.findOffersByCustomerId(id, offerRepository);
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "activateOffer/{id}", method = RequestMethod.POST)
    public Offer activateOffer(@PathVariable Long id) throws Exception {
        Offer existingOffer = offerRepository.findOne(id);
        //Todo check if the offer exist and ready to be activated first


        //add bonus to the provider system
        if (!httpClientHelper.saveBonus(existingOffer.getRewardType(), existingOffer.getOfferCode(), existingOffer.getReward())) {
            throw new Exception("Adding bonus to Provider failed");
        }

        //If it is a manual offer, we want to trigger it immediately upon activation
        if (Objects.equals(existingOffer.getTriggerType(), Offer.TriggerType.MANUAL.toString())) {

            //award free spins to customer via GSI
            //If add free spins request failed, return error back to UI not able to activate manual offer
            String rgsCode = existingOffer.getReward().getProviderName();
            String offerCode = existingOffer.getOfferCode();
            Long offerId = existingOffer.getOffer_id();
            String bonusAwardId = offerId.toString() + System.getProperty("gsi.api.bonus.bonus-manual-award-suffix");
            ArrayList<BonusPlayer> players = new ArrayList<>();
            existingOffer.getPlayers().forEach(player -> players.add(new BonusPlayer(player.getCustomerId(), player.getCustomerName())));
            if (!httpClientHelper.awardBonus(rgsCode, offerCode, bonusAwardId, players)) {
                throw new Exception("Awarding manual bonus failed");
            }

            //Once above is successful, Update bonus status And insert activation history message
            String dateTimeStr = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date());

            existingOffer.getPlayers().forEach(player -> {

                player.setViewed(false);
                player.setStatus(Player.PlayerStatus.TRIGGERED.toString());

                String message = "Customer ID: " + player.getCustomerId() + " has triggered a manual offer with Offer code: " +
                        offerCode + " on " + dateTimeStr;
                ActivationHistory activationHistory = new ActivationHistory();
                activationHistory.setCustomerId(player.getCustomerId());
                activationHistory.setOfferId(offerId);
                activationHistory.setActivationMessage(message);
                activationHistoryRepository.save(activationHistory);

            });

            activationHistoryRepository.flush();

            //TODO Maybe we need to notify Mailman that awarding bonus has been triggered and customer will receive it soon

            //Update offer status to activated
            existingOffer.setStatus(Offer.OfferStatus.ACTIVATED.toString());

            return offerRepository.saveAndFlush(existingOffer);

        } else {
            //Update all related player-offer status to Available, Only deposit and login offer will have player records at this stage
            existingOffer.getPlayers().forEach(player -> {
                player.setViewed(false);
                player.setStatus(Player.PlayerStatus.AVAILABLE.toString());
            });
            //Update offer status to activated
            existingOffer.setStatus(Offer.OfferStatus.ACTIVATED.toString());
            //Update bonuses status details
            Offer resultOffer = offerRepository.saveAndFlush(existingOffer);

            //Send out notification for deposit offer bonuses
            if (Objects.equals(existingOffer.getTriggerType(), Offer.TriggerType.DEPOSIT.toString())) {
                existingOffer.getPlayers().forEach(player -> {
                    //Notify mailman when activating deposit offer, notification is a background task
                    OfferEvents offerEvents = new OfferEvents(this, existingOffer, player, OfferEvents.EventMessageType.AVAILABLE);
                    applicationEventPublisher.publishEvent(offerEvents);
                });
            }
            return resultOffer;
        }
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "deactivateOffer/{id}", method = RequestMethod.POST)
    public Offer deactivateOffer(@PathVariable Long id) {
        Offer existingOffer = offerRepository.findOne(id);
        //Update all related player-offer status to deactivated
        existingOffer.getPlayers().forEach(player -> {
            player.setViewed(false);
            //If bonus status is Available, then we can deactivate it
            if (Objects.equals(player.getStatus(), Player.PlayerStatus.AVAILABLE.toString())) {
                player.setStatus(Player.PlayerStatus.DEACTIVATED.toString());
            }
        });
        //Update offer status to deactivated
        existingOffer.setStatus(Offer.OfferStatus.DEACTIVATED.toString());
        //Set deactivation date
        existingOffer.setDeactivatedDate(new Date());

        return offerRepository.saveAndFlush(existingOffer);
    }


    @Trace
    @CrossOrigin
    @RequestMapping(value = "getActivationHistory/{offerId}", method = RequestMethod.GET)
    public ArrayList<ActivationHistory> getActivationHistory(@PathVariable Long offerId) {
        return activationHistoryRepository.findByOfferId(offerId);
    }

    /**
     * Set the bonus to be viewed by customer
     *
     * @param viewOffer viewOffer request body
     * @return true or false
     */
    @Trace
    @CrossOrigin
    @RequestMapping(value = "viewOffer", method = RequestMethod.POST)
    public boolean viewOffer(@Valid @RequestBody ViewOffer viewOffer) {
        //Update bonus(Player record) view status to true
        return offerRepositoryImpl.updateBonusViewedStatusById(viewOffer.getCustomerId(), viewOffer.getOfferIds(), true) == viewOffer.getOfferIds().size();
    }

    @Trace
    @CrossOrigin
    @RequestMapping(value = "bonusAwardStatus", method = RequestMethod.POST)
    public boolean updateBonusAwardStatus(@RequestBody BonusAwardStatus bonusAwardStatus) {

        String awardId = bonusAwardStatus.getAwardId();
        Long offerId = Long.parseLong(awardId.split("_")[0]);
        Offer offer = offerRepository.findOne(offerId);
        Logger.log("Received bonus award status callback with bonus award id: {" + awardId + "} and status: {" + bonusAwardStatus.getStatus() + "}", Logger.LogType.INFO);
        //SUN-7917: Need to initialize games since it is Flazy loaded, and later when we want to access it the connection might have been closed.
        if (!Hibernate.isInitialized(offer.getReward().getGames())) {
            Hibernate.initialize(offer.getReward().getGames());
        }
        //Loop through all players and update bonus status accordingly
        //When we receive this call, status should always be complete
        for (BonusPlayer bonusPlayer : bonusAwardStatus.getPlayersAwarded()) {
            //Update Bonus(Player record) status
            offerRepositoryImpl.updateBonusStatusById(bonusPlayer.getPlayerId(), offerId, Player.PlayerStatus.ACTIVATED.toString());
            //Insert activation history record
            String message = "Customer ID: " + bonusPlayer.getPlayerId() + " has been awarded offer with bonus award id: " + bonusAwardStatus.getAwardId();
            updateActivationHistory(bonusPlayer.getPlayerId(), offerId, message);

            //Notify mailman when activating non-manual offer
            OfferEvents offerEvents = new OfferEvents(this, bonusPlayer.getPlayerId(), offer, OfferEvents.EventMessageType.ACTIVATED);
            applicationEventPublisher.publishEvent(offerEvents);
        }

        //Pending and failed are both considered to be unsuccessful, thus treated the same way
        ArrayList<BonusPlayer> unsuccessfulPlayers = new ArrayList<>();
        unsuccessfulPlayers.addAll(bonusAwardStatus.getPlayersPending());
        unsuccessfulPlayers.addAll(bonusAwardStatus.getPlayersFailed());


        for (BonusPlayer bonusPlayer : unsuccessfulPlayers) {
            //Update Bonus(Player record) status
            offerRepositoryImpl.updateBonusStatusById(bonusPlayer.getPlayerId(), offerId, Player.PlayerStatus.FAILED.toString());
            //Insert activation failed history record
            String message = "Customer ID: " + bonusPlayer.getPlayerId() + "failed to be awarded with offer with bonus award id: " + bonusAwardStatus.getAwardId();
            updateActivationHistory(bonusPlayer.getPlayerId(), offerId, message);

            //Notify mailman when activating non-manual offer
            OfferEvents offerEvents = new OfferEvents(this, bonusPlayer.getPlayerId(), offer, OfferEvents.EventMessageType.FAILED);
            applicationEventPublisher.publishEvent(offerEvents);
        }

        //TODO At the moment as long as the event has been pushed to publisher, this will considered to be successful, if failed to push notification, error will be logged.
        return true;
    }

    private void updateActivationHistory(Long customer_id, Long offer_id, String message) {

        //Insert activation history record
        ActivationHistory activationHistory = new ActivationHistory();
        activationHistory.setCustomerId(customer_id);
        activationHistory.setOfferId(offer_id);
        activationHistory.setActivationMessage(message);
        activationHistoryRepository.saveAndFlush(activationHistory);

    }


    //TODO FUNCTION Callback to update bonus status to be Used

}
