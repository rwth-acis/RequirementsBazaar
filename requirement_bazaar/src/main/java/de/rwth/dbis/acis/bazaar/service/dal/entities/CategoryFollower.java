package de.rwth.dbis.acis.bazaar.service.dal.entities;

public class CategoryFollower extends EntityBase {
    private final int Id;
    private final int CategoryId;
    private final int UserId;

    private CategoryFollower(Builder builder) {
        Id = builder.id;
        CategoryId = builder.categoryId;
        UserId = builder.userId;
    }

    public int getId() {
        return Id;
    }

    public int getCategoryId() {
        return CategoryId;
    }

    public int getUserId() {
        return UserId;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        int userId;
        int categoryId;
        int id;

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder categoryId(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public CategoryFollower build() {
            return new CategoryFollower(this);
        }
    }
}
