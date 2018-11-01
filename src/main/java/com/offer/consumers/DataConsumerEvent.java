package com.offer.consumers;


/**
 * Created by erwan on 31/10/15.
 *
 *
 */
public class DataConsumerEvent {

    private String data;

    public DataConsumerEvent(String data) {
        this.data = data;
    }

    public String getFixture() {
        return data;
    }
}
