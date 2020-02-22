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
 * @since 30/01/2020
 */
public class EntityContext {
    private User user;
    private Project project;
    private Category[] categories;
    private Requirement requirement;
    private Comment comment;

    public EntityContext() {
    }

    public EntityContext(Builder builder) {
        this.user           = builder.user;
        this.project        = builder.project;
        this.categories       = builder.categories;
        this.requirement    = builder.requirement;
        this.comment        = builder.comment;
    }


    public Project getProject() { return project; }

    public Category[] getCategory() {
        return categories;
    }

    public Requirement getRequirement() {
        return requirement;
    }

    public Comment getComment() {
        return comment;
    }


    public static Builder getBuilder() {
        return new Builder();
    }

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).writeValueAsString(this);
    }

    public static class Builder {
        private User user;
        private Project project;
        private Category[] categories;
        private Requirement requirement;
        private Comment comment;


        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder category(Category[] categories) {
            this.categories = categories;
            return this;
        }

        public Builder requirement(Requirement requirement) {
            this.requirement = requirement;
            return this;
        }
        public Builder attachment(User user) {
            this.user = user;
            return this;
        }

        public Builder comment(Comment comment) {
            this.comment = comment;
            return this;
        }

        public EntityContext build() {
            return new EntityContext(this);
        }
    }
}