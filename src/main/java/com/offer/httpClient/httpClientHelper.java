package com.offer.httpClient;

import com.google.gson.*;
import com.offer.model.Offer;
import com.offer.model.bonusRequest.BonusPlayer;
import com.offer.model.reward.FreeSpinReward;
import com.offer.model.reward.Reward;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import com.offer.helper.Logger;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class httpClientHelper {

    //SUN-7606 Hard coded as 7 days from the time bonus is awarded to player
    private static final int freeSpinExpiryDays = 7;

    private static final String offerValidUtilUTC = "2999-01-01T23:59:59Z";

    private static final int MIN_FAILURE_ERROR_CODE  = 300;



    /**
     *
     * @param bonusType
     * @param offerCode
     * @param reward
     * @return
     * @throws Exception
     */
    public static boolean saveBonus(String bonusType, String offerCode, Reward reward) throws Exception {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost;
        JSONObject payload =  new JSONObject();
        String baseUrl = System.getProperty("gsi.api.bonus.mesh-base-url");
        String apiKey = System.getProperty("gsi.api.bonus.mesh-authentication-key");
        String apiValue = System.getProperty("gsi.api.bonus.mesh-authentication-value");

        Logger.log("Saving bonus with bonus id: {" + offerCode + "} to game provider system", Logger.LogType.INFO);

        if(Objects.equals(bonusType, Offer.RewardType.FREESPINS.toString())) {

            String url = baseUrl + "rgs/" + reward.getProviderName() + "/bonus";
            httpPost = new HttpPost(url);

            String bonusTypeStr = "freeRounds";
            payload.put("type", bonusTypeStr);
            //We pass offer code as bonus ID, as this value will be used to display on statement
            payload.put("bonusId", offerCode + System.getProperty("gsi.api.bonus.bonus-suffix"));
            payload.put("rgsCode", reward.getProviderName());
            payload.put("stakeLevel", ((FreeSpinReward)reward).getCoinLevel());
            payload.put("numFreeRounds", ((FreeSpinReward)reward).getNumOfFreeSpins());
            //SUN-7606 Hard coded as 7 days from the time bonus is awarded to player
            payload.put("awardValidityInDays", freeSpinExpiryDays);
            //Hard coded as we are not expecting this field to be mandatory, but currently if we don't provide it award bonus will fail
            payload.put("validUntilUTC", offerValidUtilUTC);
            payload.put("rgsGameIds", reward.getGameIds());
        } else {
            throw new Exception("Bonus type: " + bonusType + " is not supported");
        }


        httpPost.setEntity(new StringEntity(payload.toString()));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader(apiKey, apiValue);


        CloseableHttpResponse response = client.execute(httpPost);
        return (response.getStatusLine().getStatusCode() == 200);
    }


    /**
     *
     *
     * @param rgsCode rgsCode is provider name
     * @param offerCode
     * @param bonusAwardId
     * @param players
     * @return
     * @throws IOException
     * @throws AuthenticationException
     */
    public static boolean awardBonus(String rgsCode, String offerCode, String bonusAwardId, ArrayList<BonusPlayer> players)  throws IOException, AuthenticationException {

        String bonusId = offerCode + System.getProperty("gsi.api.bonus.bonus-suffix");

        CloseableHttpClient client = HttpClients.createDefault();
        String baseUrl = System.getProperty("gsi.api.bonus.mesh-base-url");
        String apiKey = System.getProperty("gsi.api.bonus.mesh-authentication-key");
        String apiValue = System.getProperty("gsi.api.bonus.mesh-authentication-value");
        String url = baseUrl + "rgs/" + rgsCode + "/bonus/" + bonusId + "/award";
        HttpPost httpPost = new HttpPost(url);

        JSONObject payload =  new JSONObject();

        Gson objGson = new GsonBuilder().create();
        JsonElement playersJson = objGson.toJsonTree(players);


        //We use offer code to fill the bonusId value in API call, as this value will be used to display in the customer statement
        payload.put("bonusId", bonusId);
        payload.put("bonusAwardId", bonusAwardId);
        payload.put("players", playersJson);

        httpPost.setEntity(new StringEntity(payload.toString()));
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader(apiKey, apiValue);


        CloseableHttpResponse response = client.execute(httpPost);
        return (response.getStatusLine().getStatusCode() == 200);
    }

    /**
     * HTTP request to offer status for a customer
     *
     * @param customerId
     * @return
     * @throws IOException
     * @throws AuthenticationException
     */
    public static JsonArray awardStatus(long customerId) throws IOException, AuthenticationException {
        String apiKey = System.getProperty("gsi.api.bonus.mesh-authentication-key");
        String apiValue = System.getProperty("gsi.api.bonus.mesh-authentication-value");
        String baseUrl = System.getProperty("gsi.api.bonus.mesh-base-url");
        String url = baseUrl + "rgs/player/" + customerId + "/bonus/status";

        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader("Content-Type", "application/json");
        httpGet.setHeader(apiKey, apiValue);
        CloseableHttpResponse response = client.execute(httpGet);
        HttpEntity entity = response.getEntity();
        StatusLine statusLine = response.getStatusLine();

        if (statusLine.getStatusCode() >= MIN_FAILURE_ERROR_CODE)
            throw new HttpResponseException(statusLine.getStatusCode(), statusLine.getReasonPhrase());

        if (entity == null)
            throw new ClientProtocolException("Response contains no content");

        //Extract accessToken from httpResponse
        String resp_body = EntityUtils.toString(response.getEntity());
        Gson objGson = new GsonBuilder().create();
        JsonElement jsonElement = objGson.fromJson(resp_body, JsonElement.class);
        JsonArray jsonArray = jsonElement.getAsJsonArray();

        return jsonArray;

    }
}
