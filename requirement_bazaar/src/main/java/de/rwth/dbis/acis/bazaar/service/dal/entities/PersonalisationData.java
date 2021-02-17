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

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @since 26/11/2019
 */
public class PersonalisationData extends EntityBase {


    private int id;


    @NotNull
    @Size(min = 1, max = 50, message = "Key must have between 1 and 50 characters")
    private String key;

    @Min(value = 0)
    private int version;

    private int userId;

    @NotNull
    @Size(min = 1, max = 10000)
    private String value;

    public PersonalisationData() {

    }

    private PersonalisationData(Builder builder) {
        id = builder.id;
        key = builder.key;
        version = builder.version;
        userId = builder.userId;
        value = builder.value;
    }

    public int getId() {
        return id;
    }

    public String getKey() {
        return key;
    }
    public void setKey() {
        this.key = key;
    }

    public int getVersion() {
        return version;
    }
    public void setVersion() {
        this.version = version;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId() {
        this.userId = userId;
    }

    public String getValue(){
        return value;
    }
    public void setValue() {
        this.value = value;
    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public static class Builder {
        private int id;
        private String key;
        private int version;
        private int userId;
        private String value;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder version(int version) {
            this.version = version;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }
        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public PersonalisationData build() {
            return new PersonalisationData(this);
        }
    }
}
