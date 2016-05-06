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

import jodd.vtor.constraint.Min;
import jodd.vtor.constraint.NotBlank;

public class User extends EntityBase {
    @Min(-1)
    private final int id;

    private final String firstName;

    private final String lastName;

    private transient final String eMail;

    private final Boolean admin;

    private final long Las2peerId;

    @NotBlank
    private final String userName;

    private final String profileImage;

    private final Boolean emailLeadItems;

    private final Boolean emailFollowItems;

    public User(Builder builder) {
        id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.eMail = builder.eMail;
        this.admin = builder.admin;
        Las2peerId = builder.las2peerId;
        this.userName = builder.userName;
        this.profileImage = builder.profileImage;
        this.emailLeadItems = builder.emailLeadItems;
        this.emailFollowItems = builder.emailFollowItems;
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

    public String geteMail() {
        return eMail;
    }

    public Boolean isAdmin() {
        return admin;
    }

    public long getLas2peerId() {
        return Las2peerId;
    }

    public String getUserName() {
        return userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public Boolean isEmailLeadItems() {
        return emailLeadItems;
    }

    public Boolean isEmailFollowItems() {
        return emailFollowItems;
    }

    public static Builder geBuilder(String eMail) {
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
        private long las2peerId;
        private String userName;
        private String profileImage;
        private Boolean emailLeadItems;
        private Boolean emailFollowItems;

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

        public Builder las2peerId(long userId) {
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

        public Builder emailLeadItems(Boolean emailLeadItems) {
            this.emailLeadItems = emailLeadItems;
            return this;
        }

        public Builder emailFollowItems(Boolean emailFollowItems) {
            this.emailFollowItems = emailFollowItems;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}
