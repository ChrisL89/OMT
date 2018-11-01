package com.offer.consumers;

/**
 * Created by erwan on 31/10/15.
 *
 * Interface for Fixture Consumed Event Listener
 */
public interface DataConsumerListener {
    /**
     * Called when a Fixture is consumed.
     *
     * @param event The event.
     */
    void dataConsumer(DataConsumerEvent event);
}
