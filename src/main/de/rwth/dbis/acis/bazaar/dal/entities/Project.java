package de.rwth.dbis.acis.bazaar.dal.entities;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Project implements IdentifiedById {
    private final Integer id;

    private final String description;

    private final String name;

    /**
     * Private constructor, should be called from its builder only.
     * @param builder
     */
    private Project(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.name = builder.name;
    }

    /**
     * Builder to easily build Component objects
     * @param title Title field will be initialized using the passed value
     * @return a builder with title returned
     */
    public static Builder getBuilder(String title) {
        return new Builder(title);
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public static class Builder {

        private Integer id;

        private String description;

        private String name;

        public Builder(String title) {
            this.name = title;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        /**
         * Call this to create a Project object with the values previously set in the builder.
         * @return initialized Project object
         */
        public Project build() {
            Project created = new Project(this);

            String name = created.getName();

            if (name == null || name.length() == 0) {
                throw new IllegalStateException("name cannot be null or empty");
            }

            return created;
        }
    }

}
