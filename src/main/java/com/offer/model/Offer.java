package com.offer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.offer.model.reward.BonusReward;
import com.offer.model.reward.FreeSpinReward;
import com.offer.model.reward.Reward;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "Offer")
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank
    private String offerCode;

    @NotBlank
    private String description;

    @NotNull
    private Boolean promoCodeRequired;

    private Date createDate;

    private Date deactivatedDate;

    @NotBlank
    private String triggerType;

    //If the offer applicable to all players
    private Boolean allPlayers;

    @OneToMany(
            mappedBy = "offer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Player> players = new ArrayList<>();

    @OneToOne(
            mappedBy = "offer",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Reward reward;


    private String rewardType;

    @DateTimeFormat
    @Column(name = "startDate")
    private Date startDate;

    @DateTimeFormat
    @Column(name = "endDate")
    private Date endDate;

    //Values are Created, Activated, Deactivated
    private String status;

    public enum OfferStatus {
        //When the offer being created, but not yet activated
        CREATED,
        //Offer being activated
        ACTIVATED,
        //Offer being deactivated
        DEACTIVATED,
        //Offer past endDate
        EXPIRED
    }

    public enum TriggerType {
        DEPOSIT,
        LOGIN,
        REGISTER,
        MANUAL
    }

    public enum RewardType {
        FREESPINS,
        BONUS
    }

    public enum Providers {
        NETENT,
        QUICKSPIN,
        NYX
    }


    //Deposit or Register-Deposit Offer
    private float minDeposit;
    private float maxDeposit;
    private boolean firstDeposit;
    private String currency;


    //Login

    //Manual


    //Register
    private String registerChannel;
    @NotNull
    private Boolean depositRequired;


    public static final String POUND = "£";
    public static final String EURO = "€";




    //Default Constructor
    public Offer() {

    }


    //Login/Manual Offer Constructor
    public Offer(String offerCode, String description, String triggerType, Reward reward, String rewardType, Date startDate, Date endDate, String status, Boolean promoCodeRequired, Boolean allPlayers, Boolean depositRequired) {
        this.offerCode = offerCode;
        this.description = description;
        this.triggerType = triggerType;
        this.reward = reward;
        this.rewardType = rewardType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.promoCodeRequired = promoCodeRequired;
        this.allPlayers = allPlayers;
        this.createDate = new Date();
        this.depositRequired = depositRequired;
    }

    //Deposit Offer Constructor
    public Offer(String offerCode, String description, String triggerType, Reward reward, String rewardType, Date startDate, Date endDate, String status, Boolean promoCodeRequired, Boolean allPlayers, Boolean depositRequired, float minDeposit, float maxDeposit, boolean firstDeposit, String currency) {

        this(offerCode, description, triggerType, reward, rewardType, startDate, endDate, status, promoCodeRequired, allPlayers, depositRequired);
        this.minDeposit = minDeposit;
        this.maxDeposit = maxDeposit;
        this.firstDeposit = firstDeposit;
        this.currency = currency;
    }

    //Register Offer Constructor
    public Offer(String offerCode, String description, String triggerType, Reward reward, String rewardType, Date startDate, Date endDate, String status, Boolean promoCodeRequired, Boolean allPlayers, Boolean depositRequired, String registerChannel) {

        this(offerCode, description, triggerType, reward, rewardType, startDate, endDate, status, promoCodeRequired, allPlayers, depositRequired);
        this.registerChannel = registerChannel;
    }

    public Long getOffer_id() {
        return id;
    }

    public void setOffer_id(Long offer_id) {
        this.id = offer_id;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
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

    public String getRewardType() {
        return rewardType;
    }

    public void setRewardType(String rewardType) {
        this.rewardType = rewardType;
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

    public float getMinDeposit() {
        return minDeposit;
    }

    public String getMinDepositText() {
        return Objects.equals(currency, "GBP") ? (POUND + String.format("%.2f", minDeposit)) : (EURO + String.format("%.2f", minDeposit));
    }

    public void setMinDeposit(float minDeposit) {
        this.minDeposit = minDeposit;
    }

    public float getMaxDeposit() {
        return maxDeposit;
    }

    public String getMaxDepositText() {
        return Objects.equals(currency, "GBP") ? (POUND + String.format("%.2f", maxDeposit)) : (EURO + String.format("%.2f", maxDeposit));
    }

    public void setMaxDeposit(float maxDeposit) {
        this.maxDeposit = maxDeposit;
    }

    public boolean getPromoCodeRequired() {
        return promoCodeRequired;
    }

    public void setPromoCodeRequired(boolean promoCodeRequired) {
        this.promoCodeRequired = promoCodeRequired;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isFirstDeposit() {
        return firstDeposit;
    }

    public void setFirstDeposit(boolean firstDeposit) {
        this.firstDeposit = firstDeposit;
    }

    public String getRegisterChannel() {
        return registerChannel;
    }

    public void setRegisterChannel(String registerChannel) {
        this.registerChannel = registerChannel;
    }

    public void setPlayers(List<Player> players) {
        this.players.clear();
        this.players.addAll(players);
    }

    public void clearPlayers() {
        this.players.clear();
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    //Used only when query bonus per customer, then there should only be one player attached to the offer object
    @JsonIgnore
    public Player getFirstPlayer() {
        return this.players.get(0);
    }

    public Player findPlayerById(Long customerId) {
        return this.players.stream().filter(x -> Objects.equals(x.getCustomerId(), customerId)).findAny().orElse(null);
    }

    public Reward getReward() {
        return reward;
    }

    public void setReward(Reward reward) {
        this.reward = reward;
    }

    public void setfreeSpinReward(FreeSpinReward freeSpinReward) {
        this.reward = freeSpinReward;
    }

    public void setbonusReward(BonusReward bonusReward) {
        this.reward = bonusReward;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setAllPlayers(Boolean allPlayers) {
        this.allPlayers = allPlayers;
    }

    public Boolean getAllPlayers() {
        return allPlayers;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getDeactivatedDate() {
        return deactivatedDate;
    }

    public void setDeactivatedDate(Date deactivatedDate) {
        this.deactivatedDate = deactivatedDate;
    }

    public Boolean getDepositRequired() {
        return depositRequired;
    }

    public void setDepositRequired(Boolean depositRequired) {
        this.depositRequired = depositRequired;
    }
}
