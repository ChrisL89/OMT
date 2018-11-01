package com.offer.helper;


import org.slf4j.LoggerFactory;

import java.util.UUID;

public class Logger {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);
    private static String request_id = null;

    public enum LogType {
        INFO,
        DEBUG,
        WARN,
        ERROR
    }


    public static void log(String message, LogType logType) {

        if(request_id == null) {
            Long pid = Thread.currentThread().getId();
            request_id = pid + "_" + UUID.randomUUID().toString();
        }
        switch (logType) {
            case INFO:
                LOGGER.info("Thread id:{} - {}", request_id, message);
                break;
            case DEBUG:
                LOGGER.debug("Thread id:{} - {}", request_id, message);
                break;
            case WARN:
                LOGGER.warn("Thread id:{} - {}", request_id, message);
                break;
            case ERROR:
                LOGGER.error("Thread id:{} - {}", request_id, message);
                break;
            default:
                LOGGER.error("Thread id:{} - {}", request_id, message);
                break;
        }

    }
}
