package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.ws.rs.container.ContainerRequestContext;

import java.util.Date;

public class Activity extends EntityBase {

    private final transient int id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Date creationDate;
    private final ActivityAction activityAction;
    private final String dataUrl;
    private final DataType dataType;
    private final String dataFrontendUrl;
    private final String parentDataUrl;
    private final DataType parentDataType;
    private final String userUrl;
    private final String origin;

    private AdditionalObject additionalObject;

    private Activity(Builder builder) {
        this.id = builder.id;
        this.creationDate = builder.creationDate;
        this.activityAction = builder.activityAction;
        this.dataUrl = builder.dataUrl;
        this.dataType = builder.dataType;
        this.dataFrontendUrl = builder.dataFrontendUrl;
        this.parentDataUrl = builder.parentDataUrl;
        this.parentDataType = builder.parentDataType;
        this.userUrl = builder.userUrl;
        this.origin = builder.origin;
        this.additionalObject = builder.additionalObject;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    @JsonIgnore
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

    public String getOrigin() {
        return origin;
    }

    public AdditionalObject getAdditionalObject() {
        return additionalObject;
    }

    public enum DataType {
        STATISTIC,
        PERSONALISATION,
        PROJECT,
        CATEGORY,
        REQUIREMENT,
        COMMENT,
        ATTACHMENT,
        USER;
    }

    public enum ActivityAction {
        RETRIEVE,
        RETRIEVE_LIST,
        RETRIEVE_CHILD,
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
        protected String origin;
        protected AdditionalObject additionalObject;

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

        public Builder origin(String origin) {
            this.origin = origin;
            return this;
        }

        public Builder addititionalObject(AdditionalObject additionalObject) {
            this.additionalObject = additionalObject;
            return this;
        }

        public Activity build() {
            return new Activity(this);
        }
    }

    @JsonFilter("AdditionalObjectFilter")
    public static class AdditionalObject {
        @JsonFilter("ActivityFilter")
        private Project project;

        @JsonFilter("ActivityFilter")
        private Category category;

        @JsonFilter("ActivityFilter")
        private Requirement requirement;

        @JsonFilter("ActivityFilter")
        private User user;

        private PersonalisationData personalisationData;

        private RequestInformation request;







        public Project getProject() {
            return project;
        }

        public Category getCategory() {
            return category;
        }

        public Requirement getRequirement() {
            return requirement;
        }

        public User getUser() {
            return user;
        }

        public PersonalisationData getPersonalisationData(){ return personalisationData;}

        public  RequestInformation getRequest(){ return request; }

        public void setProject(Project project) {
            this.project = project;
        }

        public void setCategory(Category category) {
            this.category = category;
        }

        public void setRequirement(Requirement requirement) {
            this.requirement = requirement;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public void setPersonalisationData(PersonalisationData personalisationData) {
            this.personalisationData = personalisationData;
        }
        public void setRequest(RequestInformation request){
            this.request = request;
        }





        public AdditionalObject(Project project, Category category, Requirement requirement, User user, PersonalisationData personalisationData, RequestInformation request) {
            this.project = project;
            this.category = category;
            this.requirement = requirement;
            this.user = user;
            this.personalisationData = personalisationData;
            this.request = request;
        }
        public AdditionalObject(Project project, Category category, Requirement requirement, User user, PersonalisationData personalisationData) {
            this(project, category, requirement, user, personalisationData, null);
        }
        public AdditionalObject(Project project, Category category, Requirement requirement, User user) {
            this(project, category, requirement, user, null, null);
        }
        public AdditionalObject(RequestInformation request) {
            this(null, null, null, null, null, request);
        }
        public AdditionalObject(PersonalisationData data) {
            this(null, null, null, null, data, null);
        }


    }
    public static class RequestInformation {
         private String requestUri;
         private String referer;
         private String userAgent;

         public String getRequestUri() { return requestUri; }
         public String getReferer() { return referer; }
         public String getUserAgent() { return userAgent; }

         public RequestInformation(String requestUri, String referer, String userAgent){
             this.referer = referer;
             this.requestUri = requestUri;
             this.userAgent = userAgent;
         }
         public RequestInformation(ContainerRequestContext context){
             this(null,null,null);
             this.referer = context.getHeaderString("referer");
             //this.userAgent = context.getHeaderString("user-agent");          //Commented out for now since not used
             this.requestUri = context.getUriInfo().getRequestUri().toString();



         }
    }
}
