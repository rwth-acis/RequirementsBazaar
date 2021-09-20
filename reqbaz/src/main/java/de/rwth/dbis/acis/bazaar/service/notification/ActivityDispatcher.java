package de.rwth.dbis.acis.bazaar.service.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;

import javax.ws.rs.core.Response;
import java.time.OffsetDateTime;

/**
 * Created by martin on 15.02.2016.
 */
public class ActivityDispatcher {

    private final L2pLogger logger = L2pLogger.getInstance(ActivityDispatcher.class.getName());
    private final String activityTrackerService;
    private final String activityOrigin;
    private final String baseURL;
    private final String frontendBaseURL;
    private final BazaarService bazaarService;

    public ActivityDispatcher(BazaarService bazaarService, String activityTrackerService, String activityOrigin, String baseURL, String frontendBaseURL) {
        this.bazaarService = bazaarService;
        this.activityTrackerService = activityTrackerService;
        this.activityOrigin = activityOrigin;
        this.baseURL = baseURL;
        this.frontendBaseURL = frontendBaseURL;
    }

    public void sendActivityOverRMI(OffsetDateTime creationDate, Activity.ActivityAction activityAction,
                                    int dataId, Activity.DataType dataType, int userId, Activity.AdditionalObject additionalObject) {
        DALFacade dalFacade;
        try {
            dalFacade = bazaarService.getDBConnection();

            Activity.Builder activityBuilder = Activity.builder();
            activityBuilder = activityBuilder.creationDate(creationDate);
            activityBuilder = activityBuilder.activityAction(activityAction);

            String resourcePath = "";
            String parentResourcePath = null;
            String frontendResourcePath = "";
            int parentDataId = 0;
            Activity.DataType parentDataTyp = null;

            if (dataType.equals(Activity.DataType.PROJECT)) {
                resourcePath = "projects";
                frontendResourcePath = "projects" + "/" + String.valueOf(dataId);
            } else if (dataType.equals(Activity.DataType.CATEGORY)) {
                resourcePath = "categories";
                parentResourcePath = "projects";
                Category category = dalFacade.getCategoryById(dataId, userId);
                frontendResourcePath = "projects" + "/" + category.getProjectId() + "/" + "categories" + "/" + String.valueOf(dataId);
                parentDataId = category.getProjectId();
                parentDataTyp = Activity.DataType.PROJECT;
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                resourcePath = "requirements";
                parentResourcePath = "categories";
                Requirement requirement = dalFacade.getRequirementById(dataId, userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                        requirement.getCategories().get(0) + "/" + "requirements" + "/" + String.valueOf(dataId);
                parentDataId = requirement.getCategories().get(0);
                parentDataTyp = Activity.DataType.CATEGORY;
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                resourcePath = "comments";
                parentResourcePath = "requirements";
                Comment comment = dalFacade.getCommentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                        requirement.getCategories().get(0) + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
                parentDataId = requirement.getId();
                parentDataTyp = Activity.DataType.REQUIREMENT;
            } else if (dataType.equals(Activity.DataType.ATTACHMENT)) {
                resourcePath = "attachments";
                parentResourcePath = "requirements";
                Attachment attachment = dalFacade.getAttachmentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(attachment.getRequirementId(), userId);
                frontendResourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                        requirement.getCategories().get(0) + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
                parentDataId = requirement.getId();
                parentDataTyp = Activity.DataType.REQUIREMENT;
            } else if (dataType.equals(Activity.DataType.USER)) {
                resourcePath = "users";
                frontendResourcePath = "users" + "/" + dataId;
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
            activityBuilder = activityBuilder.origin(activityOrigin);
            activityBuilder = activityBuilder.additionalObject(additionalObject);
            Activity activity = activityBuilder.build();

            FilterProvider filters =
                    new SimpleFilterProvider().addFilter(
                            "ActivityFilter",
                            SimpleBeanPropertyFilter.filterOutAllExcept("id", "name"));

            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            mapper.registerModule(new JavaTimeModule());
            mapper.setFilterProvider(filters);

            Object result = Context.get().invoke(activityTrackerService, "createActivity", mapper.writeValueAsString(activity));
            if (!(result).equals(Integer.toString(Response.Status.CREATED.getStatusCode()))) {
                ExceptionHandler.getInstance().throwException(ExceptionLocation.NETWORK, ErrorCode.RMI_ERROR, "ActivityTracker RMI call failed");
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }
}
