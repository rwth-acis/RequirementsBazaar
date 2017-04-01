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

public class Attachment extends EntityBase {
    private int id;

    private int creatorId;

    @Min(value = 0, profiles = {"create"})
    private int requirementId;

    @NotNull(profiles = {"create"})
    @NotBlank(profiles = {"*"})
    @MaxLength(value = 50, profiles = {"*"})
    
    private String name;

    private String description;

    @NotNull(profiles = {"create"})
    @NotBlank(profiles = {"*"})
    
    @MaxLength(value = 1000, profiles = {"*"})
    private String mimeType;

    @NotNull(profiles = {"create"})
    @NotBlank(profiles = {"*"})
    
    @MaxLength(value = 1000, profiles = {"*"})
    private String identifier;

    @NotNull(profiles = {"create"})
    @NotBlank(profiles = {"*"})
    @MaxLength(value = 1000, profiles = {"*"})
    private String fileUrl;

    
    private Date creationDate;

    
    private Date lastUpdatedDate;

    private User creator;

    public Attachment() {
    }

    public Attachment(Builder builder) {
       
        this.id = builder.id;
        this.creatorId = builder.creatorId;
        this.requirementId = builder.requirementId;
        
        this.name = builder.name;
        this.description = builder.description;
        this.mimeType = builder.mimeType;
        this.identifier = builder.identifier;
        this.fileUrl = builder.fileUrl;
       
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.creator = builder.creator;
    }

    public int getId() {
        
        return id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(int requirementId) {
        this.requirementId = requirementId;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFileUrl() {
        return fileUrl;
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

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private int creatorId;
        private int requirementId;

        private String name;
        private String description;
        private String mimeType;
        private String identifier;
        private String fileUrl;

        private Date creationDate;
        private Date lastUpdatedDate;
        public User creator;

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

       
        public Builder name(String name) {
            this.name = name;
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

        public Builder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public Builder fileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
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

        public Builder creator(User creator) {
            this.creator = creator;
            return this;
        }

        public Attachment build() {
            return new Attachment(this);
        }

    }
}
