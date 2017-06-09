/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package de.rwth.dbis.acis.bazaar.service.dal.entities;


import com.fasterxml.jackson.annotation.JsonProperty;
import jodd.vtor.constraint.MaxLength;
import jodd.vtor.constraint.Min;
import jodd.vtor.constraint.NotBlank;
import jodd.vtor.constraint.NotNull;

import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Category extends EntityBase {

    private int id;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    @MaxLength(value= 50, profiles = {"*"})
    private String name;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    private String description;

    @Min(value= 0, profiles = {"create"})
    private int projectId;

    private User leader;

    private Date creationDate;

    private Date lastUpdatedDate;

    private Integer numberOfRequirements;
    private Integer numberOfFollowers;
    private Boolean isFollower;

    public Category() {
    }

    /**
     * Private constructor, should be called from its builder only.
     *
     * @param builder
     */
    private Category(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.projectId = builder.projectId;
        this.leader = builder.leader;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.isFollower = builder.isFollower;
    }

    /**
     * Builder to easily build Category objects
     *
     * @param name Name field will be initialized using the passed value
     * @return a builder with name returned
     */
    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getLeader() {
        return leader;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    public void setFollower(Boolean follower) {
        isFollower = follower;
    }

    public Integer getNumberOfRequirements() {
        return numberOfRequirements;
    }

    public void setNumberOfRequirements(Integer numberOfRequirements) {
        this.numberOfRequirements = numberOfRequirements;
    }

    public Integer getNumberOfFollowers() {
        return numberOfFollowers;
    }

    public void setNumberOfFollowers(Integer numberOfFollowers) {
        this.numberOfFollowers = numberOfFollowers;
    }

    @JsonProperty("isFollower")
    public Boolean isFollower() {
        return isFollower;
    }

    public static class Builder {

        public User leader;
        private int id;
        private String description;
        private String name;
        private Date creationDate;
        private Date lastUpdatedDate;
        private int projectId;
        private Boolean isFollower;

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

        public Builder leader(User leader) {
            this.leader = leader;
            return this;
        }

        public Builder isFollower(Boolean isFollower) {
            this.isFollower = isFollower;
            return this;
        }

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdatedDate(Date lastUpdatedDate) {
            this.lastUpdatedDate = lastUpdatedDate;
            return this;
        }

        /**
         * Call this to create a Category object with the values previously set in the builder.
         *
         * @return initialized Category object
         */
        public Category build() {
            Category created = new Category(this);

            String name = created.getName();

            if (name == null || name.length() == 0) {
                throw new IllegalStateException("name cannot be null or empty");
            }

            return created;
        }
    }
}
