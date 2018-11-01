package com.offer.listeners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.offer.events.OfferEvents;
import com.offer.model.Offer;
import com.offer.model.Player;
import com.offer.model.reward.FreeSpinReward;
import com.offer.model.reward.Reward;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import com.offer.helper.Logger;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;
import java.util.*;

@Component
public class OfferEventListener implements ApplicationListener<OfferEvents> {
    /**
     * It will be something like
     * {
         "to": "14713",
         "uuid": "32342343-0000-4000-A000-000000000000",
             "content": {
             "type": "notification",
             "status": "AVAILABLE",
                 "args": {
                     "depositAmount": "$50.00",
                     "freeSpinNumber": 50,
                     "gameName": "serious snow",
                     "gameCode": "bbw"
                 }
            }
        }
     */
    private JSONObject payload;



    @Override
    @Async
    public void onApplicationEvent(OfferEvents event) {
        Logger.log("Received offer event with event message type: {"+ event.getEventMessageType().toString() + "}", Logger.LogType.INFO);
        if (setPayload(event)) {
            sendNotification();
        } else {
            Logger.log("Not able to set notification payload", Logger.LogType.WARN);
        }

    }

    public JSONObject getPayload() {
        return payload;
    }

    public boolean setPayload(OfferEvents event) {
        Offer offer = event.getData();
        Reward reward = offer.getReward();
        String notificationUrl = System.getProperty("mailman.notification.url");
        if(reward instanceof FreeSpinReward) {
            //Use event message type to determine how to create payload
            if(event.getEventMessageType() == OfferEvents.EventMessageType.AVAILABLE) {
                //Available notification should only be for Deposit Offer
                Player player = event.getPlayer();
                JSONObject args =  new JSONObject();
                JSONObject href = new JSONObject();
                String uuid = UUID.randomUUID().toString();
                //If single game in reward, it will be game name, if more than 1 game, show generic game name
                Boolean multiGames = (reward.getAllowAllGames() || reward.getGames().size() > 1);
                String offerObject = multiGames ? ((reward.getAllowAllGames() ? "all " : "selected ") + reward.getProviderNameText() + " game(s)")  : reward.getGames().get(0).getGameName();

                this.payload = new JSONObject();
                args.put("minDepositAmount", offer.getMinDepositText());
                args.put("maxDepositAmount", offer.getMaxDepositText());
                args.put("freeSpinNumber", ((FreeSpinReward) reward).getNumOfFreeSpins());
                args.put("offerObject", offerObject);
                args.put("startTime", offer.getStartDate().getTime());
                args.put("endTime", offer.getEndDate().getTime());
                href.put("delete", notificationUrl + uuid);

                JSONObject content =  new JSONObject();
                content.put("type", "notification");
                content.put("status", "AVAILABLE");
                content.put("args", args);
                content.put("href", href);
                content.put("timestamp", System.currentTimeMillis());

                this.payload.put("to", player.getCustomerId().toString());
                this.payload.put("uuid", uuid);
                this.payload.put("content", content);
            } else if(event.getEventMessageType() == OfferEvents.EventMessageType.ACTIVATED) {

                JSONObject args = new JSONObject();
                JSONObject href = new JSONObject();
                String uuid = UUID.randomUUID().toString();
                //If single game in reward, it will be game name, if more than 1 game, show generic game name
                Boolean multiGames = (reward.getAllowAllGames() || reward.getGames().size() > 1);
                String offerObject = multiGames ? ((reward.getAllowAllGames() ? "all " : "selected ") + reward.getProviderNameText() + " game(s)")  : reward.getGames().get(0).getGameName();

                this.payload = new JSONObject();
                args.put("freeSpinNumber", (((FreeSpinReward) reward).getNumOfFreeSpins()));
                args.put("offerObject", offerObject);
                if(multiGames) {
                    args.put("offerId", offer.getOffer_id().toString());
                } else {
                    args.put("gameCode", reward.getGames().get(0).getGameId());
                }
                href.put("delete", notificationUrl + uuid);

                JSONObject content = new JSONObject();
                String status = multiGames ? "ACTIVATED_MULTIPLE" : "ACTIVATED";
                content.put("type", "notification");
                content.put("status", status);
                content.put("args", args);
                content.put("href", href);
                content.put("timestamp", System.currentTimeMillis());

                this.payload.put("to", event.getCustomerId().toString());
                this.payload.put("uuid", uuid);
                this.payload.put("content", content);
            } else if(event.getEventMessageType() == OfferEvents.EventMessageType.FAILED) {

                JSONObject args = new JSONObject();
                JSONObject href = new JSONObject();
                String uuid = UUID.randomUUID().toString();

                this.payload = new JSONObject();
                args.put("offerCode", offer.getOfferCode());
                href.put("delete", notificationUrl + uuid);

                JSONObject content = new JSONObject();
                content.put("type", "notification");
                content.put("status", "FAILED");
                content.put("args", args);
                content.put("href", href);
                content.put("timestamp", System.currentTimeMillis());

                this.payload.put("to", event.getCustomerId().toString());
                this.payload.put("uuid", uuid);
                this.payload.put("content", content);
            }
            return true;
        } else {
            Logger.log("Failed to process award call back request, reward with offer id {" + offer.getOffer_id() + "} not supported", Logger.LogType.INFO);
            return false;
        }
    }

