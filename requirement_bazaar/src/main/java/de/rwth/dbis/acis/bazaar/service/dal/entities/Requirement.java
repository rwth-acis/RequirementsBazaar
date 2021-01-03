package de.rwth.dbis.acis.bazaar.service.dal.entities;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import jodd.vtor.constraint.*;

import java.time.LocalDateTime;
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

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime realized;

    @Min(value = 0, profiles = {"create"})
    private int projectId;

    private User creator;
    private User leadDeveloper;

    @NotNull(profiles = {"create"})
    @Size(min = 1, profiles = {"create"})
    private List<Category> categories;

    // This field is not filled because attachments should be not included in requirements response.
    // But the API still allows to create a requirement with attachments at the same time.
    private List<Attachment> attachments;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime lastUpdatedDate;

    private Integer numberOfComments;
    private Integer numberOfAttachments;
    private Integer numberOfFollowers;

    private int upVotes;
    private int downVotes;
    private UserVote userVoted;

    private Boolean isFollower;
    private Boolean isDeveloper;
    private Boolean isContributor;

    @JsonProperty("_context")
    private EntityContext context;

    public Requirement() {
    }

    protected Requirement(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.realized = builder.realized;
        this.projectId = builder.projectId;
        this.creator = builder.creator;
        this.leadDeveloper = builder.leadDeveloper;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.upVotes = builder.upVotes;
        this.downVotes = builder.downVotes;
        this.userVoted = builder.userVoted;
        this.isFollower = builder.isFollower;
        this.isDeveloper = builder.isDeveloper;
        this.isContributor = builder.isContributor;
        this.context = builder.context;
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

    public LocalDateTime getRealized() {
        return realized;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public List<Attachment> getAttachments() {
        return attachments;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getLeadDeveloper() {
        return leadDeveloper;
    }

    public void setFollower(Boolean follower) {
        isFollower = follower;
    }

    public void setDeveloper(Boolean developer) {
        isDeveloper = developer;
    }

    public void setContributor(Boolean contributor) {
        isContributor = contributor;
    }

    public Integer getNumberOfComments() {
        return numberOfComments;
    }

    public void setNumberOfComments(Integer numberOfComments) {
        this.numberOfComments = numberOfComments;
    }

    public Integer getNumberOfAttachments() {
        return numberOfAttachments;
    }

    public void setNumberOfAttachments(Integer numberOfAttachments) {
        this.numberOfAttachments = numberOfAttachments;
    }

    public Integer getNumberOfFollowers() {
        return numberOfFollowers;
    }

    public void setNumberOfFollowers(Integer numberOfFollowers) {
        this.numberOfFollowers = numberOfFollowers;
    }

    public int getUpVotes() {
        return upVotes;
    }

    public int getDownVotes() {
        return downVotes;
    }

    public UserVote getUserVoted() {
        return userVoted;
    }

    @JsonProperty("isFollower")
    public Boolean isFollower() {
        return isFollower;
    }

    @JsonProperty("isDeveloper")
    public Boolean isDeveloper() {
        return isDeveloper;
    }

    @JsonProperty("isContributor")
    public Boolean isContributor() {
        return isContributor;
    }

    public EntityContext getContext() { return context; }
    public void setContext(EntityContext context){
        this.context = context;
    }

    public static class Builder {
        private int id;
        private String description;
        private String name;
        private LocalDateTime realized;
        private int projectId;
        private LocalDateTime creationDate;
        private LocalDateTime lastUpdatedDate;
        private int upVotes;
        private int downVotes;
        private UserVote userVoted;
        private User creator;
        private User leadDeveloper;
        private Boolean isFollower;
        private Boolean isDeveloper;
        private Boolean isContributor;
        private EntityContext context;

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

        public Builder realized(LocalDateTime realized) {
            this.realized = realized;
            return this;
        }

        public Builder creationDate(LocalDateTime creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdatedDate(LocalDateTime lastUpdatedDate) {
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

        public Requirement.Builder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public Requirement.Builder leadDeveloper(User leadDeveloper) {
            this.leadDeveloper = leadDeveloper;
            return this;
        }

        public Requirement.Builder isFollower(Boolean isFollower) {
            this.isFollower = isFollower;
            return this;
        }

        public Requirement.Builder isDeveloper(Boolean isDeveloper) {
            this.isDeveloper = isDeveloper;
            return this;
        }

        public Requirement.Builder isContributor(Boolean isContributor) {
            this.isContributor = isContributor;
            return this;
        }
        public Builder context(EntityContext context){
            this.context = context;
            return this;
        }
    }
}
