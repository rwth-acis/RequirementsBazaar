package de.rwth.dbis.acis.bazaar.service.gamification;

import i5.las2peer.logging.L2pLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class GamificationManager {

    private final L2pLogger logger = L2pLogger.getInstance(GamificationManager.class.getName());

    private final GamificationFrameworkClient gfClient;
    private final String gfGameId;

    /**
     * Notifications returned by the framework during request processing.
     * <p>
     * Can be returned to the client by querying
     */
    private Map<Integer, List<Map<String, Object>>> notificationCache = new HashMap<>();

    public GamificationManager(String gfBaseUrl, String gfUsername, String gfPassword, String gfGameId) {
        this.gfGameId = gfGameId;

        if (StringUtils.isAnyBlank(gfBaseUrl, gfUsername, gfPassword, gfGameId)) {
            logger.warning("Gamification functionality cannot be used without GamificationFramework credentials!");
            gfClient = null;
        } else {
            gfClient = new GamificationFrameworkClient(gfBaseUrl, gfUsername, gfPassword);
        }
    }

    public void initializeUser(Integer userId, String userEmail) {
        if (!isAvailable()) {
            logger.warning("Cannot add user to Gamification Framework. Gamification is not configured");
            return;
        }

        try {
            gfClient.registerUser(gfGameId, userId.toString(), userEmail);
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
            List<Map<String, Object>> notifications = gfClient.triggerAction(gfGameId, actionId, userId.toString());
            storeUserNotifications(userId, notifications);
        } catch (IOException e) {
            logger.warning("Failed to trigger action " + actionId + " for user " + userId);
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getUserNotifications(Integer userId) {
        if (notificationCache.containsKey(userId)) {
            return Collections.unmodifiableList(notificationCache.remove(userId));
        }
        return Collections.emptyList();
    }

    private void storeUserNotifications(Integer userId, List<Map<String, Object>> notifications) {
        if (notificationCache.containsKey(userId)) {
            notificationCache.get(userId).addAll(notifications);
        } else {
            notificationCache.put(userId, new ArrayList<>(notifications));
        }
    }

    public List<Map<String, Object>> getUserBadges(Integer userId) {
        try {
            List<Map<String, Object>> badges = gfClient.getEarnedBadges(gfGameId, userId.toString());
            return badges;
        } catch (IOException e) {
            logger.warning("Failed to get badges for user " + userId);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getUserStatus(Integer userId) {
        try {
            Map<String, Object> status = gfClient.getMemberStatus(gfGameId, userId.toString());
            return status;
        } catch (IOException e) {
            logger.warning("Failed to get status for user " + userId);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public boolean isAvailable() {
        return gfClient != null;
    }

    private static class Actions {
        private static final String CREATE_REQUIREMENT = "create_req";
    }
}
