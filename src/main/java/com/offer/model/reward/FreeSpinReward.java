package com.offer.model.reward;



import com.offer.model.Game;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.ArrayList;

@Entity
@DiscriminatorValue("freeSpinReward")
public class FreeSpinReward extends Reward {

    //@NotEmpty(message = "Please specify coin value")
    private int coinLevel;

    @Column(name = "numOfFreeSpins")
    //@NotEmpty(message = "Please specify number of free spins")
    private int numOfFreeSpins;


    public FreeSpinReward(String providerName, Boolean allowAllGames, ArrayList<Game> games, int coinLevel, int numOfFreeSpins) {
        super(providerName, allowAllGames, games);
        this.coinLevel = coinLevel;
        this.numOfFreeSpins = numOfFreeSpins;
    }

    //Default Constructor
    public FreeSpinReward() {

    }

    public int getCoinLevel() {
        return coinLevel;
    }

    public void setCoinLevel(int coinValue) {
        this.coinLevel = coinValue;
    }

    public int getNumOfFreeSpins() {
        return numOfFreeSpins;
    }

    public void setNumOfFreeSpins(int numOfFreeSpins) {
        this.numOfFreeSpins = numOfFreeSpins;
    }
}
