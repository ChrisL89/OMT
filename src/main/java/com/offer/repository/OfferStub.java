/*
package com.offer.repository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.offer.model.Offer;
import com.offer.model.Player;
import com.offer.model.reward.FreeSpinReward;
import org.offer.stereotype.Repository;


@Repository
public class OfferStub{
    private Map<Long, Offer> offers = new HashMap<>();
    private Long offerRewardIdIndex = 3L;
    private Long playerIdIndex = 7L;

    OfferStub() {
        offers = initialData();
    }


    public List<Offer> list() {
        return new ArrayList<>(offers.values());
    }

    public Offer create(Offer offer) {
        offerRewardIdIndex += 1;
        offer.setOffer_id(offerRewardIdIndex);

        //Update reward ID
        //offer.getReward().setOffer_id(offerRewardIdIndex);

        //Going throw players to update offer id and player id
        offer.getPlayers().forEach(player -> {
            playerIdIndex += 1;
            player.setOffer(offer);
            player.setId(playerIdIndex);
        });

        offers.put(offer.getOffer_id(), offer);
        return offer;
    }

    public Offer get(Long id) {
        return offers.get(id);
    }

    public Offer update(Long id, Offer offer) {
        offers.put(id, offer);
        return offer;
    }

    public Offer delete(Long id) {
        return offers.remove(id);
    }



    public boolean exists(String offerCode) {
        for (Offer offer : offers.values()) {
            if(Objects.equals(offer.getOfferCode(), offerCode)) {
                return true;
            }
        }

        return false;
    }



    private Map<Long, Offer> initialData() {

        offers = new HashMap<>();

        Date startDate = null;
        Date endDate = null;

        //populate initial wrecks
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            startDate = sdf.parse("2017-6-1");
            endDate = sdf.parse("2017-11-30");
        } catch(ParseException e) {
            e.printStackTrace();
            System.out.print("you get the ParseException");
        }



        //Create Reward
        ArrayList<String> gameList = new ArrayList<>() {{
            add("NetEntGameA");
            add("NetEntGameB");
            add("NetEntGameC");
        }};
        FreeSpinReward freeSpinReward = new FreeSpinReward(Offer.Providers.NETENT.ordinal(), gameList, 0.2F, 20);
        freeSpinReward.setId(1L);
        //Create Offer
        Offer loginOffer = new Offer(1L, "LOG01", "Login offer 01", Offer.TriggerType.LOGIN, freeSpinReward, Offer.RewardType.FREESPINS, startDate, endDate, true);
        //Create Players
        ArrayList<Player> players = new ArrayList<>();
        players.add(new Player(1L, 3,loginOffer, false));
        players.add(new Player(2L, 4,loginOffer,false));

        loginOffer.setPlayers(players);
        offers.put(1L, loginOffer);




        //Create Reward
        gameList = new ArrayList<>() {{
            add("NetEntGameA");
            add("NetEntGameB");
        }};
        freeSpinReward = new FreeSpinReward(Offer.Providers.NETENT.ordinal(), gameList, 0.3F, 15);
        freeSpinReward.setId(2L);
        //Create Offer
        Offer depositOffer = new Offer(2L, "DEP02", "Deposition offer 02", Offer.TriggerType.DEPOSIT, freeSpinReward, Offer.RewardType.FREESPINS, startDate, endDate, true, 50, 100, true);
        //Create Players
        players = new ArrayList<>();
        players.add(new Player(3L, 4,depositOffer, false));
        players.add(new Player(4L, 5,depositOffer,false));

        depositOffer.setPlayers(players);
        offers.put(2L, depositOffer);




        //Create Reward
        gameList = new ArrayList<>() {{
            add("NetEntGameA");
            add("NetEntGameD");
        }};
        freeSpinReward = new FreeSpinReward(Offer.Providers.NETENT.ordinal(), gameList, 0.5F, 10);
        freeSpinReward.setId(3L);
        //Create Offer
        Offer registrationOffer = new Offer(3L, "REG03", "Register offer 03", Offer.TriggerType.REGISTER, freeSpinReward, Offer.RewardType.FREESPINS, startDate, endDate, true, "iphone");
        //Create Players
        players = new ArrayList<>();
        players.add(new Player(5L, 4,registrationOffer, false));
        players.add(new Player(6L, 5,registrationOffer,false));
        players.add(new Player(7L, 6,registrationOffer,false));

        offers.put(3L, registrationOffer);
        registrationOffer.setPlayers(players);


        return offers;
    }
}











*/
