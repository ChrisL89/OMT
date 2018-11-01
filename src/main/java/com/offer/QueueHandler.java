package com.offer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offer.consumers.DataConsumerEvent;
import com.offer.consumers.DataConsumerProducer;
import com.offer.controller.ProcessMessageController;
import com.rabbitmq.client.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;
import com.offer.helper.Logger;

/**
 * Created by erwan on 6/10/15.
 *
 * This will handle the reading of messages from a rabbitmq queue.
 */
@Component
@Scope("prototype")
public class QueueHandler extends DataConsumerProducer implements Runnable {
    private static int counter = 0;
    public static int getNextCounter() {
        return ++counter;
    }
    private int maxConnectionAttempts = 5;
    private int connectionAttempts = 0;

    private ArrayList<String> packets = new ArrayList<>();

    protected String queueName;
    protected Connection connection;
    protected Channel channel;
    protected QueueingConsumer consumer;
    protected long queueTimeout;
    protected boolean autoAck = false;
    protected ConnectionFactory connectionFactory;
    protected TamaleClient client;

    private boolean keepHandling = true;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ProcessMessageController processMessageController;

    /**
     *
     * @param connectionFactory     Rabbitmq connection details.
     * @param queueName         The queue name to consume
     * @param autoAck           True to set autoAck on consumer.
     * @throws Exception    On settings error.
     * @throws IOException on rabbitmq connection error.
     */
    public QueueHandler(ConnectionFactory connectionFactory, String queueName, boolean autoAck, TamaleClient client) throws Exception {
        this.queueName = queueName;
        this.autoAck = autoAck;
        this.client = client;
        try {
            queueTimeout = Long.parseLong(System.getProperty("tamale.client.rabbitmq.queue.timeout", "5000"));
        } catch (NumberFormatException e) {
            Logger.log("queue timeout value is invalid...", Logger.LogType.ERROR);
            throw new Exception("QueueTimeout value is invalid", e);
        }
        this.connectionFactory = connectionFactory;
        Logger.log("Connecting to queue {" + queueName + "}", Logger.LogType.INFO);
        connectQueue();
    }

    private void connectQueue() {
        try{
            Logger.log("Connecting to queue {" + queueName + "} from connectQueue", Logger.LogType.INFO);
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
            consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, autoAck, consumer);
            Logger.log("Connected after " + connectionAttempts + " attempts", Logger.LogType.INFO);
            // reset attempts to 0 once connected
            connectionAttempts = 0;
        } catch (IOException e) {
            Logger.log("Attempting Reconnection attempt: " + connectionAttempts, Logger.LogType.INFO);
            connectionAttempts++;
            if(connectionAttempts < maxConnectionAttempts) {
                connectQueue();
            } else {
                stopHandling();

            }
        }

    }

    @Override
    public void run() {

        while (keepHandling) {
            try {
                handleQueue();
            } catch (InterruptedException e) {
                Logger.log("Interrupted...Reattempting", Logger.LogType.ERROR);
                connectQueue();
            } catch (IOException e) {
                Logger.log("IOException : {" + e + "} ReAttempting", Logger.LogType.ERROR);
                connectQueue();
            } catch (ShutdownSignalException e) {
                Logger.log("ShtudownException : {" + e + "} ReAttempting", Logger.LogType.ERROR);
                Logger.log("Hard Error:" + e.isHardError(), Logger.LogType.ERROR);
                Logger.log("Application initiated" + e.isInitiatedByApplication(), Logger.LogType.ERROR);
                connectQueue();
            } catch (ConsumerCancelledException e) {
                Logger.log("Looks like tamale restarted connecting in case queue exists", Logger.LogType.ERROR);
                connectQueue();
            } catch (Exception e) {
                Logger.log("Exception Occured: " + e.getMessage(), Logger.LogType.ERROR);
                throw e;
            }
        }

        Logger.log("Closing RabbitMQ connections", Logger.LogType.INFO);
        try {
            close();
        } catch (Exception e){
            Logger.log("Exception: " + e.getMessage(), Logger.LogType.WARN);
        }
        Logger.log("Finished Queue Handling for {" + queueName + "}", Logger.LogType.INFO);
        App.reinitiateTamaleConnection(this.client.stream);
    }

    /**
     * Handle the queue, which means, just poll and log the packet received.
     * This will also call a hook method that can be overwritten by child classes.
     *
     * @throws InterruptedException when listener is interrupted
     */
    private void handleQueue() throws InterruptedException, IOException {
        QueueingConsumer.Delivery delivery = consumer.nextDelivery(queueTimeout);

        if (delivery == null) {
            Logger.log("nextDelivery timeout", Logger.LogType.DEBUG);
        } else {

            if (autoAck) {
                handlePacket(delivery);
            } else {
                if (handlePacket(delivery)) {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } else {
                    // NACK with no requeue.
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                }
            }
        }
    }

    public ArrayList<String> getPackets() {
        return this.packets;
    }

    public void emptyPackets(){
        this.packets.clear();
    }

    /**
     * Do something with the Delivery. In this case, we are just logging the body content.
     *
     * @param delivery  The RabbitMQ Message in all its glory
     *
     * @return  True if the delivery should be acknowledged. false otherwise...
     */
    protected boolean handlePacket(QueueingConsumer.Delivery delivery)  {
        String packet = new String(delivery.getBody(), Charset.forName("UTF-8"));
        Logger.log("{" + delivery.getEnvelope().getDeliveryTag() + "} - Received {" + packet + "} from queue {" + queueName + "}", Logger.LogType.INFO);

        packets.add(packet);

        // Transform the packet to a Fixture object - But only if it is a WFD Transaction message.
	    // We could be receiving a start_snapshot_response, a tamale v2 message.
        try {
            fireDataConsumedEvent(new DataConsumerEvent(packet));
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(packet);
            processMessageController.processMessage(jsonObject);
        } catch (IllegalArgumentException e) {
            Logger.log("Exception : {" + e + "}", Logger.LogType.ERROR);
        } catch (NullPointerException e) {
	        // This isn't a WFD Transaction message, but we still want to pass it on.
            Logger.log("Not a Transaction Message...", Logger.LogType.ERROR);
        } catch (ParseException e) {
            Logger.log("Not able to parse packet to Json object, exception : {" + e + "}", Logger.LogType.ERROR);
        } catch (Exception e) {
            Logger.log("Generic exception: {" + e + "}", Logger.LogType.ERROR);
        }

        //Always return true as message has been acknowledged even we could not process the message
        return true;
    }

    /**
     * Close the connections
     */
    public void close() throws TimeoutException {
        try {
            channel.close();
            connection.close();
        } catch (IOException e) {
            Logger.log("Exception when closing rabbitmq connection : {" + e + "}", Logger.LogType.WARN);
        }
    }

    /**
     * STOP
     */
    public void stopHandling() {
        Logger.log("Stopping Handler...", Logger.LogType.DEBUG);
        keepHandling = false;
    }

    /**
     * @return The currently connected queue
     */
    public String getQueueName() {
        return queueName;
    }

    public boolean isHandling() {
        return keepHandling;
    }

}
