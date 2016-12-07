package de.rwth.dbis.acis.bazaar.service.notification;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementEx;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Service;
import org.apache.http.HttpResponse;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public class ActivityDispatcher {

    private BazaarService bazaarService;
    private final String activityTrackerService;
    private final String baseURL;
    private final String frontendBaseURL;

    public ActivityDispatcher(BazaarService bazaarService, String activityTrackerService, String baseURL, String frontendBaseURL) {
        this.bazaarService = bazaarService;
        this.activityTrackerService = activityTrackerService;
        this.baseURL = baseURL;
        this.frontendBaseURL = frontendBaseURL;
    }

    public void sendActivityOverRMI(Service service, Date creationTime, Activity.ActivityAction activityAction,
                                    int dataId, Activity.DataType dataType, int parantDataId,
                                    Activity.DataType parentDataTyp, int userId) {
        DALFacade dalFacade = null;
        try {
            dalFacade = bazaarService.getDBConnection();

            Gson gson = new Gson();
            Activity.Builder activityBuilder = Activity.getBuilder();
            activityBuilder = activityBuilder.creationTime(creationTime);
            activityBuilder = activityBuilder.activityAction(activityAction);

            String resourcePath = new String();
            String parentResourcePath = null;
            String frontendResourcePath = new String();
            if (dataType.equals(Activity.DataType.PROJECT)) {
                resourcePath = "projects";
                frontendResourcePath = "projects" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.COMPONENT)) {
                resourcePath = "components";
                parentResourcePath = "projects";
                Component component = dalFacade.getComponentById(dataId);
                frontendResourcePath = "projects" + "/" + component.getProjectId() + "/" + "components" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                resourcePath = "requirements";
                parentResourcePath = "components";
                RequirementEx requirement = dalFacade.getRequirementById(dataId, userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "components" + "/" +
                        requirement.getComponents().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                resourcePath = "comments";
                parentResourcePath = "requirements";
                Comment comment = dalFacade.getCommentById(dataId);
                RequirementEx requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "components" + "/" +
                        requirement.getComponents().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
            }
            resourcePath = resourcePath + "/" + String.valueOf(dataId);
            if (parentResourcePath != null) {
                parentResourcePath = parentResourcePath + "/" + String.valueOf(parantDataId);
            }

            if (activityAction != Activity.ActivityAction.DELETE) {
                activityBuilder = activityBuilder.dataUrl(baseURL + resourcePath);
            }
            activityBuilder = activityBuilder.dataType(dataType);
            String frontendUrl = frontendBaseURL.concat(frontendResourcePath);
            activityBuilder = activityBuilder.dataFrontendUrl(frontendUrl);
            if (parentResourcePath != null) {
                activityBuilder = activityBuilder.parentDataUrl(baseURL + parentResourcePath);
                activityBuilder = activityBuilder.parentDataType(parentDataTyp);
            }
            activityBuilder = activityBuilder.userUrl(baseURL + "users" + "/" + String.valueOf(userId));
            Activity activity = activityBuilder.build();
            Object result = service.invokeServiceMethod(activityTrackerService,
                    "createActivity", new Serializable[]{gson.toJson(activity)});
            if (((HttpResponse) result).getStatusLine().getStatusCode() != HttpURLConnection.HTTP_CREATED) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.NETWORK, ErrorCode.RMI_ERROR, "");
            }
        } catch (Exception ex) {
            //TODO log
        }
    }
}
