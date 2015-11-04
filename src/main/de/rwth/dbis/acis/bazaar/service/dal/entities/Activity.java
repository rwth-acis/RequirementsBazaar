package de.rwth.dbis.acis.bazaar.service.dal.entities;

import java.util.Date;

public class Activity extends EntityBase {

    private final int id;
    private final Date creationTime;
    private final ActivityAction activityAction;
    private final String dataUrl;
    private final DataType dataType;
    private final String userUrl;

    @Override
    public int getId() {
        return id;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public ActivityAction getActivityAction() {
        return activityAction;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public DataType getDataType() {
        return dataType;
    }

    public String getUserUrl() {
        return userUrl;
    }

    protected Activity(Builder builder) {
        this.id = builder.id;
        this.creationTime = builder.creationTime;
        this.activityAction = builder.activityAction;
        this.dataUrl = builder.dataUrl;
        this.dataType = builder.dataType;
        this.userUrl = builder.userUrl;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {

        protected int id;
        protected Date creationTime;
        protected ActivityAction activityAction;
        protected String dataUrl;
        protected DataType dataType;
        protected String userUrl;

        public Builder creationTime(Date creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder activityAction(ActivityAction activityAction) {
            this.activityAction = activityAction;
            return this;
        }

        public Builder dataUrl(String dataUrl) {
            this.dataUrl = dataUrl;
            return this;
        }

        public Builder dataType(DataType dataType) {
            this.dataType = dataType;
            return this;
        }

        public Builder userUrl(String userUrl) {
            this.userUrl = userUrl;
            return this;
        }

        public Activity build() {
            Activity created = new Activity(this);
            return created;
        }
    }

    public enum DataType {
        PROJECT,
        COMPONENT,
        REQUIREMENT,
        COMMENT,
        ATTACHMENT,
        VOTE
    }

    public enum ActivityAction {
        CREATE,
        UPDATE,
        DELETE
    }
}
