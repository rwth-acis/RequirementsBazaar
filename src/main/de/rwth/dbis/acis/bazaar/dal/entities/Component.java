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
public class Component extends EntityBase {


    private final int id;

    private final String description;

    private final String name;
    private final int leaderId;
    private final int projectId;

    public int getProjectId() {
        return projectId;
    }

    public int getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public int getLeaderId() { return leaderId; }

    /**
     * Private constructor, should be called from its builder only.
     * @param builder
     */
    private Component(Builder builder) {
        this.id = builder.id;

        this.description = builder.description;

        this.name = builder.name;

        this.projectId = builder.projectId;

        this.leaderId = builder.leaderId;
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

        private int id;

        private String description;

        private String name;
        private int projectId;
        public int leaderId;

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

        public Builder leaderId(int leaderId) {
            this.leaderId = leaderId;
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

        public Builder projectId(int projectId) {
            this.projectId = projectId;
            return this;
        }
    }
}
