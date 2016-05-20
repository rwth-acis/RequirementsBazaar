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

import jodd.vtor.constraint.Min;

import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/17/2015
 */
public class Role extends EntityBase {

    @Min(-1)
    private final int Id;

    private final List<Privilege> privileges;


    private final String name;

    public Role(Builder builder) {
        Id = builder.id;
        this.privileges = builder.privileges;

        this.name = builder.name;
    }

    @Override
    public int getId() {
        return Id;
    }

    public List<Privilege> getPrivileges() {
        return privileges;
    }


    public String getName() {
        return name;
    }

    public static Builder getBuilder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private String name;
        private int id;
        private List<Privilege> privileges;


        public Builder(String name) {
            this.name = name;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder privileges(List<Privilege> privileges) {
            this.privileges = privileges;
            return this;
        }

        public Role build() {
            return new Role(this);
        }
    }
}
