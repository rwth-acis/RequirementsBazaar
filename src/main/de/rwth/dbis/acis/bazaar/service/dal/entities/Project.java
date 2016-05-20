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

import jodd.vtor.constraint.MaxLength;
import jodd.vtor.constraint.Min;
import jodd.vtor.constraint.NotBlank;

import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Project extends EntityBase {
    @Min(-1)
    private final int id;

    @NotBlank
    private final String description;

    @NotBlank
    @MaxLength(50)
    private final String name;

    private final ProjectVisibility visibility;

    private final Date creation_time;

    private final Date lastupdated_time;

    public int getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(int leaderId) {
        this.leaderId = leaderId;
    }

    private int leaderId;

    private User leader;

    private Integer defaultComponentId;

    public ProjectVisibility getVisibility() {
        return visibility;
    }

    public void setLeader(User leader) {
        this.leader = leader;
    }

    /**
     * Private constructor, should be called from its builder only.
     *
     * @param builder
     */
    private Project(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.name = builder.name;
        this.visibility = builder.visibility;
        this.leader = builder.leader;
        this.leaderId = builder.leaderId;
        this.defaultComponentId = builder.defaultComponentId;
        this.creation_time = builder.creation_time;
        this.lastupdated_time = builder.lastupdated_time;
    }

    /**
     * Builder to easily build Component objects
     *
     * @param name Name field will be initialized using the passed value
     * @return a builder with name returned
     */
    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    @Override
    public int getId() {
        return id;
    }

    public Date getCreation_time() {
        return creation_time;
    }

    public Date getLastupdated_time() {
        return lastupdated_time;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public User getleader() {
        return leader;
    }

    public Integer getDefaultComponentId() {
        return defaultComponentId;
    }

    public void setDefaultComponentId(Integer defaultComponentId) {
        this.defaultComponentId = defaultComponentId;
    }

    public static class Builder {

        private int id;
        private String description;
        private String name;
        private ProjectVisibility visibility;
        private User leader;
        private int leaderId;
        private Date creation_time;
        private Date lastupdated_time;
        private Integer defaultComponentId;

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

        public Builder visibility(ProjectVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder leader(User leader) {
            this.leader = leader;
            return this;
        }

        public Builder leaderId(int userId) {
            this.leaderId = userId;
            return this;
        }

        public Builder defaultComponentId(Integer defaultComponentId) {
            this.defaultComponentId = defaultComponentId;
            return this;
        }

        public Builder creationTime(Date creation_time) {
            this.creation_time = creation_time;
            return this;
        }

        public Builder lastupdatedTime(Date lastupdated_time) {
            this.lastupdated_time = lastupdated_time;
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

    public enum ProjectVisibility {
        PUBLIC("+"), PRIVATE("-");

        public String asChar() {
            return asChar;
        }

        private final String asChar;

        ProjectVisibility(String visibility) {
            this.asChar = visibility;
        }

        public static ProjectVisibility getVisibility(final String visibilityChar) {
            for (ProjectVisibility vis : ProjectVisibility.values())
                if (vis.asChar.equals(visibilityChar))
                    return vis;

            return null;
        }
    }
}
