package com.offer.listeners;

import com.offer.events.OfferEvents;
import com.offer.model.Game;
import com.offer.model.Offer;
import com.offer.model.Player;
import com.offer.model.reward.FreeSpinReward;
import com.offer.model.reward.Reward;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.json.simple.JSONObject;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class OfferEventListenerTest extends TestCase{

    public OfferEventListenerTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( OfferEventListenerTest.class );
    }

    /**
     * Test Set Payload for available notification work as expected
     */
    public void testSetPayloadForAvailable() {

        //Setup
        List<Offer> offerList = new ArrayList<>();
        Player player = new Player(1L,"customer name1", "ACTIVE", false);
        ArrayList<Game> gameList = new ArrayList<>();
        Game game1 = new Game();
        Game game2 = new Game();
        Game game3 = new Game();
        gameList.add(game1);
        gameList.add(game2);
        gameList.add(game3);
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, 1);
        Date endDate = c.getTime();
        Reward reward = new FreeSpinReward("netent", false, gameList, 2, 5);
        Offer offer = new Offer("Deposit_Offer", "Deposit offer", Offer.TriggerType.DEPOSIT.toString(), reward, Offer.RewardType.FREESPINS.toString(), startDate, endDate, Offer.OfferStatus.ACTIVATED.toString(), true, true, false, 10, 30, false, "GBP");
        OfferEvents event = new OfferEvents(this, offer, player, OfferEvents.EventMessageType.AVAILABLE);
        OfferEventListener offerEventListener = new OfferEventListener();

        //Action
        offerEventListener.setPayload(event);

        //Assert
        JSONObject payload = offerEventListener.getPayload();
        JSONObject content = (JSONObject) payload.get("content");
        JSONObject args = (JSONObject) content.get("args");

        assertEquals("Check offer status", OfferEvents.EventMessageType.AVAILABLE.toString(), content.get("status").toString());
        assertEquals("Check min deposit", "£10.00", args.get("minDepositAmount"));
        assertEquals("Check max deposit", "£30.00", args.get("maxDepositAmount"));
        assertEquals("Check player id", "1", payload.get("to"));
        assertEquals("Check free spins number", 5, args.get("freeSpinNumber"));
    }

    public void testSetPayloadForActivated() {
        //Setup
        List<Offer> offerList = new ArrayList<>();
        Player player = new Player(1L,"customer name1", "ACTIVE", false);
        ArrayList<Game> gameList = new ArrayList<>();
        Game game1 = new Game();
        game1.setGameId("Game Id 1");
        Game game2 = new Game();
        game2.setGameId("Game Id 2");
        gameList.add(game1);
        gameList.add(game2);
        Date startDate = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        c.add(Calendar.DATE, 1);
        Date endDate = c.getTime();
        Reward reward = new FreeSpinReward("netent", false, gameList, 2, 5);
        Offer offer = new Offer("Deposit_Offer", "Deposit offer", Offer.TriggerType.DEPOSIT.toString(), reward, Offer.RewardType.FREESPINS.toString(), startDate, endDate, Offer.OfferStatus.ACTIVATED.toString(), true, true, false, 10, 30, false, "GBP");
        offer.setOffer_id(1L);
        OfferEvents event = new OfferEvents(this, 1L, offer, OfferEvents.EventMessageType.ACTIVATED);
        OfferEventListener offerEventListener = new OfferEventListener();

        //Action
        offerEventListener.setPayload(event);

        //Assert
        JSONObject payload = offerEventListener.getPayload();
        JSONObject content = (JSONObject) payload.get("content");
        JSONObject args = (JSONObject) content.get("args");

        assertEquals("Check offer status", "ACTIVATED_MULTIPLE", content.get("status").toString());
        assertEquals("Check player id", "1", payload.get("to"));
        assertEquals("Check free spins number", 5, args.get("freeSpinNumber"));
        assertEquals("Check offer id", "1", args.get("offerId"));
    }


}
