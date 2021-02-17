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
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

public class User extends EntityBase {

    private int id;

    @NotNull(message = "Username can't be null")
    @Size(min = 1, max = 1000, message = "Username must have between 1 and 1000 characters")
    private String userName;

    @Size(min = 1, max = 1000, message = "first name must have between 1 and 1000 characters")
    private String firstName;

    @Size(min = 1, max = 1000, message = "last name must have between 1 and 1000 characters")
    private String lastName;

    @NotNull(message = "eMail can't be null")
    @Size(min = 1, max = 1000, message = "eMail must have between 1 and 1000 characters")
    private transient String eMail;

    private Boolean admin;

    @NotNull(message = "las2peerId can't be null")
    @Size(min = 1, max = 1000, message = "las2peerId must have between 1 and 1000 characters")
    private String las2peerId;

    private String profileImage;

    private Boolean emailLeadSubscription;

    private Boolean emailFollowSubscription;

    private Boolean personalizationEnabled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    private LocalDateTime lastUpdatedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone="Europe/Berlin")
    private LocalDateTime lastLoginDate;

    public User() {
    }

    public User(Builder builder) {
        this.id = builder.id;
        this.userName = builder.userName;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.eMail = builder.eMail;
        this.admin = builder.admin;
        this.las2peerId = builder.las2peerId;
        this.profileImage = builder.profileImage;
        this.emailLeadSubscription = builder.emailLeadSubscription;
        this.emailFollowSubscription = builder.emailFollowSubscription;
        this.creationDate = builder.creationDate;
        this.lastUpdatedDate = builder.lastUpdatedDate;
        this.lastLoginDate = builder.lastLoginDate;
        this.personalizationEnabled = builder.personalizationEnabled;
    }

    public int getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    @JsonIgnore
    public String getEMail() {
        return eMail;
    }

    public Boolean isAdmin() {
        return admin;
    }

    public String getLas2peerId() {
        return las2peerId;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public Boolean isEmailLeadSubscription() {
        return emailLeadSubscription != null && emailLeadSubscription;
    }

    public Boolean isEmailFollowSubscription() {
        return emailFollowSubscription != null && emailFollowSubscription;
    }

    public Boolean isPersonalizationEnabled() {
        return personalizationEnabled != null && personalizationEnabled;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public LocalDateTime getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public static Builder getBuilder(String eMail) {
        return new Builder(eMail);
    }

    public boolean getAdmin() {
        return admin;
    }



    public static class Builder {
        private int id;
        private String firstName;
        private String lastName;
        private String eMail;
        private Boolean admin;
        private String las2peerId;
        private String userName;
        private String profileImage;
        private Boolean emailLeadSubscription;
        private Boolean emailFollowSubscription;
        private Boolean personalizationEnabled;
        private LocalDateTime creationDate;
        private LocalDateTime lastUpdatedDate;
        private LocalDateTime lastLoginDate;


        public Builder(String eMail) {
            this.eMail = eMail;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder eMail(String eMail) {
            this.eMail = eMail;
            return this;
        }

        public Builder admin(Boolean admin) {
            this.admin = admin;
            return this;
        }

        public Builder las2peerId(String userId) {
            this.las2peerId = userId;
            return this;
        }

        public Builder userName(String userName) {
            this.userName = userName;
            return this;
        }

        public Builder profileImage(String profileImage) {
            this.profileImage = profileImage;
            return this;
        }

        public Builder emailLeadSubscription(Boolean emailLeadSubscription) {
            this.emailLeadSubscription = emailLeadSubscription;
            return this;
        }

        public Builder emailFollowSubscription(Boolean emailFollowSubscription) {
            this.emailFollowSubscription = emailFollowSubscription;
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

        public Builder lastLoginDate(LocalDateTime lastLoginDate) {
            this.lastLoginDate = lastLoginDate;
            return this;
        }
        public Builder personalizationEnabled(Boolean personalizationEnabled){
            this.personalizationEnabled = personalizationEnabled;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
