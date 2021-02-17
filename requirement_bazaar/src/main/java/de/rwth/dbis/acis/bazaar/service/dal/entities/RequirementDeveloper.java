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

/**
 * @since 6/11/2014
 */
public class RequirementDeveloper extends EntityBase {
    private final int Id;
    private final int RequirementId;
    private final int UserId;

    private RequirementDeveloper(Builder builder) {
        Id = builder.id;
        RequirementId = builder.requirementId;
        UserId = builder.userId;
    }

    public int getId() {
        return Id;
    }

    public int getRequirementId() {
        return RequirementId;
    }

    public int getUserId() {
        return UserId;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        int userId;
        int requirementId;
        int id;

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public RequirementDeveloper build() {
            return new RequirementDeveloper(this);
        }
    }
}
