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

import java.util.Date;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public class Comment {
    private final int requirementId;
    private final int Id;
    private final String message;
    private final int creatorId;
    private final Date creation_time;


    public Comment(Builder builder) {
        Id = builder.id;
        this.message = builder.message;
        this.creatorId = builder.userId;
        this.requirementId = builder.requirementId;
        this.creation_time = builder.creation_time;
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

    public static Builder getBuilder(String message){
        return new Builder(message);
    }

    public static class Builder {
        private int id;
        private String message;
        private int userId;
        private int requirementId;
        private Date creation_time;

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

        public Comment build() {
            return new Comment(this);
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public Builder creationTime(Date creationTime) {
            this.creation_time = creationTime;
            return this;
        }
    }
}
