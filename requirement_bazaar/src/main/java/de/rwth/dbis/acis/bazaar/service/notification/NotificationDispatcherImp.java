package de.rwth.dbis.acis.bazaar.service.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import i5.las2peer.api.Context;
import i5.las2peer.api.logging.MonitoringEvent;
import i5.las2peer.logging.L2pLogger;

import java.time.LocalDateTime;
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
    public void dispatchNotification(final LocalDateTime creationDate, final Activity.ActivityAction activityAction, final MonitoringEvent mobSOSEvent,
                                     final int dataId, final Activity.DataType dataType, final int userId) {

        // Filters to generate JSON elements
        FilterProvider filters =
                new SimpleFilterProvider().addFilter(
                        "ActivityFilter",
                        SimpleBeanPropertyFilter.filterOutAllExcept("id", "name"));
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.setFilters(filters);

        Activity.AdditionalObject additionalObject = generateAdditionalObject(dataType, dataId, userId);

        try {
            if (emailDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                    activityAction == Activity.ActivityAction.REALIZE)) {
                // add email
                emailDispatcher.addEmailNotification(creationDate, activityAction, dataId, dataType, userId, additionalObject);
            }
            if (activityDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
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
            if (dataType.equals(Activity.DataType.PROJECT) && dataId != 0) {
                Project project = dalFacade.getProjectById(dataId, userId);
                additionalObject = new Activity.AdditionalObject(project, null, null, user);
            } else if (dataType.equals(Activity.DataType.CATEGORY)) {
                Category category = dalFacade.getCategoryById(dataId, userId);
                Project project = dalFacade.getProjectById(category.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, null, user);
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                Requirement requirement = dalFacade.getRequirementById(dataId, userId);
                Category category = dalFacade.getCategoryById(requirement.getCategories().get(0).getId(), userId);
                Project project = dalFacade.getProjectById(requirement.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, requirement, user);
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                Comment comment = dalFacade.getCommentById(dataId);
                Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                Category category = dalFacade.getCategoryById(requirement.getCategories().get(0).getId(), userId);
                Project project = dalFacade.getProjectById(requirement.getProjectId(), userId);
                additionalObject = new Activity.AdditionalObject(project, category, requirement, user);
            } else if (dataType.equals(Activity.DataType.ATTACHMENT)) {
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
