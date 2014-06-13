package de.rwth.dbis.acis.bazaar.dal.entities;


/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Requirement  implements IdentifiedById{
    private final Integer id;

    private final String title;

    private final String description;

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

    public Integer getId() {
        return id;
    }

    private Requirement(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.title = builder.title;


    }
    /**
     * Builder to easily build Requirement objects
     * @param title Title field will be initialized using the passed value
     * @return a builder with title returned
     */
    public static Builder getBuilder(String title) {
        return new Builder(title);
    }

    public static class Builder {

        private Integer id;

        private String description;

        private String title;

        public Builder(String title) {
            this.title = title;
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
         * Call this to create a Requirement object with the values previously set in the builder.
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
    }
}
