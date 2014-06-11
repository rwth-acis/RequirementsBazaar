package de.rwth.dbis.acis.bazaar.dal.entities;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Component implements IdentifiedById {


    private final Integer id;

    private final String description;

    private final String name;

    public Integer getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    /**
     * Private constructor, should be called from its builder only.
     * @param builder
     */
    private Component(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.name = builder.name;
    }


    /**
     * Builder to easily build Component objects
     * @param name Name field will be initialized using the passed value
     * @return a builder with name returned
     */
    public static Builder getBuilder(String name) {
        return new Builder(name);
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
         * Call this to create a Component object with the values previously set in the builder.
         * @return initialized Component object
         */
        public Component build() {
            Component created = new Component(this);

            String name = created.getName();

            if (name == null || name.length() == 0) {
                throw new IllegalStateException("name cannot be null or empty");
            }

            return created;
        }
    }
}
