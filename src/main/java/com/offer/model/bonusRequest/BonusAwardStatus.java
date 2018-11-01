package com.offer.model.bonusRequest;

import com.offer.model.bonusRequest.BonusPlayer;

import java.util.ArrayList;

public class BonusAwardStatus {

    public BonusAwardStatus() {

    }

    //Either Complete or Pending
    private String status;

    private String bonusId;

    private String awardId;

    private ArrayList<BonusPlayer> playersAwarded;

    private ArrayList<BonusPlayer> playersPending;

    private ArrayList<BonusPlayer> playersFailed;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBonusId() {
        return bonusId;
    }

    public void setBonusId(String bonusId) {
        this.bonusId = bonusId;
    }

    public String getAwardId() {
        return awardId;
    }

    public void setAwardId(String bonusAwardId) {
        this.awardId = bonusAwardId;
    }

    public ArrayList<BonusPlayer> getPlayersAwarded() {
        return playersAwarded;
    }

    public void setPlayersAwarded(ArrayList<BonusPlayer> playersAwarded) {
        this.playersAwarded = playersAwarded;
    }

    public ArrayList<BonusPlayer> getPlayersPending() {
        return playersPending;
    }

    public void setPlayersPending(ArrayList<BonusPlayer> playersPending) {
        this.playersPending = playersPending;
    }

    public ArrayList<BonusPlayer> getPlayersFailed() {
        return playersFailed;
    }

    public void setPlayersFailed(ArrayList<BonusPlayer> playersFailed) {
        this.playersFailed = playersFailed;
    }
}
