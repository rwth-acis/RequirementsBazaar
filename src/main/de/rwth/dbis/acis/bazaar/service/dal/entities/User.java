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

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/11/2014
 */
public class User extends EntityBase {
    private final int Id;

    private final String firstName;

    private final String lastName;

    private final String eMail;

    private final boolean admin;

    private final long Las2peerId;

    private final String userName;

    private final String profileImage;

    public User(Builder builder) {
        Id = builder.id;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.eMail = builder.eMail;
        this.admin = builder.admin;
        Las2peerId = builder.las2peerId;
        this.userName = builder.userName;
        this.profileImage = builder.profileImage;
    }

    public int getId() {
        return Id;
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

    public boolean isAdmin() {
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
        private boolean admin;
        private long las2peerId;
        private String userName;
        private String profileImage;


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

        public Builder admin(boolean admin) {
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

        public User build() {
            return new User(this);
        }
    }
}
