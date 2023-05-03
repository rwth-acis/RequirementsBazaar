package de.rwth.dbis.acis.bazaar.service.gamification;

import i5.las2peer.logging.L2pLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

public class GamificationManager {

    private final L2pLogger logger = L2pLogger.getInstance(GamificationManager.class.getName());

    private final GamificationFrameworkClient gfClient;
    private final String gfGameId;
    private final String gfNamespace;


    /**
     * Notifications returned by the framework during request processing.
     * <p>
     * Can be returned to the client by querying
     */
    private Map<Integer, List<Map<String, Object>>> notificationCache = new HashMap<>();

    public GamificationManager(String gfBaseUrl, String gfUsername, String gfPassword, String gfGameId, String gfNamespace) {
        this.gfGameId = gfGameId;
        this.gfNamespace = gfNamespace;

        if (StringUtils.isAnyBlank(gfBaseUrl, gfUsername, gfPassword, gfGameId, gfNamespace)) {
            logger.warning("Gamification functionality cannot be used without GamificationFramework credentials!");
            gfClient = null;
        } else {
            gfClient = new GamificationFrameworkClient(gfBaseUrl, gfUsername, gfPassword);
        }
    }

    public void initializeUser(Integer userId, boolean firstLogin) {
        if (!isAvailable()) {
            logger.warning("Cannot add user to Gamification Framework. Gamification is not configured");
            return;
        }

        try {
            gfClient.registerUser(gfGameId, gfNamespace + userId);
            gfClient.addMemberToGame(gfGameId, gfNamespace + userId);
            if (firstLogin) {
                triggerFirstLoginAction((userId));
            }
        } catch (IOException e) {
            logger.warning("Failed to add user to gamification framework: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void triggerCreateRequirementAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.CREATE_REQUIREMENT, userId, gfNamespace + userId);
    }

    public void triggerCompleteRequirementAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.COMPLETE_REQUIREMENT, userId, gfNamespace + userId);
    }

    public void triggerCreateProjectAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.CREATE_PROJECT, userId, gfNamespace + userId);
    }

    public void triggerCreateCommentAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.CREATE_COMMENT, userId, gfNamespace + userId);
    }

    public void triggerVoteAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.VOTE_REQUIREMENT, userId, gfNamespace + userId);
    }

    public void triggerDevelopAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.DEVELOP_REQUIREMENT, userId, gfNamespace + userId);
    }

    public void triggerFollowAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.FOLLOW_REQUIREMENT, userId, gfNamespace + userId);
    }

    public void triggerFirstLoginAction(Integer userId) {
        triggerActionIfGamificationAvailable(Actions.FIRST_LOGIN, userId, gfNamespace + userId);
    }

    private void triggerActionIfGamificationAvailable(String actionId, Integer userId, String gamificationUser) {
        if (!isAvailable()) {
            logger.warning("Cannot trigger action " + actionId + ". Gamification is not configured");
            return;
        }
        try {
            List<Map<String, Object>> notifications = gfClient.triggerAction(gfGameId, actionId, gamificationUser);
            storeUserNotifications(userId, notifications);
        } catch (IOException e) {
            logger.warning("Failed to trigger action " + actionId + " for user " + gamificationUser);
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
            return gfClient.getEarnedBadges(gfGameId, gfNamespace + userId);
        } catch (IOException e) {
            logger.warning("Failed to get badges for user " + gfNamespace + userId);
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public Map<String, Object> getUserStatus(Integer userId) {
        try {
            Map<String, Object> status = gfClient.getMemberStatus(gfGameId, gfNamespace + userId);
            return status;
        } catch (IOException e) {
            logger.warning("Failed to get status for user " + gfNamespace + userId);
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }

    public boolean isAvailable() {
        return gfClient != null;
    }

    private static class Actions {
        private static final String CREATE_REQUIREMENT = "create_req";
        private static final String COMPLETE_REQUIREMENT = "complete_req";
        private static final String VOTE_REQUIREMENT = "vote_req";
        private static final String FOLLOW_REQUIREMENT = "follow_req";
        private static final String DEVELOP_REQUIREMENT = "develop_req";
        private static final String CREATE_PROJECT = "create_proj";
        private static final String CREATE_COMMENT = "create_com";
        private static final String FIRST_LOGIN = "first_login";

    }
}
