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

package de.rwth.dbis.acis.bazaar.dal.entities;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class Project implements IdentifiedById {
    private final int id;

    private final String description;

    private final String name;
    private final ProjectVisibility visibility;

    public int getLeaderId() {
        return leaderId;
    }

    public ProjectVisibility getVisibility() {
        return visibility;
    }

    private final int leaderId;

    /**
     * Private constructor, should be called from its builder only.
     * @param builder
     */
    private Project(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.name = builder.name;

        this.visibility = builder.visibility;

        this.leaderId = builder.leaderId;
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
    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public static class Builder {

        private int id;

        private String description;

        private String name;
        private ProjectVisibility visibility;
        private int leaderId;

        public Builder(String title) {
            this.name = title;
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

        public Builder visibility(ProjectVisibility visibility) {
            this.visibility = visibility;
            return this;
        }

        public Builder leaderId(int userId) {
            this.leaderId = userId;
            return this;
        }
    }

    public enum ProjectVisibility{
        PUBLIC,PRIVATE
    }
}
