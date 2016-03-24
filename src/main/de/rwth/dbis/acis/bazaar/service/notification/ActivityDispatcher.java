package de.rwth.dbis.acis.bazaar.service.notification;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.security.Context;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public class ActivityDispatcher {

    private final String baseURL;
    private final String activityTrackerService;

    public ActivityDispatcher(String baseURL, String activityTrackerService) {
        this.baseURL = baseURL;
        this.activityTrackerService = activityTrackerService;
    }

    public void sendActivityOverRMI(Service service, Date creationTime, Activity.ActivityAction activityAction,
                                    int dataId, Activity.DataType dataType, int userId) {
        try {
            Gson gson = new Gson();
            Activity.Builder activityBuilder = Activity.getBuilder();
            activityBuilder = activityBuilder.creationTime(creationTime);
            activityBuilder = activityBuilder.activityAction(activityAction);
            String resourcePath = null;
            if (dataType.equals(Activity.DataType.PROJECT)) {
                resourcePath = "projects";
            } else if (dataType.equals(Activity.DataType.COMPONENT)) {
                resourcePath = "components";
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                resourcePath = "requirements";
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                resourcePath = "comments";
            }
            resourcePath = resourcePath + "/" + String.valueOf(dataId);
            if (activityAction != Activity.ActivityAction.DELETE) {
                activityBuilder = activityBuilder.dataUrl(baseURL + resourcePath);
            }
            activityBuilder = activityBuilder.dataType(dataType);
            activityBuilder = activityBuilder.userUrl(baseURL + "users" + "/" + String.valueOf(userId));
            Activity activity = activityBuilder.build();
            Object result = service.invokeServiceMethod(activityTrackerService,
                    "createActivity", new Serializable[]{gson.toJson(activity)});
            if (((HttpResponse) result).getStatus() != HttpURLConnection.HTTP_CREATED) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.NETWORK, ErrorCode.RMI_ERROR, "");
            }
        } catch (Exception ex) {
            Context.logError(this, "Could not send activity with RMI call to ActivityTracker");
        }
    }
}
