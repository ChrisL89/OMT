package com.offer.model;

import com.offer.model.reward.Reward;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class BonusDtoTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public BonusDtoTest(String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( BonusDtoTest.class );
    }

    /**
     * Test the conversion function
     */
    public void testConvertToDto() {

        //Setup
        List<Offer> offerList = new ArrayList<>();
        Player player = new Player(1L,"customer name1", "ACTIVE", false);
        List<Player> playerList = new ArrayList<>();
        playerList.add(player);
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
        Reward reward = new Reward("netent", false, gameList);
        Offer offer1 = new Offer("Login_Offer", "Login offer", Offer.TriggerType.LOGIN.toString(), reward, Offer.RewardType.FREESPINS.toString(), startDate, endDate, Offer.OfferStatus.ACTIVATED.toString(), true, true, false);
        Offer offer2 = new Offer("Deposit_Offer", "Deposit offer", Offer.TriggerType.DEPOSIT.toString(), reward, Offer.RewardType.FREESPINS.toString(), startDate, endDate, Offer.OfferStatus.EXPIRED.toString(), false, true, true);
        offer1.setPlayers(playerList);
        offer2.setPlayers(playerList);
        offerList.add(offer1);
        offerList.add(offer2);


        //Action
        List<BonusDto> result = BonusDto.convertToDtoList(offerList);

        //Assert
        assertEquals("Expect same number of the offers", 2, result.size());
        assertEquals("Expect offer code is the same", "Login_Offer", result.get(0).getOfferCode());
        assertEquals("Expect description is the same", "Login offer", result.get(0).getDescription());
        assertEquals("Expect trigger type is the same", Offer.TriggerType.DEPOSIT.toString(), result.get(1).getTriggerType());
        assertEquals("Expect number of games are the same", 3, result.get(1).getGames().size());
        assertEquals("Expect offer status is the same", "netent", result.get(1).getProviderName());


    }
}
