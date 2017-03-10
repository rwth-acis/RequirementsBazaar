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
public class RequirementComponent extends EntityBase {

    private final int id;
    private final int componentId;
    private final int requirementId;

    public RequirementComponent(Builder builder) {
        this.id = builder.id;
        this.componentId = builder.componentId;
        this.requirementId = builder.requirementId;
    }

    public int getId() {
        return id;
    }

    public int getComponentId() {
        return componentId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public static Builder getBuilder(int component_id) {
        return new Builder(component_id);
    }

    public static class Builder {
        private int id;
        private int componentId;
        private int requirementId;

        public Builder(int component_id) {
            this.componentId = component_id;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder componentId(int componentId) {
            this.componentId = componentId;
            return this;
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public RequirementComponent build() {
            return new RequirementComponent(this);
        }
    }
}
