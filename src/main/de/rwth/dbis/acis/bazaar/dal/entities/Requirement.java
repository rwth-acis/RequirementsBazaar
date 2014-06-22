package de.rwth.dbis.acis.bazaar.dal.entities;


/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Requirement implements IdentifiedById {
    private final int id;

    private final String title;

    private final String description;
    private final int projectId;
    private final int leadDeveloperId;
    private final int creatorId;

//    private final Date creation_time;

//    public Date getCreation_time() {
//        return creation_time;
//    }

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

        this.projectId = builder.leadDeveloperId;
        this.leadDeveloperId = builder.creatorId;
        this.creatorId = builder.projectId;

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
    }
}
