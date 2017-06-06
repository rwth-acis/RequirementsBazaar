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
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public class RequirementCategory extends EntityBase {

    private final int id;
    private final int categoryId;
    private final int requirementId;

    public RequirementCategory(Builder builder) {
        this.id = builder.id;
        this.categoryId = builder.categoryId;
        this.requirementId = builder.requirementId;
    }

    public int getId() {
        return id;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public static Builder getBuilder(int category_id) {
        return new Builder(category_id);
    }

    public static class Builder {
        private int id;
        private int categoryId;
        private int requirementId;

        public Builder(int category_id) {
            this.categoryId = category_id;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder categoryId(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public RequirementCategory build() {
            return new RequirementCategory(this);
        }
    }
}
