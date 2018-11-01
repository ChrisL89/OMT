package com.offer.model.RequestBody;

import java.util.ArrayList;

public class ViewOffer {

    private ArrayList<Long> offerIds = new ArrayList<>();

    private Long customerId;


    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public ArrayList<Long> getOfferIds() {
        return offerIds;
    }

    public void setOfferIds(ArrayList<Long> offerIds) {
        this.offerIds = offerIds;
    }
}
