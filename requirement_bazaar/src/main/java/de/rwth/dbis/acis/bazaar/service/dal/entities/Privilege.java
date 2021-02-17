/*
 *
 *  Copyright (c) 2015, RWTH Aachen University.
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
 * @since 2/17/2015
 */
public class Privilege extends EntityBase {

    private final int Id;

    private final PrivilegeEnum name;

    private Privilege(Builder builder) {
        Id = builder.id;
        this.name = builder.name;
    }

    @Override
    public int getId() {
        return Id;
    }

    public PrivilegeEnum getName() {
        return name;
    }

    public static Builder getBuilder(PrivilegeEnum privilege) {
        return new Builder(privilege);
    }

    public static class Builder {
        private int id;
        private PrivilegeEnum name;

        public Builder(PrivilegeEnum privilege) {
            this.name = privilege;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(PrivilegeEnum name) {
            this.name = name;
            return this;
        }

        public Privilege build() {
            return new Privilege(this);
        }
    }
}
