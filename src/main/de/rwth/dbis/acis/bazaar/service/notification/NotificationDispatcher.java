package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;

import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public interface NotificationDispatcher {
    void dispatchNotification(Date creationDate, Activity.ActivityAction activityAction,
                              int dataId, Activity.DataType dataType, int parentDataId, Activity.DataType parentDataType, int userId);

    void setActivityDispatcher(ActivityDispatcher activityDispatcher);

    void setEmailDispatcher(EmailDispatcher emailDispatcher);
}
