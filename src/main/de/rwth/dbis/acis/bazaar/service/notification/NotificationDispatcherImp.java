package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import i5.las2peer.api.Service;

import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public class NotificationDispatcherImp implements NotificationDispatcher {

    private ActivityDispatcher activityDispatcher;
    private EmailDispatcher emailDispatcher;

    public void setActivityDispatcher(ActivityDispatcher activityDispatcher) {
        this.activityDispatcher = activityDispatcher;
    }

    public void setEmailDispatcher(EmailDispatcher emailDispatcher) {
        this.emailDispatcher = emailDispatcher;
    }

    @Override
    public void dispatchNotification(Service service, Date creationTime, Activity.ActivityAction activityAction, int dataId, Activity.DataType dataType, String resourcePath, int userId) {
        if (activityDispatcher != null) {
            activityDispatcher.sendActivityOverRMI(service, creationTime, activityAction, dataId, dataType, resourcePath, userId);
        }
        if (emailDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE)) {
            emailDispatcher.sendEmailNotification(creationTime, activityAction, dataId, dataType, resourcePath, userId);
        }
    }

}
