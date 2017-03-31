package de.rwth.dbis.acis.bazaar.service.dal.entities;

import java.util.Date;

public class Activity extends EntityBase {

    private final int id;
    private final Date creationDate;
    private final ActivityAction activityAction;
    private final String dataUrl;
    private final DataType dataType;
    private final String dataFrontendUrl;
    private final String parentDataUrl;
    private final DataType parentDataType;
    private final String userUrl;

    @Override
    public int getId() {
        return id;
    }

    public Date getCreationDate() {
        return creationDate;
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

    public String getDataFrontendUrl() {
        return dataFrontendUrl;
    }

    public String getParentDataUrl() {
        return parentDataUrl;
    }

    public DataType getParentDataType() {
        return parentDataType;
    }

    public String getUserUrl() {
        return userUrl;
    }

    protected Activity(Builder builder) {
        this.id = builder.id;
        this.creationDate = builder.creationDate;
        this.activityAction = builder.activityAction;
        this.dataUrl = builder.dataUrl;
        this.dataType = builder.dataType;
        this.dataFrontendUrl = builder.dataFrontendUrl;
        this.parentDataUrl = builder.parentDataUrl;
        this.parentDataType = builder.parentDataType;
        this.userUrl = builder.userUrl;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {

        protected int id;
        protected Date creationDate;
        protected ActivityAction activityAction;
        protected String dataUrl;
        protected DataType dataType;
        protected String dataFrontendUrl;
        protected String parentDataUrl;
        protected DataType parentDataType;
        protected String userUrl;

        public Builder creationDate(Date creationDate) {
            this.creationDate = creationDate;
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

        public Builder dataFrontendUrl(String dataFrontendUrl) {
            this.dataFrontendUrl = dataFrontendUrl;
            return this;
        }

        public Builder parentDataUrl(String parentDataUrl) {
            this.parentDataUrl = parentDataUrl;
            return this;
        }

        public Builder parentDataType(DataType parentDataType) {
            this.parentDataType = parentDataType;
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
        ATTACHMENT
    }

    public enum ActivityAction {
        CREATE,
        UPDATE,
        DELETE,
        REALIZE,
        UNREALIZE,
        VOTE,
        UNVOTE,
        DEVELOP,
        UNDEVELOP,
        FOLLOW,
        UNFOLLOW,
        LEADDEVELOP,
        UNLEADDEVELOP
    }
}
