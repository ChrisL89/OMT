package com.offer;

import com.offer.exceptions.InvalidMessageTypeException;
import com.offer.exceptions.MissingHeaderException;
import com.offer.messages.v2.MessageType;
import com.offer.messages.v2.responses.AbstractResponse;
import com.offer.messages.v2.responses.StartSnapshotResponse;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.offer.helper.Logger;

import java.io.IOException;

/**
 * Created by erwan on 16/10/15.
 *
 */
public class DeltaQueueHandler extends QueueHandler {

    /**
     * @param connectionFactory Rabbitmq connection details.
     * @param queueName         The queue name to consume
     * @throws Exception   On settings error.
     */
    public DeltaQueueHandler(ConnectionFactory connectionFactory, String queueName, TamaleClient client, boolean autoAck) throws Exception {
        super(connectionFactory, queueName, autoAck, client);
    }

    @Override
    protected boolean handlePacket(QueueingConsumer.Delivery delivery) {
        if (!super.handlePacket(delivery)) {
            Logger.log("Parent handlePacket returned false.", Logger.LogType.WARN);
            return false;
        }

        try {
            AbstractResponse response = TamaleClient.messageConverter.convertResponse(delivery);
            if (response.getMessageType().equals(MessageType.START_SNAPSHOT_RESPONSE)) {
                String snapshotQueue = ((StartSnapshotResponse)response).getSnapshotQueue();
                Logger.log("Received StartSnapshot Response. Queue : {" + snapshotQueue + "}", Logger.LogType.INFO);
            } else {
                Logger.log("Received a Tamale Response message : {" + response.getMessageType().toString() + "}", Logger.LogType.INFO);
            }
            return true;
        } catch (MissingHeaderException e) {
            // This is not a valid Tamale message, so ignore this exception.
            return true;
        } catch (InvalidMessageTypeException | IOException e) {
            Logger.log("Exception while trying to parse a Tamale Response : {" + e + "}", Logger.LogType.ERROR);
            return false;    // We still return true?
        }

    }
}
