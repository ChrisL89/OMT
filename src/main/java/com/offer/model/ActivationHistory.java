package com.offer.model;


import javax.persistence.*;

@Entity
@Table(name = "activation_history")
public class ActivationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long customerId;

    private Long offerId;

    private String ActivationMessage;


    public ActivationHistory() {

    }


    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public void setOfferId(Long offerId) {
        this.offerId = offerId;
    }

    public String getActivationMessage() {
        return ActivationMessage;
    }

    public void setActivationMessage(String activationMessage) {
        ActivationMessage = activationMessage;
    }
}
