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

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public abstract class Attachment {
    private final int Id;
    private final int creatorId;
    private final int requirementId;
    private final String title;

    public Attachment(Builder builder) {
        this.Id = builder.id;
        this.creatorId = builder.creatorId;
        this.requirementId = builder.requirementId;
        this.title = builder.title;
    }

    public int getId() {
        return Id;
    }

    //TODO Create real object mapping extension
    public int getCreatorId() {
        return creatorId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public static abstract class Builder {
        private int id;
        private int creatorId;
        private int requirementId;
        private String title;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder creator(int creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Builder requirementId(int requirementId) {
            this.requirementId = requirementId;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public abstract Attachment build();
    }
}
