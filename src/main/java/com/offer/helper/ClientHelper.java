package com.offer.helper;

import com.offer.TamaleClient;
import com.offer.dto.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by thangnguyen on 25/05/16.
 */
public class ClientHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ClientHelper.class);
    /**
     * Return client by server's token
     *
     * @param server
     * @param clients
     * @return
     */
    public static TamaleClient getActiveClient(Server server, HashMap<String, TamaleClient> clients) {
        return clients.get(server.getToken());
    }

    public static Server setTamaleConnection(String stream, Environment environment) throws IOException {
        // Load the properties file and blend it into the System properties.

        System.setProperty("tamale.client.rabbitmq.queue.request", environment.getProperty("tamale.client.rabbitmq.queue.request"));
        System.setProperty("tamale.client.rabbitmq.queue.timeout", environment.getProperty("tamale.client.rabbitmq.queue.timeout"));
        System.setProperty("tamale.client.rabbitmq.autoAck", environment.getProperty("tamale.client.rabbitmq.autoAck"));


        Server server = new Server();
        server.setName(environment.getProperty("tamale.client.rabbitmq.name"));
        server.setHost(environment.getProperty("tamale.client.rabbitmq.host"));
        server.setPort(environment.getProperty("tamale.client.rabbitmq.port"));
        server.setUser(environment.getProperty("tamale.client.rabbitmq.user"));
        server.setPassword(environment.getProperty("tamale.client.rabbitmq.password"));
        server.setToken(environment.getProperty("tamale.client.rabbitmq.token"));
        server.setStream(stream);
        return server;
    }

    public static void setGsiApiProperties(Environment environment) {
        System.setProperty("gsi.api.bonus.mesh-base-url", environment.getProperty("gsi.api.bonus.mesh-base-url"));
        System.setProperty("gsi.api.bonus.mesh-authentication-key", environment.getProperty("gsi.api.bonus.mesh-authentication-key"));
        System.setProperty("gsi.api.bonus.mesh-authentication-value", environment.getProperty("gsi.api.bonus.mesh-authentication-value"));
        System.setProperty("gsi.api.bonus.bonus-suffix", environment.getProperty("gsi.api.bonus.bonus-suffix"));
        System.setProperty("gsi.api.bonus.bonus-manual-award-suffix", environment.getProperty("gsi.api.bonus.bonus-manual-award-suffix"));
        System.setProperty("gsi.api.bonus.bonus-auto-award-suffix", environment.getProperty("gsi.api.bonus.bonus-auto-award-suffix"));
        System.setProperty("mailman.authentication.url", environment.getProperty("mailman.authentication.url"));
        System.setProperty("mailman.authentication.client_id", environment.getProperty("mailman.authentication.client_id"));
        System.setProperty("mailman.authentication.client_secret", environment.getProperty("mailman.authentication.client_secret"));
        System.setProperty("mailman.authentication.grant_type", environment.getProperty("mailman.authentication.grant_type"));
        System.setProperty("mailman.authentication.redis_auth_key", environment.getProperty("mailman.authentication.redis_auth_key"));
        System.setProperty("mailman.authentication.redis.host", environment.getProperty("mailman.authentication.redis.host"));
        System.setProperty("mailman.notification.url", environment.getProperty("mailman.notification.url"));

    }
}
