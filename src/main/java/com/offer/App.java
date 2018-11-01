package com.offer;

import com.offer.dto.Server;
import com.offer.helper.ClientHelper;
import com.offer.helper.Logger;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashMap;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
public class App
{
    //we use the token for the hashmap's keys
    public static HashMap<String,TamaleClient> clients = new HashMap<>();
    public static TamaleClient client;
    public static ApplicationContext applicationContext;


    public static void main( String[] args )
    {
        ApplicationContext appContext = SpringApplication.run(App.class, args);
        applicationContext = appContext;
        ClientHelper.setGsiApiProperties(appContext.getEnvironment());
        App.setUpTamaleConnectionByStream("transaction", appContext);
        App.setUpTamaleConnectionByStream("login", appContext);
        App.setUpTamaleConnectionByStream("register", appContext);
    }

    private static void setUpTamaleConnectionByStream(String stream, ApplicationContext applicationContext) {
        try {
        Server server = ClientHelper.setTamaleConnection(stream, applicationContext.getEnvironment());

        TamaleClient client = new TamaleClient(server);


        AutowireCapableBeanFactory factory = applicationContext.getAutowireCapableBeanFactory();
        factory.autowireBean( client );
        factory.initializeBean( client, "TamaleClient" );
        Thread clientThread = new Thread(client, TamaleClient.class.toString());
        clientThread.start();

        //and start the session
        client.initiateSession(server.getToken(), server.getStream());
        App.clients.put(server.getToken(),client);
        } catch (Exception e) {
            //something is wrong
            //we return an error code
            Logger.log("Error setting up Tamale connection: " + e.toString(), Logger.LogType.ERROR);
        }
    }

    /**
     * Whenever tamale restarts the queues are lost so we need to re-establish session with tamale to start
     * receiving data again
     *
     * @param stream
     */
    public static void reinitiateTamaleConnection(String stream) {
        App.setUpTamaleConnectionByStream(stream, applicationContext);

    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(3);
        executor.setQueueCapacity(5000);
        executor.setThreadNamePrefix("Notification-");
        executor.initialize();
        return executor;
    }
}
