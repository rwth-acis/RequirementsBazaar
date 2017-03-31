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
import jodd.vtor.constraint.NotNull;

import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public class Comment extends EntityBase {

    private int Id;

    @Min(value = 0, profiles = {"create"})
    private int requirementId;

    @NotBlank(profiles = {"*"})
    @NotNull(profiles = {"create"})
    @MaxLength(value = 1000, profiles = {"*"})
    private String message;

    private int creatorId;

    @Min(value = 0, profiles = {"create"})
    private Integer replyToComment;

    private Date creationDate;

    private Date lastUpdatedDate;

    private User creator;

    public Comment() {
    }

    public Comment(Builder builder) {
        this.Id = builder.id;
        this.message = builder.message;
        this.creatorId = builder.userId;
        this.requirementId = builder.requirementId;
        this.replyToComment = builder.replyToComment;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.creator = builder.creator;
    }

    public int getId() {
        return Id;
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

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public Integer getReplyToComment() {
        return replyToComment;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public static Builder getBuilder(String message) {
        return new Builder(message);
    }

    public static class Builder {
        private int id;
        private String message;
        private int userId;
        private int requirementId;
        private Integer replyToComment;
        public Date creationDate;
        public Date lastUpdatedDate;
        public User creator;


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

        public Builder creatorId(int creatorId) {
            this.userId = creatorId;
            return this;
        }

        public Builder replyToComment(Integer replyToComment) {
            this.replyToComment = replyToComment;
            return this;
        }

        public Builder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Builder lastUpdatedDate(Date lastUpdatedDate) {
            this.lastUpdatedDate = lastUpdatedDate;
            return this;
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public Builder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public Comment build() {
            return new Comment(this);
        }
    }
}
