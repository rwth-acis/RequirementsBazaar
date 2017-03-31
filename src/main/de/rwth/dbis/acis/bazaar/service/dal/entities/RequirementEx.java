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

import java.util.Date;
import java.util.List;

public class RequirementEx extends Requirement {

    public RequirementEx() {
    }

    private RequirementEx(BuilderEx builder) {
        super(builder);
        this.creator = builder.creator;
        this.leadDeveloper = builder.leadDeveloper;
        this.developers = builder.developers;
        this.followers = builder.followers;
        this.contributors = builder.contributors;
    }

    private User creator;
    private User leadDeveloper;
    private List<User> developers;
    private List<User> followers;
    private List<User> contributors;

    public User getCreator() {
        return creator;
    }

    public User getLeadDeveloper() {
        return leadDeveloper;
    }

    public List<User> getDevelopers() {
        return developers;
    }

    public List<User> getFollowers() {
        return followers;
    }

    public List<User> getContributors() {
        return contributors;
    }

    public static BuilderEx getBuilder(String name) {
        return new BuilderEx(name);
    }

    public static class BuilderEx extends Builder {

        public User creator;
        public List<User> developers;
        public List<User> followers;
        public List<User> contributors;
        private User leadDeveloper;

        public BuilderEx(String name) {
            super(name);
        }

        @Override
        public RequirementEx build() {
            return new RequirementEx(this);
        }

        public BuilderEx creator(User creator) {
            this.creator = creator;
            return this;
        }

        @Override
        public BuilderEx description(String description) {
            super.description(description);
            return this;
        }

        @Override
        public BuilderEx realized(Date realized) {
            super.realized(realized);
            return this;
        }

        @Override
        public BuilderEx id(int id) {
            super.id(id);
            return this;
        }

        @Override
        public BuilderEx projectId(int projectId) {
            super.projectId(projectId);
            return this;
        }

        @Override
        public BuilderEx creatorId(int userId) {
            super.creatorId(userId);
            return this;
        }

        @Override
        public BuilderEx creationDate(Date creationDate) {
            super.creationDate(creationDate);
            return this;
        }

        @Override
        public BuilderEx lastUpdatedDate(Date lastUpdatedDate) {
            super.lastUpdatedDate(lastUpdatedDate);
            return this;
        }

        public BuilderEx leadDeveloper(User leadDeveloper) {
            this.leadDeveloper = leadDeveloper;
            return this;
        }

        public BuilderEx developers(List<User> developers) {
            this.developers = developers;
            return this;
        }

        public BuilderEx followers(List<User> followers) {
            this.followers = followers;
            return this;
        }

        public BuilderEx contributors(List<User> contributors) {
            this.contributors = contributors;
            return this;
        }

        public BuilderEx attachments(List<Attachment> attachments) {
            this.attachments = attachments;
            return this;
        }

    }
}
