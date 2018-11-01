package com.offer.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;

//TODO this really should be called bonus table(instance of Offer specific to a player)
@Entity
@Table(name="Player")
public class Player {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long customerId;

    private String customerName;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    //this is the bonus status for the player, it can be: available, activated, used, expired, deactivated and etc
    private String status;

    private Boolean viewed;

    private Date processedDate;

    public enum PlayerStatus {
        //When offer is created, the instance of offer(bonus) for a customer is set to pending
        PENDING,
        //offer is activated
        AVAILABLE,
        //Bonus being triggered and under process by provider to award free spins
        TRIGGERED,
        //This is a specific status to register-deposit offer
        REGISTERED,
        //Failed to award free spins by GSI
        FAILED,
        //bonus being awarded to customer with certain actions(deposit,login etc)
        //Activated can only go to USED, not other status
        ACTIVATED,
        //Operator deactivated Offer, so bonus that are in PENDING/AVAILABLE status being deactivated as well
        DEACTIVATED,
        //Bonus being used up
        USED,
        //set this bonus status when offer is expired
        EXPIRED,
        //set this bonus status when Free spins expired
        FREESPINEXPIRED
    }

    public Player() {

    }

    public Player(Long customerId, String customerName, String status, Boolean viewed) {
        this.customerId = customerId;
        this.status = status;
        this.viewed = viewed;
        this.customerName = customerName;
    }

    public Player(Long customerId, String customerName, Offer offer, String status, Boolean viewed) {
        this.customerId = customerId;
        this.offer = offer;
        this.status = status;
        this.viewed = viewed;
        this.customerName = customerName;
    }

    public Player(Long id, Long customerId, String customerName, Offer offer, String status, Boolean viewed) {
        this(customerId, customerName, offer, status, viewed);
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Boolean getViewed() {
        return viewed;
    }

    public void setViewed(Boolean viewed) {
        this.viewed = viewed;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

    public void setProcessedDate(Date processedDate) {
        this.processedDate = processedDate;
    }
}
