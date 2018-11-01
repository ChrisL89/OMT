package com.offer.consumers;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by erwan on 31/10/15.
 *
 *
 */
public class DataConsumerProducer {

    protected List<DataConsumerListener> dataConsumedListenerList;

    public DataConsumerProducer() {
        this.dataConsumedListenerList = new LinkedList<>();
    }

    public boolean addDataConsumedListener(DataConsumerListener dataConsumerListener) {
        return this.dataConsumedListenerList.add(dataConsumerListener);
    }

    public boolean removeDataConsumedListener(DataConsumerListener dataConsumerListener) {
        return this.dataConsumedListenerList.remove(dataConsumerListener);
    }

    public void fireDataConsumedEvent(DataConsumerEvent event) {
        dataConsumedListenerList.stream().forEach(c -> c.dataConsumer(event));
    }

}
