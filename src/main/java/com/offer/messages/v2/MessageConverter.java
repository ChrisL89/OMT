package com.offer.messages.v2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.offer.exceptions.InvalidMessageTypeException;
import com.offer.exceptions.MissingHeaderException;
import com.offer.messages.v2.responses.AbstractResponse;
import com.rabbitmq.client.LongString;
import com.rabbitmq.client.QueueingConsumer;
import com.offer.helper.Logger;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by erwan on 1/10/15.
 *
 * Converts the rabbitMq messages to POJOs
 */
public class MessageConverter {


    private ObjectMapper mapper;

    public MessageConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Convert a RabbitMQ Message to a Response object.
     *
     * @param delivery  The RabbitMQ delivery message
     * @return  A Response object.
     * @throws MissingHeaderException
     * @throws InvalidMessageTypeException
     * @throws IOException      When the JSON is invalid
     */
    public AbstractResponse convertResponse(QueueingConsumer.Delivery delivery) throws MissingHeaderException, InvalidMessageTypeException, IOException {

        Map<String, Object> headers = delivery.getProperties().getHeaders();

	    if (headers == null) {
		    throw new MissingHeaderException("No headers");
	    }

	    Object messageTypeObj = headers.get(Message.MESSAGE_TYPE_HEADER);

		if (messageTypeObj == null) {
			throw new MissingHeaderException("Missing message_type header.");
		}


        String messageTypeString = new String(((LongString)messageTypeObj).getBytes(), Charset.forName("UTF-8"));

        if (messageTypeString.length() == 0) {
            throw new MissingHeaderException("Missing message_type header.");
        }

        try {
            // Get the class name for the response from the message_type header.
            MessageType messageTypeEnum = MessageType.valueOf(messageTypeString.toUpperCase());
            Class<?> responseClass = messageTypeEnum.getClassName();
            // Instantiate the class
            Constructor<?> constructor = responseClass.getConstructor(MessageType.class);
            AbstractResponse response = (AbstractResponse) constructor.newInstance(messageTypeEnum);
            // And load it with the JSON.
            String body = new String(delivery.getBody(), Charset.forName("UTF-8"));
            mapper.readerForUpdating(response).readValue(body);
            return response;
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            Logger.log("Error creating response object from message_type {" + e + "}", Logger.LogType.ERROR);
            // Rethrow.
            throw new InvalidMessageTypeException("Could not create responce from " + messageTypeString, e);
        }

    }

    public ObjectMapper getMapper() {
        return mapper;
    }
}
