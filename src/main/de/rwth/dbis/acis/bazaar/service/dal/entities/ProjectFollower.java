package de.rwth.dbis.acis.bazaar.service.dal.entities;

public class ProjectFollower extends EntityBase {
    private final int Id;
    private final int ProjectId;
    private final int UserId;

    private ProjectFollower(Builder builder) {
        Id = builder.id;
        ProjectId = builder.projectId;
        UserId = builder.userId;
    }

    public int getId() {
        return Id;
    }

    public int getProjectId() {
        return ProjectId;
    }

    public int getUserId() {
        return UserId;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        int userId;
        int projectId;
        int id;

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public ProjectFollower build() {
            return new ProjectFollower(this);
        }
    }
}

