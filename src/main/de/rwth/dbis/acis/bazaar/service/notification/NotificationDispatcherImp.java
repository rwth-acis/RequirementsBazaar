package de.rwth.dbis.acis.bazaar.service.notification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import i5.las2peer.api.Context;
import i5.las2peer.logging.L2pLogger;
import i5.las2peer.logging.NodeObserver;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by martin on 15.02.2016.
 */
public class NotificationDispatcherImp implements NotificationDispatcher {

    private L2pLogger logger = L2pLogger.getInstance(NotificationDispatcherImp.class.getName());
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private ActivityDispatcher activityDispatcher;
    private EmailDispatcher emailDispatcher;
    private BazaarService bazaarService;

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
    public void dispatchNotification(final Date creationDate, final Activity.ActivityAction activityAction, final NodeObserver.Event mobSOSEvent,
                                     final int dataId, final Activity.DataType dataType, final int userId) {
//        executorService.execute(new Runnable() { //TODO: try to run sendActivityOverRMI inside Runnable when las2peer allows this
//            public void run() {
//                if (activityDispatcher != null) {
//                    activityDispatcher.sendActivityOverRMI(service, creationDate, activityAction, dataId, dataType, userId);
//                }
//            }
//        });
        try {
            executorService.execute(new Runnable() {
                public void run() {
                    if (emailDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                            activityAction == Activity.ActivityAction.REALIZE)) {
                        emailDispatcher.sendEmailNotification(creationDate, activityAction, dataId, dataType, userId);
                    }
                }
            });
            Activity.AdditionalObject additionalObject = generateAdditonalObject(dataType, dataId, userId);
            if (activityDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                    activityAction == Activity.ActivityAction.REALIZE || activityAction == Activity.ActivityAction.DEVELOP ||
                    activityAction == Activity.ActivityAction.LEADDEVELOP || activityAction == Activity.ActivityAction.FOLLOW ||
                    activityAction == Activity.ActivityAction.VOTE)) {
                activityDispatcher.sendActivityOverRMI(creationDate, activityAction, dataId, dataType, userId, additionalObject);
            }
            if (mobSOSEvent != null) {
                L2pLogger.logEvent(mobSOSEvent, Context.getCurrent().getMainAgent(), new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(additionalObject));
            }
        } catch (JsonProcessingException e) {
            logger.warning(e.getMessage());
        }
    }

    private Activity.AdditionalObject generateAdditonalObject(Activity.DataType dataType, int dataId, int userId) {
        DALFacade dalFacade;
        Activity.AdditionalObject additionalObject = null;
        try {
            dalFacade = bazaarService.getDBConnection();

            User user = dalFacade.getUserById(userId);
            if (dataType.equals(Activity.DataType.PROJECT)) {
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
                Requirement requirement = dalFacade.getRequirementById(comment.getId(), userId);
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
}
