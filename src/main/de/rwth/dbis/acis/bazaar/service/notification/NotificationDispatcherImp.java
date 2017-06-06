package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import i5.las2peer.api.Service;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by martin on 15.02.2016.
 */
public class NotificationDispatcherImp implements NotificationDispatcher {

    private ActivityDispatcher activityDispatcher;
    private EmailDispatcher emailDispatcher;
    ExecutorService executorService = Executors.newCachedThreadPool();

    public void setActivityDispatcher(ActivityDispatcher activityDispatcher) {
        this.activityDispatcher = activityDispatcher;
    }

    public void setEmailDispatcher(EmailDispatcher emailDispatcher) {
        this.emailDispatcher = emailDispatcher;
    }

    @Override
    public void dispatchNotification(final Service service, final Date creationDate, final Activity.ActivityAction activityAction,
                                     final int dataId, final Activity.DataType dataType, final int parentDataId,
                                     final Activity.DataType parentDataType, final int userId) {
//        executorService.execute(new Runnable() {
//            public void run() {
//                if (activityDispatcher != null) {
//                    activityDispatcher.sendActivityOverRMI(service, creationDate, activityAction, dataId, dataType, userId);
//                }
//            }
//        });
        executorService.execute(new Runnable() {
            public void run() {
                if (emailDispatcher != null && (activityAction == Activity.ActivityAction.CREATE || activityAction == Activity.ActivityAction.UPDATE ||
                        activityAction == Activity.ActivityAction.REALIZE)) {
                    emailDispatcher.sendEmailNotification(creationDate, activityAction, dataId, dataType, userId);
                }
            }
        });
        if (activityDispatcher != null) {
            activityDispatcher.sendActivityOverRMI(service, creationDate, activityAction, dataId, dataType, parentDataId,
                    parentDataType, userId);
        }
    }

}
