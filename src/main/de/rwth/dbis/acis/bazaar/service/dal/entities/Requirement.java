package de.rwth.dbis.acis.bazaar.service.dal.entities;


import jodd.vtor.constraint.*;


import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Requirement extends EntityBase {
    @Min(-1)
    private final int id;


    @NotBlank
    @MaxLength(50)
    private final String title;


    private final Date creation_time;

    @NotBlank
    @MaxLength(255)
    private final String description;

    @Min(-1)
    private final int projectId;
    @Min(-1)
    private final int leadDeveloperId;
    @Min(-1)
    private final int creatorId;

    public Date getCreation_time() {
        return creation_time;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getLeadDeveloperId() {
        return leadDeveloperId;
    }

    public int getCreatorId() {
        return creatorId;
    }

    protected Requirement(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.title = builder.title;

        this.projectId = builder.projectId;
        this.leadDeveloperId = builder.leadDeveloperId;
        this.creatorId = builder.creatorId;
        this.creation_time = builder.creation_time;

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
        private int projectId;
        private int leadDeveloperId;
        private int creatorId;
        private Date creation_time;

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

        public Builder leadDeveloperId(int userId) {
            this.leadDeveloperId = userId;
            return this;
        }

        public Builder creatorId(int userId) {
            this.creatorId = userId;
            return this;
        }

        public Builder creationTime(Date creationTime) {
            this.creation_time = creationTime;
            return this;
        }
    }
}
