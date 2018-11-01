package com.offer.controller;

import com.offer.httpClient.httpClientHelper;
import com.offer.model.ActivationHistory;
import com.offer.model.Offer;
import com.offer.model.Player;
import com.offer.model.bonusRequest.BonusPlayer;
import com.offer.repository.ActivationHistoryRepository;
import com.offer.repository.offerRepository.OfferRepositoryImpl;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.offer.helper.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class ProcessMessageController {

/*    array(
	        5 => 'CC Deposit',
            9 => 'Paypal Deposit',
            11 => 'BPAY Deposit',
            13 => 'EFT Deposit',
            14 => 'Cash Deposit',
            23 => 'Cheque',
            46 => 'NETELLER Deposit',
            103 => 'Bank to Bank',
            104 => 'PaysafeCard Deposit',
            303 => 'Casino',
            309 => 'Casino',
    );*/
    private static String[] depositMethods = {"5","9","11","13","14","23","46","103","104","303","309"};

    @Autowired
    OfferRepositoryImpl offerRepositoryImpl;

    @Autowired
    ActivationHistoryRepository activationHistoryRepository;

    private static final int REGISTER_PROCESS_DELAY = 5;


    /**
     * Convert message and process it by looking over existing offer of that type and applicable to this customer
     * @param jsonObject customer activity packet from Tamale
     */
    public void processMessage(JSONObject jsonObject) throws Exception {

        JSONObject jsonTransaction = (JSONObject) ((JSONArray)  jsonObject.get("transaction")).get(0);
        String objectType = (String) jsonTransaction.get("object_type");

        if (Objects.equals(objectType, "transaction")) {
            Logger.log("Transaction message received", Logger.LogType.INFO);
            processTransactionMessage(jsonTransaction);
        } else if(Objects.equals(objectType, "login")) {
            Logger.log("Login message received", Logger.LogType.INFO);
            processLoginMessage(jsonTransaction);
        } else if(Objects.equals(objectType, "register")) {
            Logger.log("Register message received", Logger.LogType.INFO);
            processRegisterMessage(jsonTransaction);
        }
    }

    public void processTransactionMessage(JSONObject jsonObject) throws Exception {

        //try catch when casting values, as Packet message format seems to be inconsistent.
        try {
            JSONObject payloadArray = (JSONObject) jsonObject.get("payload");
            String transaction_type = (String) payloadArray.get("transaction_type");
            //Confirm transaction type is one of deposit type
            if(Arrays.asList(depositMethods).contains(transaction_type)) {
                Logger.log("Start processing deposit transaction", Logger.LogType.INFO);
                String dateTimeStr = (String) jsonObject.get("timestamp");
                Long customer_id = Long.parseLong((String) payloadArray.get("customer_id"));
                String customerName = (String) payloadArray.get("customer_username");
                Float amount = Float.parseFloat((String) payloadArray.get("transaction_amount"));
                String currency = (String) payloadArray.get("currency");

                String promotion_code = (String) payloadArray.get("promotion_code");
                Offer offer;
                //Depends on whether promo code is provided, use different condition to search for the offer
                if(promotion_code.isEmpty()) {
                    //If promo code is empty during deposit, it could try to trigger a deposit offer or a register-deposit offer(in deposit step)
                    offer = offerRepositoryImpl.findDepositOfferByCustomerId(customer_id, amount, currency);
                } else {
                    offer = offerRepositoryImpl.findDepositOfferByCustomerId(customer_id, Player.PlayerStatus.AVAILABLE.toString(), Offer.TriggerType.DEPOSIT.toString(), amount, currency, promotion_code);
                }

                if(offer != null) {
                    Logger.log("Offer has been found with offer Id: " + offer.getOffer_id(), Logger.LogType.INFO);
                    //Send add free spin request to GSI
                    String rgsCode = offer.getReward().getProviderName();
                    String offerCode = offer.getOfferCode();
                    ArrayList<BonusPlayer> players = new ArrayList<>();
                    Player player = offer.findPlayerById(customer_id);
                    //player might be null if it is open to all, so we need to construct it from tamale message
                    if(player == null) {
                        players.add(new BonusPlayer(customer_id, customerName));
                    } else {
                        players.add(new BonusPlayer(player.getCustomerId(), player.getCustomerName()));
                    }

                    String bonusAwardId = offer.getOffer_id().toString() + "_" + customer_id + System.getProperty("gsi.api.bonus.bonus-auto-award-suffix");

                    if(!httpClientHelper.awardBonus(rgsCode, offerCode, bonusAwardId, players)) {
                        Logger.log("Awarding deposit offer with offer Id: " + offer.getOffer_id() + " and customer id: " + customer_id + " failed", Logger.LogType.INFO);
                        throw new Exception("Awarding Deposit bonus failed");
                    }

                    String message;
                    if(Objects.equals(offer.getTriggerType(), Offer.TriggerType.REGISTER.toString())) {
                        //Update Bonus(Player record) status
                        Logger.log("Register offer Updating Player status", Logger.LogType.INFO);
                        offerRepositoryImpl.updateBonusStatusById(customer_id, offer.getOffer_id(), Player.PlayerStatus.TRIGGERED.toString());
                        message = "Customer ID: " + customer_id + " has deposited: " + amount + " " + currency + " on " + dateTimeStr +
                                " and triggered " + offerCode + " on deposit part";
                    } else {
                        if(offer.getAllPlayers()) {
                            //Insert Bonus(Player record)
                            Logger.log("Deposit offer Inserting Player record", Logger.LogType.INFO);
                            offerRepositoryImpl.insertPlayerRecord(Player.PlayerStatus.TRIGGERED.toString(), customer_id, offer.getOffer_id(), customerName);
                        } else {
                            //Update Bonus(Player record) status
                            Logger.log("Deposit offer Updating Player status", Logger.LogType.INFO);
                            offerRepositoryImpl.updateBonusStatusById(customer_id, offer.getOffer_id(), Player.PlayerStatus.TRIGGERED.toString());
                        }
                        message = "Customer ID: " + customer_id + " has deposited: " + amount + " " + currency + " on " + dateTimeStr +
                                " and triggered " + offerCode;
                    }
                    //Insert activation history record
                    updateActivationHistory(customer_id, offer.getOffer_id(), message);

                    //TODO Maybe we need to notify Mailman that awarding bonus has been triggered and customer will receive it soon

                } else {
                    Logger.log("Offer is not found", Logger.LogType.INFO);
                }
            } else {
                Logger.log("Not a valid deposit transaction", Logger.LogType.WARN);
            }
        } catch (ClassCastException e) {
            Logger.log("Not a valid transaction packet format with cast exception: " + e.getMessage(), Logger.LogType.ERROR);
        }
    }

    public void processLoginMessage(JSONObject jsonObject) throws Exception {

        //try catch when casting values, as Packet message format seems to be inconsistent.
        try {
            JSONObject payloadArray = (JSONObject) jsonObject.get("payload");
            Logger.log("Start processing login transaction", Logger.LogType.INFO);
            String dateTimeStr = (String) jsonObject.get("timestamp");
            Long customer_id = Long.parseLong((String) payloadArray.get("customer_id"));
            //Try to find all bonuses this customer have with type deposit and amount is in the range.
            Offer offer = offerRepositoryImpl.findLoginOfferByCustomerId(customer_id, Player.PlayerStatus.AVAILABLE.toString(), Offer.TriggerType.LOGIN.toString());

            if(offer != null) {
                Logger.log("Offer has been found with offer Id: {" + offer.getOffer_id() + "}", Logger.LogType.INFO);
                //Send add free spin request to GSI
                String rgsCode = offer.getReward().getProviderName();
                String offerCode = offer.getOfferCode();
                ArrayList<BonusPlayer> players = new ArrayList<>();
                Player player = offer.findPlayerById(customer_id);
                players.add(new BonusPlayer(player.getCustomerId(), player.getCustomerName()));
                String bonusAwardId = offer.getOffer_id().toString() + "_" + player.getCustomerId() + System.getProperty("gsi.api.bonus.bonus-auto-award-suffix");

                if(!httpClientHelper.awardBonus(rgsCode, offerCode, bonusAwardId, players)) {
                    Logger.log("Awarding deposit offer with offer Id: {" + offer.getOffer_id() + "} and customer id: {" + customer_id + "} failed", Logger.LogType.INFO);
                    throw new Exception("Awarding Deposit bonus failed");
                }

                //Update Bonus(Player record) status
                offerRepositoryImpl.updateBonusStatusById(customer_id, offer.getOffer_id(), Player.PlayerStatus.TRIGGERED.toString());

                //Insert activation history record
                String message = "Customer ID: " + customer_id + " has logged in on " + dateTimeStr +
                        " and triggered " + offerCode;
                updateActivationHistory(customer_id, offer.getOffer_id(), message);

                //TODO Maybe we need to notify Mailman that awarding bonus has been triggered and customer will receive it soon
            } else {
                Logger.log("Offer is not found", Logger.LogType.INFO);
            }
        } catch (ClassCastException e) {
            Logger.log("Not a valid login packet format with cast exception: {" + e.getMessage() + "}", Logger.LogType.INFO);
        }
    }

    public void processRegisterMessage(JSONObject jsonObject) throws Exception {

        //try catch when casting values, as Packet message format seems to be inconsistent.
        try {
            JSONObject payloadArray = (JSONObject) jsonObject.get("payload");
            Logger.log("Start processing register transaction", Logger.LogType.INFO);
            String promoCode = (String) payloadArray.get("promotion_code");

            //Try to find first bonus this customer have with type register
            Offer offer = offerRepositoryImpl.findRegisterOfferForCustomer(promoCode, Offer.TriggerType.REGISTER.toString(), Offer.OfferStatus.ACTIVATED.toString());

            if(offer != null) {
                String dateTimeStr = (String) jsonObject.get("timestamp");
                Long customer_id = Long.parseLong((String) payloadArray.get("customer_id"));
                String customerName = (String) payloadArray.get("customer_username");
                String offerCode = offer.getOfferCode();
                String message;

                if(offer.getDepositRequired()) {
                    //Insert Bonus(Player record)
                    Logger.log("Inserting Player record", Logger.LogType.INFO);
                    offerRepositoryImpl.insertPlayerRecord(Player.PlayerStatus.REGISTERED.toString(), customer_id, offer.getOffer_id(), customerName);
                    //Insert activation history record, there is no player records to update, as register offer does not have player records
                    message = "Customer ID: " + customer_id + " has registered in on " + dateTimeStr +
                            " and triggered " + offerCode + " on register part";
                } else {
                    String bonusAwardId = offer.getOffer_id().toString() + "_" + customer_id + System.getProperty("gsi.api.bonus.bonus-auto-award-suffix");
                    String rgsCode = offer.getReward().getProviderName();
                    ArrayList<BonusPlayer> players = new ArrayList<>();
                    players.add(new BonusPlayer(customer_id, customerName));
                    //SUN-8063: Adding a delay if it is an pure register offer,
                    //this is to allow customer registration to complete in Casino Provider side before try to award free spins.
                    TimeUnit.SECONDS.sleep(REGISTER_PROCESS_DELAY);
                    if(!httpClientHelper.awardBonus(rgsCode, offerCode, bonusAwardId, players)) {
                        Logger.log(" Awarding register offer with offer Id: {" + offer.getOffer_id() + "} and customer id: {" + customer_id+ "} failed", Logger.LogType.INFO);
                        throw new Exception("Awarding register bonus failed");
                    }
                    //Insert Bonus(Player record)
                    Logger.log(" Inserting Player record", Logger.LogType.INFO);
                    offerRepositoryImpl.insertPlayerRecord(Player.PlayerStatus.TRIGGERED.toString(), customer_id, offer.getOffer_id(), customerName);

                    //Insert activation history record, there is no player records to update, as register offer does not have player records
                     message = "Customer ID: " + customer_id + " has registered in on " + dateTimeStr +
                            " and triggered " + offerCode;
                }
                updateActivationHistory(customer_id, offer.getOffer_id(), message);

                //TODO Maybe we need to notify Mailman that awarding bonus has been triggered and customer will receive it soon
            } else {
                Logger.log("Offer is not found", Logger.LogType.INFO);
            }
        } catch (ClassCastException e) {
            Logger.log("Not a valid register packet format with cast exception: {" + e.getMessage() + "}", Logger.LogType.ERROR);
        }
    }


    private void updateActivationHistory(Long customer_id, Long offer_id, String message) {

        //Insert activation history record
        ActivationHistory activationHistory = new ActivationHistory();
        activationHistory.setCustomerId(customer_id);
        activationHistory.setOfferId(offer_id);
        activationHistory.setActivationMessage(message);
        activationHistoryRepository.saveAndFlush(activationHistory);

    }

}
