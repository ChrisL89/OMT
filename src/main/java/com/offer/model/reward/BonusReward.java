package com.offer.model.reward;

import com.offer.model.Game;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("bonusReward")
public class BonusReward extends Reward {

    //Class not yet being used
    public BonusReward() {

    }

    public BonusReward(String providerName, Boolean allowAllGames, ArrayList<Game> games) {
        super(providerName, allowAllGames, games);
    }

}