    private void sendNotification() {


        String accessToken = getAuthenticationKey();
        Logger.log("append token: {" + accessToken + "}", Logger.LogType.INFO);

        Logger.log("Notification sending {" + payload.toJSONString() + "}", Logger.LogType.INFO);
        String url = System.getProperty("mailman.notification.url");
        try {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Authorization", "Bearer " + accessToken);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(payload.toString(), "UTF-8"));
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(httpPost);
            Logger.log("Notification response: {" + response + "}", Logger.LogType.INFO);
        } catch (Exception e) {
            Logger.log("Unable to publish notification to frontend", Logger.LogType.ERROR);
        }

    }

    /**
     * Call authentication service and return access token
     * @return String token
     */
    private String getAuthenticationKey() {

        String redisAuthKey = System.getProperty("mailman.authentication.redis_auth_key");
        String redisHost = System.getProperty("mailman.authentication.redis.host");
        String accessToken;
        Jedis jedis;

        try {
        //Try to get accessToken from Redis, if error connecting redis, generate access token from auth service directly
        jedis = new Jedis(redisHost, 6379);
        accessToken = jedis.get(redisAuthKey);
        } catch (Exception e) {
            Logger.log("Error connecting to redis to obtain access Token, so obtain from auth service. Exception: {" + e.getMessage() + "}", Logger.LogType.INFO);
            accessToken = generateAccessToken();
            return accessToken;
        }

        //If get auth token from redis, then return directly
        if(!(accessToken == null)) {
            Logger.log("Found access token in Redis: {" + accessToken + "}", Logger.LogType.INFO);
            return accessToken;
        } else {
            accessToken = generateAccessToken();
            //Set accessToken to Redis, since token expire on server in 10800 seconds, and by here token has been generated for some time, so reduce expiry time in our system
            jedis.setex(redisAuthKey, 10500, accessToken);
        }

        return accessToken;
    }


    private String generateAccessToken() {

        String url = System.getProperty("mailman.authentication.url");
        String clientId = System.getProperty("mailman.authentication.client_id");
        String clientSecret = System.getProperty("mailman.authentication.client_secret");
        String grantType = System.getProperty("mailman.authentication.grant_type");
        String accessToken;

        try {
            HttpPost httpPost = new HttpPost(url);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("client_id", clientId));
            params.add(new BasicNameValuePair("client_secret", clientSecret));
            params.add(new BasicNameValuePair("grant_type", grantType));
            httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
            //Execute and get response
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(httpPost);
            //Extract accessToken from httpResponse
            String resp_body = EntityUtils.toString(response.getEntity());
            Gson objGson = new GsonBuilder().create();
            JsonElement jsonElement = objGson.fromJson(resp_body, JsonElement.class);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            accessToken = jsonObject.get("access_token").getAsString();

            Logger.log("Access token generated by auth service: {" + accessToken + "}", Logger.LogType.INFO);

            return accessToken;

        } catch (Exception e) {
            Logger.log("Unable to get auth accessToken from authentication service", Logger.LogType.ERROR);
        }

        return null;
    }
}

