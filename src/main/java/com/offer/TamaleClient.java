package com.offer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.offer.dto.Server;
import com.offer.consumers.DataConsumerEvent;
import com.offer.consumers.DataConsumerListener;
import com.offer.consumers.DataConsumerProducer;
import com.offer.exceptions.InvalidMessageTypeException;
import com.offer.exceptions.MissingHeaderException;
import com.offer.messages.v2.MessageConverter;
import com.offer.messages.v2.MessageType;
import com.offer.messages.v2.requests.StartSessionRequest;
import com.offer.messages.v2.requests.StopSessionRequest;
import com.offer.messages.v2.responses.AbstractResponse;
import com.offer.messages.v2.responses.StartSessionResponse;
import com.rabbitmq.client.ConnectionFactory;
import com.offer.helper.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

/**
 * Created by erwan on 1/10/15.
 *
 *  The actual client.
 */
public class TamaleClient extends DataConsumerProducer implements Runnable {

    public static final MessageConverter messageConverter = new MessageConverter(new ObjectMapper());

    private Requester requester;
    private final ConnectionFactory connectionFactory;
    private boolean keepRunning = true;

    public DeltaQueueHandler deltaQueueHandler;

    private String deltaQueueName;

    @Autowired
    private ApplicationContext appContext;

    volatile public boolean startSessionFlag = false;
    volatile public boolean runningSessionFlag = false;
    volatile public boolean startSessionFinish = false;
    volatile public boolean sessionStartedSuccesslful = false;

    public String stream;
    public String token = "";
    private boolean autoAck;
    private Server server;

    public TamaleClient(Server server) throws IOException,TimeoutException {
        this.server = server;
        // ConnectionFactory
        connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(server.getHost());
        connectionFactory.setPort(Integer.parseInt(server.getPort()));
        connectionFactory.setUsername(server.getUser());
        connectionFactory.setPassword(server.getPassword());

        // AutoAck
        autoAck = System.getProperty("tamale.client.rabbitmq.autoAck", "false").equals("true");

        logConnectionDetails(connectionFactory);

        requester = new Requester(connectionFactory);
    }

    /**
     * The start of life for the client.
     */
    public void run() {

            while(keepRunning) {
                try {
                    if (startSessionFlag) {
                        startSessionFlag = false;

                        // Send the start session request
                        StartSessionResponse session = startSession(token, stream);

                        // Start the Delta queue handler.
                        handleDeltaQueue(session.getDeltaQueue());
                        runningSessionFlag = true;
                        this.startSessionFinish = true;
                        this.sessionStartedSuccesslful = true;
                    }

                    Thread.sleep(50);

                } catch (JsonProcessingException e) {
                    Logger.log("Json Processing Exception : {" + e + "}", Logger.LogType.ERROR);
                } catch (IOException e) {
                    Logger.log("IOException : {" + e + "}", Logger.LogType.ERROR);
                } catch (InterruptedException e) {
                    Logger.log("Interrupted : {" + e + "}", Logger.LogType.ERROR);
                } catch (MissingHeaderException e) {
                    Logger.log("Missing Header : {" + e + "}", Logger.LogType.ERROR);
                } catch (InvalidMessageTypeException e) {
                    //set the appropriate flags
                    //so that the error can be shown in the front-end
                    this.startSessionFinish = true;
                    this.sessionStartedSuccesslful = false;

                    Logger.log("Invalid Message Type : {" + e + "}", Logger.LogType.ERROR);
                } catch (Exception e) {
                    Logger.log("Exception : {" + e + "}", Logger.LogType.ERROR);
                }
            }

        Logger.log("Client done.", Logger.LogType.INFO);
    }

    /**
     * Start a Session with Tamale.
     *
     * @return  The Start Session Response
     *
     * @throws IOException
     * @throws InterruptedException
     * @throws MissingHeaderException
     * @throws InvalidMessageTypeException
     */
    protected StartSessionResponse startSession(String token, String stream) throws IOException, InterruptedException, MissingHeaderException, InvalidMessageTypeException {
        // Send the startSession Request.
        StartSessionRequest startSessionRequest = new StartSessionRequest();
        startSessionRequest.setStream(stream);
        startSessionRequest.setToken(token);

        requester.sendRequest(startSessionRequest);

        AbstractResponse response = requester.waitForResponse(startSessionRequest.getCorrelationId());

        if (!response.getMessageType().equals(MessageType.START_SESSION_RESPONSE)) {
            throw new InvalidMessageTypeException("Expecting startSessionResponse but got " + response.getMessageType() + " instead");
        } else {
            return (StartSessionResponse) response;
        }

    }


