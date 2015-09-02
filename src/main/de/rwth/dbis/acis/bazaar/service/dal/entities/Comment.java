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


import jodd.vtor.constraint.*;

import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public class Comment extends EntityBase {
    @Min(-1)
    private final int requirementId;
    @Min(-1)
    private final int Id;

    @NotBlank
    @MaxLength(65536)
    private final String message;

    @Min(-1)
    private int creatorId;

    private final Date creationTime;

    private final Date lastupdatedTime;

    private User creator;

    public Comment(Builder builder) {
        Id = builder.id;
        this.message = builder.message;
        this.creatorId = builder.userId;
        this.requirementId = builder.requirementId;
        this.creationTime = builder.creationTime;
        this.lastupdatedTime = builder.lastupdatedTime;
        this.creator = builder.creator;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public int getId() {
        return Id;
    }

    public String getMessage() {
        return message;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public Date getLastupdatedTime() {
        return lastupdatedTime;
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
        public Date creationTime;
        public Date lastupdatedTime;
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

        public Builder creationTime(Date creationTime) {
            this.creationTime = creationTime;
            return this;
        }

        public Builder lastupdatedTime(Date lastupdatedTime) {
            this.lastupdatedTime = lastupdatedTime;
            return this;
        }

        public Comment build() {
            return new Comment(this);
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public Builder creator(User creator) {
            this.creator = creator;
            return this;
        }
    }
}
