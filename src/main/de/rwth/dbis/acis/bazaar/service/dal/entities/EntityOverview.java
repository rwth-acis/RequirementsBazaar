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


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * @author Milan Deruelle <deruelle@dbis.rwth-aachen.de>
 * @since 22/01/2020
 */
public class EntityOverview {

    private List<Integer> projects;
    private List<Integer> categories;
    private List<Integer> requirements;
    private List<Integer> comments;

    public EntityOverview() {
    }

    public EntityOverview(Builder builder) {
        this.projects = builder.projects;
        this.categories = builder.categories;
        this.requirements = builder.requirements;
        this.comments = builder.comments;
    }


    public List<Integer> getProjects() {
        return projects;
    }

    public List<Integer> getCategories() {
        return categories;
    }

    public List<Integer> getRequirements() {
        return requirements;
    }

    public List<Integer> getComments() {
        return comments;
    }


    public static Builder getBuilder() {
        return new Builder();
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(this);
    }

    public static class Builder {
        private List<Integer> projects;
        private List<Integer> categories;
        private List<Integer> requirements;
        private List<Integer> comments;


        public Builder projects(List<Integer> projects) {
            this.projects = projects;
            return this;
        }

        public Builder categories(List<Integer> categories) {
            this.categories = categories;
            return this;
        }

        public Builder requirements(List<Integer> requirements) {
            this.requirements = requirements;
            return this;
        }

        public Builder comments(List<Integer> comments) {
            this.comments = comments;
            return this;
        }

        public EntityOverview build() {
            return new EntityOverview(this);
        }
    }
}
