package com.offer.model.bonusRequest;

public class BonusPlayer {

    public BonusPlayer() {

    }

    public BonusPlayer(Long playerId, String username) {
        this.playerId = playerId;
        this.username = username;
    }

    private String username;

    private Long playerId;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