    /**
     * Start Handling the Delta Queue.
     *
     * @param queueName The Delta Queue Name.
     * @throws Exception
     */
    public void handleDeltaQueue(String queueName) throws Exception {
        deltaQueueName = queueName;
        deltaQueueHandler = new DeltaQueueHandler(connectionFactory, queueName, this, autoAck);

        AutowireCapableBeanFactory factory = appContext.getAutowireCapableBeanFactory();
        factory.autowireBean( deltaQueueHandler );
        factory.initializeBean( deltaQueueHandler, "DeltaQueueHandler" );


        deltaQueueHandler.addDataConsumedListener(new ClientDataListener());
        Thread deltaQueueHandlerThread = new Thread(deltaQueueHandler, "DeltaQueueHandler-" + QueueHandler.getNextCounter());
        deltaQueueHandlerThread.start();
    }

    /**
     * Ask the client to initiate a session...
     *
     * @param token The client token
     * @param stream    The stream
     * @return  If a session is going to be initiated.
     */
    public boolean initiateSession(String token, String stream) {

        if (runningSessionFlag) {
            Logger.log("Session Already Running...", Logger.LogType.WARN);
            return false;
        }

        this.token = token;
        this.stream = stream;
        this.startSessionFlag = true;
        this.sessionStartedSuccesslful = false;

        return true;
    }

    /**
     * Tell the client to stop the session using a defined token and stream - for testing only.
     *
     * @param token The token to use,
     * @param stream    The stream to use.
     * @return true if session can be stopped.
     */
    public boolean stopSession(String token, String stream) {
        this.token = token;
        this.stream = stream;
        return stopSession();
    }

    /**
     *
     * @return true if session can be stopped.
     */
    public boolean stopSession() {
        if (!runningSessionFlag) {
            return false;
        }

        // Shutdown the delta queue handler if it is running.
        if (deltaQueueHandler != null && deltaQueueHandler.isHandling()) {
            Logger.log("Stopping DeltaQueueHandler", Logger.LogType.INFO);
            deltaQueueHandler.stopHandling();
        }

        // Send a StopSessionRequest
        StopSessionRequest stopSessionRequest = new StopSessionRequest();
        stopSessionRequest.setStream(stream);
        stopSessionRequest.setToken(token);

        try {
            requester.sendRequest(stopSessionRequest);
        } catch (IOException e) {
            Logger.log("IOException trying to send StopSessionRequest... {" + e + "}", Logger.LogType.ERROR);
        }

        runningSessionFlag = false;

        return true;
    }

    /**
     * Terminate the client. This will send a StopSessionRequest and close all rabbitmq connections.
     */
    public void terminate() {
        keepRunning = false;

        stopSession();

        // Do we need to close rabbit connections?
        try {
            requester.close();
        } catch (IOException e) {
            Logger.log("IOException closing the Requester. {" + e + "}", Logger.LogType.WARN);
        }
    }

    /**
     *
     * @return The currently running queue handler
     */
    public Optional<QueueHandler> getRunningHandler() {
        if (deltaQueueHandler != null && deltaQueueHandler.isHandling()) {
            return Optional.of(deltaQueueHandler);
        } else {
            return Optional.empty();
        }
    }

    private void logConnectionDetails(ConnectionFactory connectionFactory) {
        Logger.log("Connection Details : HOST({" + connectionFactory.getHost() +
                "}) PORT({" + connectionFactory.getPort() + "}) USER({" +
                connectionFactory.getUsername() + "}) PASS({" + connectionFactory.getPassword() +
                "})",
                Logger.LogType.INFO);
    }

	/**
     * Glue between the GUI (if it is loaded) and the QueueHandler.
     */
    private class ClientDataListener implements DataConsumerListener {

        @Override
        public void dataConsumer(DataConsumerEvent event) {
            fireDataConsumedEvent(event);
        }
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }
}
