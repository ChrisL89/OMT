package com.offer;

import com.offer.exceptions.InvalidMessageTypeException;
import com.offer.exceptions.MissingHeaderException;
import com.offer.messages.v2.requests.AbstractRequest;
import com.offer.messages.v2.responses.AbstractResponse;
import com.rabbitmq.client.*;
import com.offer.helper.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeoutException;

/**
 * Created by erwan on 5/10/15.
 *
 * The Requester will send requests to tamale and listen on the reply queue for a reply.
 */
public class Requester {

    private Connection connection;
    private Channel requestChannel;
    private String replyQueue;
    private QueueingConsumer requestConsumer;

    public Requester(ConnectionFactory connectionFactory) throws IOException,TimeoutException {
        connection = connectionFactory.newConnection();
    }

    /**
     * Connect to the rabbitmq server and create a temporary response queue.
     * @throws IOException
     */
    private void connect() throws IOException {
        Logger.log("Connecting Requester...", Logger.LogType.INFO);
        requestChannel = connection.createChannel();
        requestConsumer = new QueueingConsumer(requestChannel);
        replyQueue = createResponseQueue();
        requestChannel.basicConsume(replyQueue, false, requestConsumer);
    }

    /**
     * Create and return a temporary queue.
     *
     * @return The created queue name.
     * @throws IOException
     */
    private String createResponseQueue() throws IOException {
        return requestChannel.queueDeclare().getQueue();
    }

    /**
     * Send a Request to Tamale on RabbitMQ
     *
     * @param request   The Request object to send.
     * @throws IOException
     */
    public void sendRequest(AbstractRequest request) throws IOException {
        if (requestChannel == null || !requestChannel.isOpen()) {
            connect();
        }

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(request.getCorrelationId())
//                .contentType("application/json")
                .replyTo(replyQueue)
                .headers(request.getMessageHeaders())
                .build();

        String requestString = TamaleClient.messageConverter.getMapper().writeValueAsString(request);

        Logger.log("Sending Request {" + request.getMessageType().toString() + "} {" + request.getCorrelationId() + "} : {" + requestString + "}", Logger.LogType.INFO);

        requestChannel.basicPublish("", System.getProperty("tamale.client.rabbitmq.queue.request"), props, requestString.getBytes());
        //requestChannel.basicPublish("", "tamale_operations_request_q", props, requestString.getBytes());
    }

    /**
     * Blocking Poll of the response queue for a message matching the correlationId given.
     *
     *
     * @param correlationId The correlationId of the response we are waiting for.
     * @return  The Response message.
     *
     * @throws InterruptedException
     * @throws InvalidMessageTypeException
     * @throws MissingHeaderException
     * @throws IOException
     */
    public AbstractResponse waitForResponse(String correlationId) throws InterruptedException, InvalidMessageTypeException, MissingHeaderException, IOException {
        return waitForResponse(correlationId, true);
    }

    public AbstractResponse waitForResponse(String correlationId, boolean dontWait) throws InterruptedException, InvalidMessageTypeException, MissingHeaderException, IOException {
        while(true) {
            QueueingConsumer.Delivery delivery = requestConsumer.nextDelivery();
            if (delivery == null) {
                Logger.log("nextDelivery time out, retrying...", Logger.LogType.DEBUG);
                if (dontWait) {
                    return null;
                } else {
                    Thread.sleep(2000);
                }
            } else {
                String responseCorrelationId = delivery.getProperties().getCorrelationId();
                String responseString = new String(delivery.getBody(), Charset.forName("UTF-8"));
                long deliveryTag = delivery.getEnvelope().getDeliveryTag();
                Logger.log("Received CorrelationId {" + responseCorrelationId + "}, deliveryTag {" + deliveryTag + "}, packet {" + responseString + "}", Logger.LogType.INFO);

                if (responseCorrelationId == null) {
                    Logger.log("Response does not have a correlation ID for body. Ignoring.", Logger.LogType.WARN);
                    requestChannel.basicNack(deliveryTag, false, false);
                } else if (responseCorrelationId.equals(correlationId)) {
                    requestChannel.basicAck(deliveryTag, false);
                    AbstractResponse response = TamaleClient.messageConverter.convertResponse(delivery);
                    Logger.log("Received Tamale Message Type : {" + response.getMessageType().toString() + "}", Logger.LogType.INFO);
                    return response;
                } else {
                    Logger.log("Message received but not matching correlationId. Ignoring.", Logger.LogType.INFO);
                    requestChannel.basicNack(deliveryTag, false, false);
                }
            }
        }
    }

    /**
     * Close the rabbitmq connection.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        Logger.log("Closing Requester...", Logger.LogType.INFO);
        connection.close();
    }
}
