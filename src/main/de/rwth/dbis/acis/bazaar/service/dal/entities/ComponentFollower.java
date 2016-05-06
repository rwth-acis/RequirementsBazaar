package de.rwth.dbis.acis.bazaar.service.dal.entities;

public class ComponentFollower extends EntityBase {
    private final int Id;
    private final int ComponentId;
    private final int UserId;

    public ComponentFollower(Builder builder) {
        Id = builder.id;
        ComponentId = builder.componentId;
        UserId = builder.userId;
    }

    public int getId() {
        return Id;
    }

    public int getComponentId() {
        return ComponentId;
    }

    public int getUserId() {
        return UserId;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        public int userId;
        public int componentId;
        public int id;

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder componentId(int componentId) {
            this.componentId = componentId;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public ComponentFollower build() {
            return new ComponentFollower(this);
        }
    }
}
