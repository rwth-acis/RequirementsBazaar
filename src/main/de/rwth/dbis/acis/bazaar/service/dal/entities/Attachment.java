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

import java.util.Date;

public abstract class Attachment extends EntityBase {
    @Min(-1)
    private final int Id;

    @Min(-1)
    private final int creatorId;

    @Min(-1)
    private final int requirementId;

    @NotBlank
    @MaxLength(100)
    private final String title;

    private final String description;

    @NotBlank
    @MaxLength(255)
    private final String mimeType;

    private final Date creation_time;

    private final Date lastupdated_time;

    public Attachment(Builder builder) {
        this.Id = builder.id;
        this.creatorId = builder.creatorId;
        this.requirementId = builder.requirementId;
        this.title = builder.title;
        this.description = builder.description;
        this.mimeType = builder.mimeType;
        this.creation_time = builder.creation_time;
        this.lastupdated_time = builder.lastupdated_time;
    }

    public int getId() {
        return Id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Date getCreation_time() {
        return creation_time;
    }

    public Date getLastupdated_time() {
        return lastupdated_time;
    }

    public static abstract class Builder {
        private int id;
        private int creatorId;
        private int requirementId;
        private String title;
        private String description;
        private String mimeType;
        private Date creation_time;
        private Date lastupdated_time;

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

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder creationTime(Date creationTime) {
            this.creation_time = creationTime;
            return this;
        }

        public Builder lastupdatedTime(Date lastupdated_time) {
            this.lastupdated_time = lastupdated_time;
            return this;
        }

        public abstract Attachment build();
    }
}
