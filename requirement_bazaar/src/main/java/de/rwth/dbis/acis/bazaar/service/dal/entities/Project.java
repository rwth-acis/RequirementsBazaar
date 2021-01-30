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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jodd.vtor.constraint.MaxLength;
import jodd.vtor.constraint.NotBlank;
import jodd.vtor.constraint.NotNull;

import java.time.LocalDateTime;

/**
 * @since 6/9/2014
 */
public class Project extends EntityBase {

    private int id;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    @MaxLength(value = 50, profiles = {"*"})
    private String name;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    private String description;

    private Boolean visibility = true;

    private Integer defaultCategoryId;

    private User leader;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime lastUpdatedDate;

    private Integer numberOfCategories;
    private Integer numberOfRequirements;
    private Integer numberOfFollowers;
    private Boolean isFollower;

    public Project() {
    }

    /**
     * Private constructor, should be called from its builder only.
     *
     * @param builder
     */
    private Project(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.description = builder.description;
        this.visibility = builder.visibility;
        this.defaultCategoryId = builder.defaultCategoryId;
        this.leader = builder.leader;
        this.isFollower = builder.isFollower;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
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

    public Integer getDefaultCategoryId() {
        return defaultCategoryId;
    }

    public void setDefaultCategoryId(Integer defaultCategoryId) {
        this.defaultCategoryId = defaultCategoryId;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public Integer getNumberOfCategories() {
        return numberOfCategories;
    }

    public void setNumberOfCategories(Integer numberOfCategories) {
        this.numberOfCategories = numberOfCategories;
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

    public void setFollower(Boolean isFollower) {
        this.isFollower = isFollower;
    }

    @JsonProperty("isFollower")
    public Boolean isFollower() {
        return isFollower;
    }

    public static class Builder {

        private int id;
        private String description;
        private String name;
        private Boolean visibility;
        private User leader;
        private LocalDateTime creationDate;
        private LocalDateTime lastUpdatedDate;
        private Integer defaultCategoryId;
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

        public Builder visibility(Boolean visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder leader(User leader) {
            this.leader = leader;
            return this;
        }

        public Builder defaultCategoryId(Integer defaultCategoryId) {
            this.defaultCategoryId = defaultCategoryId;
            return this;
        }

        public Builder isFollower(Boolean isFollower) {
            this.isFollower = isFollower;
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

        /**
         * Call this to create a Project object with the values previously set in the builder.
         *
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
