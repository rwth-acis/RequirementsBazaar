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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreateValidation;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @since 6/11/2014
 */
public class Comment extends EntityBase {

    private int id;

    @NotNull(groups = CreateValidation.class)
    private String message;

    @Min(value = 0, groups = CreateValidation.class)
    private Integer replyToComment;

    @Min(value = 0, groups = CreateValidation.class)
    private int requirementId;

    private User creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    private LocalDateTime lastUpdatedDate;

    @JsonProperty("_context")
    private EntityContext context;


    public Comment() {
    }

    public Comment(Builder builder) {
        this.id = builder.id;
        this.message = builder.message;
        this.replyToComment = builder.replyToComment;
        this.requirementId = builder.requirementId;
        this.creator = builder.creator;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.context = builder.context;
    }

    public int getId() {
        return id;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getReplyToComment() {
        return replyToComment;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public User getCreator() {
        return creator;
    }

    public EntityContext getContext() {
        return context;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setContext(EntityContext context) {
        this.context = context;
    }

    public static Builder getBuilder(String message) {
        return new Builder(message);
    }

    public static class Builder {
        private int id;
        private String message;
        private int requirementId;
        private Integer replyToComment;
        LocalDateTime creationDate;
        LocalDateTime lastUpdatedDate;
        User creator;
        EntityContext context;
     /*   private Project project;
        private Category category;
        private Requirement requirement; */


        public Builder(String message) {
            message(message);
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder replyToComment(Integer replyToComment) {
            this.replyToComment = replyToComment;
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

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }
/*
        public Builder requirement(Requirement requirement) {
            this.requirement = requirement;
            return this;
        }

        public Builder project(Project project) {
            this.project = project;
            return this;
        }

        public Builder category(Category category) {
            this.requirementId = requirementId;
            return this;
        } */


        public Builder creator(User creator) {
            this.creator = creator;
            return this;
        }
        public Builder context(EntityContext context) {
            this.context = context;
            return this;
        }

        public Comment build() {
            return new Comment(this);
        }
    }
}
