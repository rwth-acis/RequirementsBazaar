package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import i5.las2peer.api.Service;

import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public interface NotificationDispatcher {
    void dispatchNotification(Service service, Date creationDate, Activity.ActivityAction activityAction,
                              int dataId, Activity.DataType dataType, int parentDataId, Activity.DataType parentDataType, int userId);

    void setActivityDispatcher(ActivityDispatcher activityDispatcher);

    void setEmailDispatcher(EmailDispatcher emailDispatcher);
}
