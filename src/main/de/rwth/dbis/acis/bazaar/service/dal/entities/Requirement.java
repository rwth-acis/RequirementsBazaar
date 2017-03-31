package de.rwth.dbis.acis.bazaar.service.dal.entities;


import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import jodd.vtor.constraint.*;

import java.util.Date;
import java.util.List;

/**
 * Requirement entity
 */
public class Requirement extends EntityBase {

    private int id;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    @MaxLength(value = 50, profiles = {"*"})
    private String name;

    private Date creationDate;

    private Date lastUpdatedDate;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    @MaxLength(value = 1000, profiles = {"*"})
    private String description;

    private Date realized;

    @NotNull(profiles = {"create"})
    @Size(min = 1, profiles = {"create"})
    private List<Component> components;

    private List<Attachment> attachments;

    @Min(value = 0, profiles = {"create"})
    private int projectId;

    private int upVotes;
    private int downVotes;
    private UserVote userVoted;

    @Min(-1)
    private int creatorId;

    private Integer numberOfComments;
    private Integer numberOfAttachments;
    private Integer numberOfFollowers;

    public Date getRealized() {
        return realized;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Component> getComponents() {
        return components;
    }

    public void setComponents(List<Component> components) {
        this.components = components;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public void setNumberOfComments(Integer numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public void setNumberOfAttachments(Integer numberOfAttachments) {
        this.numberOfAttachments = numberOfAttachments;
    }

    public void setNumberOfFollowers(Integer numberOfFollowers) {
        this.numberOfFollowers = numberOfFollowers;
    }

    public Requirement() {
    }

    protected Requirement(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.name = builder.name;
        this.realized = builder.realized;
        this.projectId = builder.projectId;
        this.creatorId = builder.creatorId;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.upVotes = builder.upVotes;
        this.downVotes = builder.downVotes;
        this.userVoted = builder.userVoted;
        this.attachments = builder.attachments;
    }

    /**
     * Builder to easily build Requirement objects
     *
     * @param name Name field will be initialized using the passed value
     * @return a builder with name returned
     */
    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private int id;
        private String description;
        private String name;
        private Date realized;
        private int projectId;
        private int creatorId;
        private Date creationDate;
        private Date lastUpdatedDate;
        private int upVotes;
        private int downVotes;
        private UserVote userVoted;
        protected List<Attachment> attachments;

        public Builder(String name) {
            this.name = name;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        /**
         * Call this to create a Requirement object with the values previously set in the builder.
         *
         * @return initialized Requirement object
         */
        public Requirement build() {
            Requirement created = new Requirement(this);

            String name = created.getName();

            if (name == null || name.length() == 0) {
                throw new IllegalStateException("name cannot be null or empty");
            }

            return created;
        }

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder creatorId(int userId) {
            this.creatorId = userId;
            return this;
        }

        public Builder realized(Date realized) {
            this.realized = realized;
            return this;
        }

        public Builder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdatedDate(Date lastUpdatedDate) {
            this.lastUpdatedDate = lastUpdatedDate;
            return this;
        }

        public Builder upVotes(int upVotes) {
            this.upVotes = upVotes;
            return this;
        }

        public Builder downVotes(int downVotes) {
            this.downVotes = downVotes;
            return this;
        }

        public Builder userVoted(UserVote userVoted) {
            this.userVoted = userVoted;
            return this;
        }
    }
}
