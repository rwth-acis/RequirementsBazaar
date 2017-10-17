package de.rwth.dbis.acis.bazaar.service.dal.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by Martin on 15.06.2017.
 */
public class CategoryContributors extends EntityBase {

    private final int id;

    private User leader;
    private List<User> requirementCreator;
    private List<User> leadDeveloper;
    private List<User> developers;
    private List<User> commentCreator;
    private List<User> attachmentCreator;

    protected CategoryContributors(Builder builder) {
        this.id = builder.id;
        this.leader = builder.leader;
        this.requirementCreator = builder.requirementCreator;
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

    public User getLeader() {
        return leader;
    }

    public List<User> getRequirementCreator() {
        return requirementCreator;
    }

    public List<User> getLeadDeveloper() {
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
        private User leader;
        private List<User> requirementCreator;
        private List<User> leadDeveloper;
        private List<User> developers;
        private List<User> commentCreator;
        private List<User> attachmentCreator;

        public Builder leader(User leader) {
            this.leader = leader;
            return this;
        }

        public Builder requirementCreator(List<User> requirementCreator) {
            this.requirementCreator = requirementCreator;
            return this;
        }

        public Builder leadDeveloper(List<User> leadDeveloper) {
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

        public CategoryContributors build() {
            return new CategoryContributors(this);
        }
    }
}
