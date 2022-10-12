package de.rwth.dbis.acis.bazaar.service.gamification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import i5.las2peer.logging.L2pLogger;
import okhttp3.*;
import org.apache.commons.lang3.Validate;

public class GamificationFrameworkClient {

    private final L2pLogger logger = L2pLogger.getInstance(GamificationFrameworkClient.class.getName());

    private final HttpUrl gfGameServiceUrl;
    private final HttpUrl gfVisualizationServiceUrl;

    private final OkHttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GamificationFrameworkClient(String gfGameServiceUrl, String gfVisualizationServiceUrl, String gfUsername, String gfPassword) {
        this.gfGameServiceUrl = HttpUrl.parse(gfGameServiceUrl);
        this.gfVisualizationServiceUrl = HttpUrl.parse(gfVisualizationServiceUrl);

        httpClient = new OkHttpClient.Builder()
                .authenticator((route, response) -> {
                    if (response.request().header("Authorization") != null) {
                        return null;
                    }
                    String credential = Credentials.basic(gfUsername, gfPassword);
                    return response.request().newBuilder()
                            .header("Authorization", credential)
                            .build();
                })
                .build();
    }

    public void addMemberToGame(String gameId, String memberUsername) throws IOException {
        Validate.notBlank(gameId);
        Validate.notBlank(memberUsername);

        Request request = new Request.Builder()
                .url(gfGameServiceUrl.newBuilder()
                        .addPathSegment("data")
                        .addPathSegment(gameId)
                        .addPathSegment(memberUsername)
                        .build()
                )
                .method("POST", null)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            logger.info("Added member " + memberUsername + " to game " + gameId + " (response: " + response.body().string() + ")");
        }
    }

    public List<GFNotification> triggerAction(String gameId, String actionId, String username) throws IOException {
        Validate.notBlank(gameId);
        Validate.notBlank(actionId);
        Validate.notBlank(username);

        Request request = new Request.Builder()
                .url(gfVisualizationServiceUrl.newBuilder()
                        .addPathSegment("actions")
                        .addPathSegment(gameId)
                        .addPathSegment(actionId)
                        .addPathSegment(username)
                        .build()
                )
                .method("POST", null)
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String rawResponse = response.body().string();
            logger.info("Triggered action " + actionId + " on game " + gameId + " for user " + username + " (response: " + rawResponse + ")");

            final MapType mapType = objectMapper.getTypeFactory().constructMapType(
                    Map.class, String.class, Object.class);
            Map<String, Object> responseJson = objectMapper.readValue(rawResponse, mapType);

            // process notifications
            List<GFNotification> notifications = new ArrayList<>();
            if (responseJson.containsKey("notification")) {
                Object notificationRoot = responseJson.get("notification");
                if (notificationRoot instanceof List) {
                    ((List<?>) notificationRoot).forEach(notificationJson -> {
                        notifications.add(new GFNotification((Map<String, Object>) notificationJson));
                    });
                } else if (notificationRoot instanceof Map) {
                    notifications.add(new GFNotification((Map<String, Object>) notificationRoot));
                } else {
                    logger.warning("Unexpected value of 'notification' object: " + notificationRoot);
                }
            }
            return notifications;
        }
    }
}
