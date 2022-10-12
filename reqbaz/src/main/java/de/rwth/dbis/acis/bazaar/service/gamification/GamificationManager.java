package de.rwth.dbis.acis.bazaar.service.gamification;

import java.io.IOException;
import java.util.*;

import i5.las2peer.logging.L2pLogger;
import org.apache.commons.lang3.StringUtils;

public class GamificationManager {

    private final L2pLogger logger = L2pLogger.getInstance(GamificationManager.class.getName());

    private final GamificationFrameworkClient gfClient;
    private final String gfGameId;

    /**
     * Notifications returned by the framework during request processing.
     *
     * Can be returned to the client by querying
     */
    private Map<Integer, List<GFNotification>> notificationCache = new HashMap<>();

    public GamificationManager(String gfGameServiceUrl, String gfVisualizationServiceUrl, String gfUsername, String gfPassword, String gfGameId) {
        this.gfGameId = gfGameId;

        if (StringUtils.isAnyBlank(gfGameServiceUrl, gfVisualizationServiceUrl, gfUsername, gfPassword, gfGameId)) {
            logger.warning("Gamification functionality cannot be used without GamificationFramework credentials!");
            gfClient = null;
        } else {
            gfClient = new GamificationFrameworkClient(gfGameServiceUrl, gfVisualizationServiceUrl, gfUsername, gfPassword);
        }
    }

    public void initializeUser(Integer userId) {
        if (!isAvailable()) {
            logger.warning("Cannot add user to Gamification Framework. Gamification is not configured");
            return;
        }

        try {
            gfClient.addMemberToGame(gfGameId, userId.toString());
        } catch (IOException e) {
            logger.warning("Failed to add user to gamification framework: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void triggerCreateRequirementAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.CREATE_REQUIREMENT, userId);
    }

    private void triggerActionIfGamificationAvailable(String actionId, Integer userId) {
        if (!isAvailable()) {
            logger.warning("Cannot trigger action " + actionId + ". Gamification is not configured");
            return;
        }

        try {
            List<GFNotification> notifications = gfClient.triggerAction(gfGameId, actionId, userId.toString());
            storeUserNotifications(userId, notifications);
        } catch (IOException e) {
            logger.warning("Failed to trigger action " + actionId + " for user " + userId);
            e.printStackTrace();
        }
    }

    public List<GFNotification> getUserNotifications(Integer userId) {
        if (notificationCache.containsKey(userId)) {
            return Collections.unmodifiableList(notificationCache.get(userId));
        }
        return Collections.emptyList();
    }

    private void storeUserNotifications(Integer userId, List<GFNotification> notifications) {
        if (notificationCache.containsKey(userId)) {
            notificationCache.get(userId).addAll(notifications);
        } else {
            notificationCache.put(userId, new ArrayList<>(notifications));
        }
    }

    private boolean isAvailable() {
        return gfClient != null;
    }

    private static class Actions {
        private static final String CREATE_REQUIREMENT = "create_req";
    }
}
