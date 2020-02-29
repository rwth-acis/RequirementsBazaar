package de.rwth.dbis.acis.bazaar.service.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.L2pLogger;

import java.util.Date;
import java.util.TimerTask;

/**
 * Created by martin on 15.02.2016.
 */
public class NotificationDispatcherImp extends TimerTask implements NotificationDispatcher {

    private L2pLogger logger = L2pLogger.getInstance(NotificationDispatcherImp.class.getName());
    private ActivityDispatcher activityDispatcher;
    private EmailDispatcher emailDispatcher;
    private BazaarService bazaarService;
    private ObjectMapper mapper;

    public void setBazaarService(BazaarService service) {
        this.bazaarService = service;
    }

    public void setActivityDispatcher(ActivityDispatcher activityDispatcher) {
        this.activityDispatcher = activityDispatcher;
    }

    public void setEmailDispatcher(EmailDispatcher emailDispatcher) {
        this.emailDispatcher = emailDispatcher;
    }

    @Override
    public void dispatchNotification(final Date creationDate, final Activity.ActivityAction activityAction, final MonitoringEvent mobSOSEvent,
                                     final int dataId, final Activity.DataType dataType, final int userId) {
        dispatchNotification(creationDate, activityAction,mobSOSEvent, dataId, dataType, userId, null);
    }

    @Override
    public void dispatchNotification(final Date creationDate, final Activity.ActivityAction activityAction, final MonitoringEvent mobSOSEvent,
                                     final int dataId, final Activity.DataType dataType, final int userId,Activity.AdditionalObject additionalObject) {

        // Filters to generate JSON elements
        FilterProvider filters =
                new SimpleFilterProvider()
                        .addFilter("ActivityFilter", SimpleBeanPropertyFilter.filterOutAllExcept("id", "name"))
                        .addFilter("AdditionalObjectFilter", SimpleBeanPropertyFilter.serializeAllExcept());
        mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setFilters(filters);

        if(additionalObject == null) {
            additionalObject = generateAdditionalObject(dataType, dataId, userId);
        }else{
            //If we parse an external additional Object, merge the generated one with the external.
            Activity.AdditionalObject defaultAdditional = generateAdditionalObject(dataType, dataId, userId);
            if(additionalObject.getCategory() != null) defaultAdditional.setCategory(additionalObject.getCategory());
            if(additionalObject.getPersonalisationData() != null) defaultAdditional.setPersonalisationData(additionalObject.getPersonalisationData());
            if(additionalObject.getProject() != null) defaultAdditional.setProject(additionalObject.getProject());
            if(additionalObject.getRequirement() != null) defaultAdditional.setRequirement(additionalObject.getRequirement());
            if(additionalObject.getUser() != null) defaultAdditional.setUser(additionalObject.getUser());
            if(additionalObject.getRequest() != null) defaultAdditional.setRequest(additionalObject.getRequest());
            additionalObject = defaultAdditional;
        }

        try {
            if (emailDispatcher != null && (dataType != Activity.DataType.PERSONALISATION) && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                    activityAction == Activity.ActivityAction.REALIZE)) {
                // add email
                emailDispatcher.addEmailNotification(creationDate, activityAction, dataId, dataType, userId, additionalObject);
            }
            if (activityDispatcher != null && (dataType != Activity.DataType.PERSONALISATION) && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                    activityAction == Activity.ActivityAction.REALIZE || activityAction == Activity.ActivityAction.DEVELOP ||
                    activityAction == Activity.ActivityAction.LEADDEVELOP || activityAction == Activity.ActivityAction.FOLLOW ||
                    activityAction == Activity.ActivityAction.VOTE)) {
                // dispatch activity
                activityDispatcher.sendActivityOverRMI(creationDate, activityAction, dataId, dataType, userId, additionalObject);
            }
            if (mobSOSEvent != null) {
                // dispatch mobSOS log call
                Context.get().monitorEvent(mobSOSEvent, mapper.writeValueAsString(additionalObject));
            }
        } catch (JsonProcessingException e) {
            logger.warning(e.getMessage());
        }
    }

    private Activity.AdditionalObject generateAdditionalObject(Activity.DataType dataType, int dataId, int userId) {
        DALFacade dalFacade;
        Activity.AdditionalObject additionalObject = null;
        try {
            dalFacade = bazaarService.getDBConnection();

            User user = dalFacade.getUserById(userId);
            if (dataType.equals(Activity.DataType.PERSONALISATION) && dataId != 0) {
                PersonalisationData personalisationData = dalFacade.getPersonalisationData(dataId);
                additionalObject = new Activity.AdditionalObject(null, null, null, user, personalisationData);
            } else if (dataType.equals(Activity.DataType.PROJECT) && dataId != 0) {
                Project project = dalFacade.getProjectById(dataId, userId);
                additionalObject = new Activity.AdditionalObject(project, null, null, user);
            } else if (dataType.equals(Activity.DataType.CATEGORY) && dataId != 0) {
                Category category = dalFacade.getCategoryById(dataId, userId);
                Project project = dalFacade.getProjectById(category.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, null, user);
            } else if (dataType.equals(Activity.DataType.REQUIREMENT) && dataId != 0) {
                Requirement requirement = dalFacade.getRequirementById(dataId, userId);
                Category category = dalFacade.getCategoryById(requirement.getCategories().get(0).getId(), userId);
                Project project = dalFacade.getProjectById(requirement.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, requirement, user);
            } else if (dataType.equals(Activity.DataType.COMMENT) && dataId != 0) {
                Comment comment = dalFacade.getCommentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                Category category = dalFacade.getCategoryById(requirement.getCategories().get(0).getId(), userId);
                Project project = dalFacade.getProjectById(requirement.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, requirement, user);
            } else if (dataType.equals(Activity.DataType.ATTACHMENT) && dataId != 0) {
                Attachment attachment = dalFacade.getAttachmentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(attachment.getRequirementId(), userId);
                Category category = dalFacade.getCategoryById(requirement.getCategories().get(0).getId(), userId);
                Project project = dalFacade.getProjectById(requirement.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, requirement, user);
            } else {
                additionalObject = new Activity.AdditionalObject(null, null, null, user);
            }
        } catch (Exception e) {
            logger.warning(e.getMessage());
        }
        return additionalObject;
    }

    @Override
    public void run() {
        if (emailDispatcher != null) {
            emailDispatcher.emptyNotificationSummery();
        }
    }
}
