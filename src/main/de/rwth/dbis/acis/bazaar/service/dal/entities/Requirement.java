package de.rwth.dbis.acis.bazaar.service.dal.entities;


import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import jodd.vtor.constraint.*;

import java.util.Date;
import java.util.List;

/**
 * Requirement entity
 */
public class Requirement extends EntityBase {
    @Min(-1)
    private int id;

    @NotBlank
    @NotNull(profiles = {"create"})
    @MaxLength(50)
    private String title;

    private Date creation_time;

    private Date lastupdated_time;

    @NotBlank
    @NotNull(profiles = {"create"})
    private String description;

    private Date realized;

    @NotNull(profiles = {"create"})
    @Size(min = 1, profiles = {"create"})
    private List<Component> components;

    private List<Attachment> attachments;

    @Min(-1)
    private int projectId;

    private int upVotes;
    private int downVotes;
    private UserVote userVoted;

    @Min(-1)
    private int creatorId;

    public Date getRealized() {
        return realized;
    }

    public Date getCreation_time() {
        return creation_time;
    }

    public Date getLastupdated_time() {
        return lastupdated_time;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Requirement() {
    }

    protected Requirement(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.title = builder.title;
        this.realized = builder.realized;
        this.projectId = builder.projectId;
        this.creatorId = builder.creatorId;
        this.creation_time = builder.creation_time;
        this.lastupdated_time = builder.lastupdated_time;
        this.upVotes = builder.upVotes;
        this.downVotes = builder.downVotes;
        this.userVoted = builder.userVoted;
        this.attachments = builder.attachments;
    }

    /**
     * Builder to easily build Requirement objects
     *
     * @param title Title field will be initialized using the passed value
     * @return a builder with title returned
     */
    public static Builder getBuilder(String title) {
        return new Builder(title);
    }

    public static class Builder {
        private int id;
        private String description;
        private String title;
        private Date realized;
        private int projectId;
        private int creatorId;
        private Date creation_time;
        private Date lastupdated_time;
        private int upVotes;
        private int downVotes;
        private UserVote userVoted;
        protected List<Attachment> attachments;

        public Builder(String title) {
            this.title = title;
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

            String name = created.getTitle();

            if (name == null || name.length() == 0) {
                throw new IllegalStateException("title cannot be null or empty");
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

        public Builder creationTime(Date creationTime) {
            this.creation_time = creationTime;
            return this;
        }

        public Builder lastupdatedTime(Date lastupdatedTime) {
            this.lastupdated_time = lastupdatedTime;
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
