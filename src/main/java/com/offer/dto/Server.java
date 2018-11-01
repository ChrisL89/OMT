package com.offer.dto;

import org.springframework.beans.factory.annotation.Value;

/**
 * Created by thangnguyen on 24/05/16.
 */
public class Server {
    @Value("${tamale.client.rabbitmq.name}")
    String name;
    String host;
    String port;
    String user;
    String password;
    String token;
    String stream;

    public Server() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }
}
