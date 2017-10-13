package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

// import javax.ws.rs.core.Link;
import java.util.List;

/**
 * Created by Martin on 12.06.2017.
 */
public class RequirementContributors extends EntityBase {

    private final int id;

    private User creator;
    private User leadDeveloper;
    private List<User> developers;
    //private Link developersLink; // TODO: Skip after 10 elements and add HATEOAS link
    private List<User> commentCreator;
    //private Link commentCreatorLink;
    private List<User> attachmentCreator;
    //private Link attachmentCreatorLink;

    protected RequirementContributors(Builder builder) {
        this.id = builder.id;
        this.creator = builder.creator;
        this.leadDeveloper = builder.leadDeveloper;
        this.developers = builder.developers;
        this.commentCreator = builder.commentCreator;
        this.attachmentCreator = builder.attachmentCreator;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    @Override
    @JsonIgnore
    public int getId() {
        return id;
    }

    public User getCreator() {
        return creator;
    }

    public User getLeadDeveloper() {
        return leadDeveloper;
    }

    public List<User> getDevelopers() {
        return developers;
    }

    public List<User> getCommentCreator() {
        return commentCreator;
    }

    public List<User> getAttachmentCreator() {
        return attachmentCreator;
    }

    public static class Builder {

        private int id;
        private User creator;
        private User leadDeveloper;
        private List<User> developers;
        private List<User> commentCreator;
        private List<User> attachmentCreator;

        public Builder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public Builder leadDeveloper(User leadDeveloper) {
            this.leadDeveloper = leadDeveloper;
            return this;
        }

        public Builder developers(List<User> developers) {
            this.developers = developers;
            return this;
        }

        public Builder commentCreator(List<User> commentCreator) {
            this.commentCreator = commentCreator;
            return this;
        }

        public Builder attachmentCreator(List<User> attachmentCreator) {
            this.attachmentCreator = attachmentCreator;
            return this;
        }

        public RequirementContributors build() {
            return new RequirementContributors(this);
        }
    }
}
