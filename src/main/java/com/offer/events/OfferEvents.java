package com.offer.events;

import com.offer.model.Offer;
import com.offer.model.Player;
import org.springframework.context.ApplicationEvent;

import java.util.Objects;

public class OfferEvents extends ApplicationEvent {

    public enum EventMessageType {
        //Notification message when offer has been assigned to the customer and activated
        AVAILABLE,
        //Notification message when offer has been triggered and free spins awarded to the customer
        ACTIVATED,
        //Notification message when offer has been triggered but free spins award failed for the customer
        FAILED,
    }

    /*
    Json object of data to be published
     */
    private Offer data;

    private Player player;

    private EventMessageType eventMessageType;

    private Long customerId;

    private int numOfFreeSpins;

    private Boolean multiGames;

    private String offerObject;

    private String gameCode;

    private Long offerId;

    private String offerCode;


    public OfferEvents(Object source, Offer data, Player player, EventMessageType eventMessageType) {
        super(source);
        this.data = data;
        this.player = player;
        this.eventMessageType = eventMessageType;
    }

    public OfferEvents(Object source, Long customerId, Offer data, EventMessageType eventMessageType) {
        super(source);
        this.customerId = customerId;
        this.data = data;
        this.eventMessageType = eventMessageType;
    }


    public Offer getData() {
        return data;
    }

    public Player getPlayer() {
        return player;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getOfferId() {
        return offerId;
    }

    public int getNumOfFreeSpins() {
        return numOfFreeSpins;
    }

    public Boolean getMultiGames() {
        return multiGames;
    }

    public String getGameCode() {
        return gameCode;
    }

    public String getOfferObject() {
        return offerObject;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public EventMessageType getEventMessageType() {
        return eventMessageType;
    }

    public void setEventMessageType(EventMessageType eventMessageType) {
        this.eventMessageType = eventMessageType;
    }
}
