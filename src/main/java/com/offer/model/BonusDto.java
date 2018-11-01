package com.offer.model;

import com.offer.model.reward.FreeSpinReward;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BonusDto {

    private String offerCode;

    private Date startDate;

    private Date endDate;

    private Date processedDate;

    private String description;

    private String triggerType;

    private String bonusStatus;

    private String currency;

    private Boolean viewed;

    private Boolean allowAllGames;

    private String providerName;

    private List<Game> games;

    private int numOfFreeSpins;

    private int coinLevel;

    private float minDeposit;

    private float maxDeposit;

    private Long offerId;

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public String getBonusStatus() {
        return bonusStatus;
    }

    public void setBonusStatus(String bonusStatus) {
        this.bonusStatus = bonusStatus;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    public Boolean getAllowAllGames() {
        return allowAllGames;
    }

    public void setAllowAllGames(Boolean allowAllGames) {
        this.allowAllGames = allowAllGames;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(ArrayList<Game> games) {
        this.games = games;
    }

    public int getNumOfFreeSpins() {
        return numOfFreeSpins;
    }

    public void setNumOfFreeSpins(int numOfFreeSpins) {
        this.numOfFreeSpins = numOfFreeSpins;
    }

    public int getCoinLevel() {
        return coinLevel;
    }

    public void setCoinLevel(int coinLevel) {
        this.coinLevel = coinLevel;
    }

    public float getMinDeposit() {
        return minDeposit;
    }

    public void setMinDeposit(float minDeposit) {
        this.minDeposit = minDeposit;
    }

    public float getMaxDeposit() {
        return maxDeposit;
    }

    public void setMaxDeposit(int minDeposit) {
        this.minDeposit = minDeposit;
    }

    public long getOfferId() {
        return offerId;
    }

    public void setOfferId(long offerId) {
        this.offerId = offerId;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    private BonusDto() {

    }

    public BonusDto(String offerCode, Date startDate, Date endDate, String description, String triggerType, String bonusStatus, Boolean viewed, Boolean allowAllGames, String providerName, ArrayList<Game> games, Long offerId, Date processedDate, String currency) {
        this.offerCode = offerCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.processedDate = processedDate;
        this.description = description;
        this.triggerType = triggerType;
        this.bonusStatus = bonusStatus;
        this.viewed = viewed;
        this.allowAllGames = allowAllGames;
        this.providerName = providerName;
        this.games = games;
        this.offerId = offerId;
        this.currency = currency;
    }


    private void convertToDto(Offer offer) {

        this.offerCode = offer.getOfferCode();
        this.startDate = offer.getStartDate();
        this.endDate = offer.getEndDate();
        this.description = offer.getDescription();
        this.triggerType = offer.getTriggerType();
        this.bonusStatus = offer.getFirstPlayer().getStatus();
        this.viewed = offer.getFirstPlayer().getViewed();
        this.allowAllGames = offer.getReward().getAllowAllGames();
        this.providerName = offer.getReward().getProviderName();
        this.games = offer.getReward().getGames();
        this.offerId = offer.getOffer_id();
        this.maxDeposit = offer.getMaxDeposit();
        this.minDeposit = offer.getMinDeposit();
        this.processedDate = offer.getFirstPlayer().getProcessedDate();
        this.currency = offer.getCurrency();

        if(offer.getReward() instanceof FreeSpinReward) {
            this.numOfFreeSpins = ((FreeSpinReward) offer.getReward()).getNumOfFreeSpins();
            this.coinLevel = ((FreeSpinReward) offer.getReward()).getCoinLevel();
        }
    }

    public static List<BonusDto> convertToDtoList(List<Offer> offerList) {
        List<BonusDto> bonuses = new ArrayList<>();

        for (Offer offer : offerList) {
            BonusDto bonusDto = new BonusDto();
            bonusDto.convertToDto(offer);
            bonuses.add(bonusDto);
        }

        return bonuses;
    }


}
