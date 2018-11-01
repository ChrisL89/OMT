package com.offer.repository.offerRepository;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.offer.model.BonusDto;
import com.offer.model.Offer;
import com.offer.model.Player;
import com.offer.httpClient.httpClientHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class OfferRepositoryImpl implements OfferRepositoryCustom{

    @PersistenceContext
    EntityManager entityManager;



    /**
     * Get the list of offers and Convert it to simplified object for the response
     * @param customerId Wagerplayer customer id
     * @param offerRepository repository
     * @return list of customer bonuses
     */
    @Override
    public List<BonusDto> findOffersByCustomerId(Long customerId, OfferRepository offerRepository) {
        TypedQuery<Offer> query = entityManager.createQuery("SELECT o FROM Offer o JOIN FETCH o.players p JOIN FETCH o.reward r WHERE p.customerId = " +
                customerId , Offer.class);

        List<Offer> offers = query.getResultList();

        try {
            //Grab all offers for the customer from GSI
            JsonArray jsonArray = httpClientHelper.awardStatus(customerId);
            List<JsonObject> jsonObjectList = new ArrayList<>();
            for (JsonElement jsonElement : jsonArray) {
                jsonObjectList.add(jsonElement.getAsJsonObject());
            }
            offers.forEach(offer -> {
                //If bonus is available and offer is expired, update bonus status
                if (Objects.equals(offer.getFirstPlayer().getStatus(), Player.PlayerStatus.AVAILABLE.toString())  && Objects.equals(offer.getStatus(), Offer.OfferStatus.EXPIRED.toString())) {
                    offer.getFirstPlayer().setStatus(Player.PlayerStatus.EXPIRED.toString());
                    offer.getFirstPlayer().setViewed(false);
                    offerRepository.save(offer);
                } else if(Objects.equals(offer.getFirstPlayer().getStatus(), Player.PlayerStatus.ACTIVATED.toString())) {
                    Optional<JsonObject> result = jsonObjectList.stream().filter(jsonObject -> Objects.equals(jsonObject.get("bonusId").getAsString(), offer.getOfferCode() + System.getProperty("gsi.api.bonus.bonus-suffix"))).findAny();
                    if(!result.isPresent()) {
                        return;
                    }
                    String targetStatus = result.get().get("status").getAsString().toUpperCase();
                    //Verify if the status is changed, is yes, then update accordingly
                    if (Objects.equals(targetStatus, "EXPIRED")) {
                        Player player = offer.getFirstPlayer();
                        player.setStatus(Player.PlayerStatus.FREESPINEXPIRED.toString());
                        player.setViewed(false);
                        offerRepository.save(offer);
                    } else if (Objects.equals(targetStatus, "COMPLETE")) {
                        Player player = offer.getFirstPlayer();
                        player.setStatus(Player.PlayerStatus.USED.toString());
                        player.setViewed(false);
                        offerRepository.save(offer);
                    }
                }
            });
            //Persist all offer updates to DB
            offerRepository.flush();
        } catch (Exception e) {
            com.offer.helper.Logger.log("Problem updating Award(Player) Status. Reason: " + e.getMessage(), com.offer.helper.Logger.LogType.WARN);
        }
        return BonusDto.convertToDtoList(offers);
    }

    /**
     * Get the list of offers and Convert it to simplified object for the response
     * @param customerId Wagerplayer customer id
     * @param status Customer bonus status
     * @param fromNumber from index
     * @param toNumber to index
     * @return list of customer bonuses
     */
    @Override
    public List<BonusDto> findOffersByCustomerId(Long customerId, String status, int fromNumber, int toNumber, OfferRepository offerRepository) {

        TypedQuery<Offer> query = entityManager.createQuery("SELECT o FROM Offer o JOIN FETCH o.players p JOIN FETCH o.reward r WHERE p.customerId = " +
                customerId + " AND p.status = '" + status + "'", Offer.class);
        query.setFirstResult(fromNumber-1);
        query.setMaxResults(toNumber-fromNumber+1);

        List<Offer> offers = query.getResultList();
        List<Offer> filteredOffers = new ArrayList<>();

        //If it is checking available bonus, we want to make sure Offer is not expired, otherwise update bonus status
        if(Objects.equals(status, Player.PlayerStatus.AVAILABLE.toString())) {
            filteredOffers = verifyAvailableOffers(offers, offerRepository);
            return BonusDto.convertToDtoList(filteredOffers);
        } else if(Objects.equals(status, Player.PlayerStatus.ACTIVATED.toString())) {
            filteredOffers = verifyActiveOffers(offers, customerId, offerRepository);
            return BonusDto.convertToDtoList(filteredOffers);
        }
        //TODO When querying Complete tab(EXPIRED, USED, FREESPINEXPIRED we might need to run above logic to extract additonal ones from available/active column, and return offer list)
        return BonusDto.convertToDtoList(offers);
    }

    /**
     *Get the list of available offers in OMT, if the offer status is changed to expired, update bonus status,
     * if not then add to the list we are going to return
     *
     * @param offers offers show available for the customer in OMT
     * @param offerRepository repository for updating offer details
     * @return list of available offers up to date
     */
    public List<Offer> verifyAvailableOffers(List<Offer> offers, OfferRepository offerRepository) {
        List<Offer> filteredOffers = new ArrayList<>();
        offers.forEach(offer -> {
            if(Objects.equals(offer.getStatus(), Offer.OfferStatus.EXPIRED.toString())) {
                offer.getPlayers().forEach(player -> {
                    player.setStatus(Player.PlayerStatus.EXPIRED.toString());
                    player.setViewed(false);
                });
                offerRepository.save(offer);
            } else {
                //Offer to return as they are not updated
                filteredOffers.add(offer);
            }
        });
        offerRepository.flush();
        return filteredOffers;
    }


    /**
     *Loop through all active offer in OMT, find the same offer in GSI List,
     * if the offer status has changed, update offer status in OMT, if not then add it to the list we are going to return
     *
     * @param offers list of offers we want to verify before return
     * @param customerId WP customer id
     * @param offerRepository repository to update offers
     * @return List of active offers after compare with response offer list
     */
    public List<Offer> verifyActiveOffers(List<Offer> offers, long customerId, OfferRepository offerRepository) {
        List<Offer> filteredOffers = new ArrayList<>();
        try {
            //Check if the player status is active, offer can be all used or expired
            // check with GSI if what is the current offer status
            JsonArray jsonArray = httpClientHelper.awardStatus(customerId);

            //Looping through each Active offer in OMT, if the offer status is updated in response, then update in OMT, else add it to filterList
            offers.forEach(offer -> {
                //Find the offer in the response list
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    if (Objects.equals(jsonObject.get("bonusId").getAsString(), offer.getOfferCode() + System.getProperty("gsi.api.bonus.bonus-suffix"))) {
                        //Verify if the status is changed
                        String targetStatus = jsonObject.get("status").getAsString().toUpperCase();
                        if (Objects.equals(targetStatus, "EXPIRED")) {
                            Player player = offer.findPlayerById(customerId);
                            player.setStatus(Player.PlayerStatus.FREESPINEXPIRED.toString());
                            player.setViewed(false);
                            offerRepository.save(offer);
                        } else if (Objects.equals(targetStatus, "COMPLETE")) {
                            Player player = offer.findPlayerById(customerId);
                            player.setStatus(Player.PlayerStatus.USED.toString());
                            player.setViewed(false);
                            offerRepository.save(offer);
                        } else {
                            filteredOffers.add(offer);
                        }
                        break;
                    }
                }
            });
            offerRepository.flush();
        } catch (Exception e) {
            com.offer.helper.Logger.log("Problem updating Award Status. Reason: " + e.getMessage(), com.offer.helper.Logger.LogType.WARN);
        }
        return filteredOffers;
    }

    /**
     * This is to find eligible deposit or register deposit offer for a customer(as customer did not enter promo code)
     *
     * @param customerId Wagerplayer customer id
     * @param depositAmount Deposit amount
     * @param currency Deposit currency
     * @return Offer result | null
     */
    @Override
    public Offer findDepositOfferByCustomerId(Long customerId, Float depositAmount, String currency) {

        //1.If it is register-deposit offer, promo code is always required
        //2.deposit is always required
        TypedQuery<Offer> queryRegisterOffers = entityManager.createQuery("SELECT o FROM Offer o JOIN FETCH o.players p JOIN FETCH o.reward r WHERE p.customerId = " +
                customerId + " AND o.promoCodeRequired = true AND o.depositRequired = true AND p.status = '" + Player.PlayerStatus.REGISTERED.toString() + "' AND o.triggerType = '" + Offer.TriggerType.REGISTER.toString() + "' And o.minDeposit <= " + depositAmount +
                " AND o.maxDeposit >= " + depositAmount + " AND currency = '" + currency + "' AND o.status = '" + Offer.OfferStatus.ACTIVATED.toString() + "' AND o.startDate < NOW() AND o.endDate > NOW()", Offer.class);

        TypedQuery<Offer> queryDepositOffers = entityManager.createQuery("SELECT o FROM Offer o JOIN FETCH o.players p JOIN FETCH o.reward r WHERE p.customerId = " +
                customerId + " AND o.promoCodeRequired = false AND p.status = '" + Player.PlayerStatus.AVAILABLE.toString() + "' AND o.triggerType = '" + Offer.TriggerType.DEPOSIT.toString() + "' AND o.minDeposit <= " + depositAmount +
                        " AND o.maxDeposit >= " + depositAmount + " AND currency = '" + currency + "' AND o.status = '" + Offer.OfferStatus.ACTIVATED.toString() + "' AND o.startDate < NOW() AND o.endDate > NOW()", Offer.class);

        //If the offer result has more than one offer, just pick the first one in the list
        List<Offer> offers = queryRegisterOffers.getResultList();
        if(!offers.isEmpty()) {
            return offers.get(0);
        } else {
            offers = queryDepositOffers.getResultList();
            if(!offers.isEmpty()) {
                return offers.get(0);
            }
        }

        return null;
    }

    /**
     * This is to find eligible deposit offer for a customer with offer code
     *
     * @param customerId Wagerplayer customer id
     * @param bonusStatus Customer bonus status
     * @param offerTriggerType Offer trigger type (Deposit, Login, Register)
     * @param depositAmount Deposit amount
     * @param currency Deposit currency
     * @return Offer result | null
     */
    @Override
    public Offer findDepositOfferByCustomerId(Long customerId, String bonusStatus, String offerTriggerType, Float depositAmount, String currency, String promotionCode) {

        Offer resultOffer = null;
        //TODO Maybe we need to workout to do eager load dynamically, so we don't have to fetch players as well.
        TypedQuery<Offer> query = entityManager.createQuery("SELECT o FROM Offer o LEFT JOIN FETCH o.players p JOIN FETCH o.reward r WHERE o.offerCode = '" + promotionCode +
                "' AND o.triggerType = '" + offerTriggerType + "' AND o.minDeposit <= " + depositAmount + " AND o.maxDeposit >= " + depositAmount + " AND o.currency = '" +
                currency + "' AND o.status = '" + Offer.OfferStatus.ACTIVATED.toString() + "' AND o.startDate < NOW() AND o.endDate > NOW()", Offer.class);


        List<Offer> offers = query.getResultList();
        if(offers.isEmpty()) {
            return null;
        }
        Offer offer = offers.get(0);

        if(offer.getAllPlayers()) {
            //If offer is open to all players, expect player record does not exist
            if(offer.getPlayers().stream().noneMatch(player -> Objects.equals(player.getCustomerId(), customerId))) {
                resultOffer = offer;
            }
        } else {
            //If the offer is only open to selected player, expecting player record should exist for the customer id and status available
            if(offer.getPlayers().stream().anyMatch(player ->
                (Objects.equals(player.getCustomerId(), customerId) && Objects.equals(player.getStatus(), bonusStatus))
            )) {
                resultOffer = offer;
            }
        }

        return resultOffer;
    }

    /**
     * This is to find eligible login offer for a customer
     * @param customerId Wagerplayer customer id
     * @param bonusStatus Customer bonus status
     * @param offerTriggerType Offer trigger type (Deposit, Login, Register)
     * @return Offer result | null
     */
    @Override
    public Offer findLoginOfferByCustomerId(Long customerId, String bonusStatus, String offerTriggerType) {
        TypedQuery<Offer> query = entityManager.createQuery("SELECT o FROM Offer o JOIN FETCH o.players p JOIN FETCH o.reward r WHERE p.customerId = " +
                customerId + " AND p.status = '" + bonusStatus + "' AND o.triggerType = '" + offerTriggerType + "' AND o.status = '" + Offer.OfferStatus.ACTIVATED.toString() + "' AND o.startDate < NOW() AND o.endDate > NOW()", Offer.class);

        Offer offer;
        //If the offer result has been more than one offer, just pick the first one in the list
        List<Offer> offers = query.getResultList();
        if(!offers.isEmpty()) {
            offer = offers.get(0);
            return offer;
        } else {
            return null;
        }
    }

    /**
     * This is to find eligible register offer for a customer
     * @return Offer | null
     */
    @Override
    public Offer findRegisterOfferForCustomer(String promoCode, String offerTriggerType, String offerStatus) {

        TypedQuery<Offer> query;
        String queryStr;

        queryStr = "SELECT o FROM Offer o JOIN FETCH o.reward r WHERE o.triggerType = '" + offerTriggerType;
        //If promo code is provided, then look for offer with that specific promo code(offer code)
        if(!promoCode.isEmpty()) {
            queryStr += "' AND o.offerCode = '" + promoCode + "'";
        //If promo code is not provided, only look for register offer with promoCodeRequired set to false
        } else {
            queryStr += "' AND o.promoCodeRequired = false";
        }
        queryStr += " AND o.status = '"+ offerStatus  + "' AND o.startDate < NOW() AND o.endDate > NOW()";

        query = entityManager.createQuery(queryStr, Offer.class);

        Offer offer;
        //If the offer result has been more than one offer, just pick the first one in the list
        List<Offer> offers = query.getResultList();
        if(!offers.isEmpty()) {
            offer = offers.get(0);
            return offer;
        } else {
            return null;
        }
    }

    /**
     * Update customer bonus status to Activated
     * @param customerId Wagerplayer customer id
     * @param offerId Offer id
     */
    @Transactional
    public void updateBonusStatusById(Long customerId, Long offerId, String status) {

        Query query;
        //If it is activating, update the processed date column as well
        if(Objects.equals(status, Player.PlayerStatus.ACTIVATED.toString()) || Objects.equals(status, Player.PlayerStatus.FAILED.toString())) {
            query = entityManager.createQuery("UPDATE Player p SET p.status = '" + status + "', p.processedDate = now(), p.viewed = false" + " WHERE p.customerId = " +
                    customerId + " AND p.offer.id = " + offerId);
        } else {
            query = entityManager.createQuery("UPDATE Player p SET p.status = '" + status + "', p.viewed = false WHERE p.customerId = " +
                    customerId + " AND p.offer.id = " + offerId);
        }

        query.executeUpdate();
    }

    /**
     * Insert player record(used by register offer only as we cannot insert player list while creating register offer)
     * @param status player status
     * @param customerId Wagerplayer customer id
     * @param offerId offer id
     * @param customerName Wagerplayer customer name
     */
    @Transactional
    public void insertPlayerRecord(String status, Long customerId, Long offerId, String customerName) {

        Query query = entityManager.createNativeQuery("INSERT INTO player (status, viewed, customer_id, offer_id, customer_name) VALUES (?, ?, ?, ?, ?)");
        query.setParameter(1, status);
        query.setParameter(2, 0);
        query.setParameter(3, customerId);
        query.setParameter(4, offerId);
        query.setParameter(5, customerName);

        query.executeUpdate();
    }


    /**
     * This is called when customer viewed the bonus on EF, so it does not show red dot on EF anymore
     * customerId + offerId makes bonus/Player record unique
     * @param customerId Wagerplayer customer id
     * @param offerIds Offer id
     * @param viewedStatus it should set to
     * @return 1 means updated or 0 means nothing updated
     */
    @Transactional
    public int updateBonusViewedStatusById(Long customerId, ArrayList<Long> offerIds, Boolean viewedStatus) {

        Query query = entityManager.createQuery("UPDATE Player p SET p.viewed = " + viewedStatus + " WHERE p.customerId = " +
                customerId + " AND p.offer.id in (:offer_ids)");
        query.setParameter("offer_ids", offerIds);

        return query.executeUpdate();
    }
}
