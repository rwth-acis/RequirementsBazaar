package de.rwth.dbis.acis.bazaar.service.notification;

import com.google.gson.Gson;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Category;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Service;

import javax.ws.rs.core.Response;
import java.io.Serializable;
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

    public void sendActivityOverRMI(Service service, Date creationDate, Activity.ActivityAction activityAction,
                                    int dataId, Activity.DataType dataType, int parentDataId,
                                    Activity.DataType parentDataTyp, int userId) {
        DALFacade dalFacade = null;
        try {
            dalFacade = bazaarService.getDBConnection();

            Gson gson = new Gson();
            Activity.Builder activityBuilder = Activity.getBuilder();
            activityBuilder = activityBuilder.creationDate(creationDate);
            activityBuilder = activityBuilder.activityAction(activityAction);

            String resourcePath = new String();
            String parentResourcePath = null;
            String frontendResourcePath = new String();
            if (dataType.equals(Activity.DataType.PROJECT)) {
                resourcePath = "projects";
                frontendResourcePath = "projects" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.CATEGORY)) {
                resourcePath = "categories";
                parentResourcePath = "projects";
                Category category = dalFacade.getCategoryById(dataId);
                frontendResourcePath = "projects" + "/" + category.getProjectId() + "/" + "categories" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                resourcePath = "requirements";
                parentResourcePath = "categories";
                Requirement requirement = dalFacade.getRequirementById(dataId, userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                        requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                resourcePath = "comments";
                parentResourcePath = "requirements";
                Comment comment = dalFacade.getCommentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                        requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
            }
            resourcePath = resourcePath + "/" + String.valueOf(dataId);
            if (parentResourcePath != null) {
                parentResourcePath = parentResourcePath + "/" + String.valueOf(parentDataId);
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

            Object result = service.getContext().invoke(activityTrackerService, "createActivity", new Serializable[]{gson.toJson(activity)});
            if (!(result).equals(new Integer(Response.Status.CREATED.getStatusCode()).toString())) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.NETWORK, ErrorCode.RMI_ERROR, "ActivityTracker RMI call failed");
            }
        } catch (Exception ex) {
            //TODO log
            System.out.println(ex.getMessage());
        }
    }
}
