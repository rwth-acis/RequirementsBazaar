package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import i5.las2peer.api.logging.MonitoringEvent;

import java.util.Date;

/**
 * Created by martin on 15.02.2016.
 */
public interface NotificationDispatcher extends Runnable {
    void dispatchNotification(Date creationDate, Activity.ActivityAction activityAction, final MonitoringEvent mobSOSEvent,
                              int dataId, Activity.DataType dataType, int userId);

    void setBazaarService(BazaarService service);

    void setActivityDispatcher(ActivityDispatcher activityDispatcher);

    void setEmailDispatcher(EmailDispatcher emailDispatcher);
}
