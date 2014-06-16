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
 * @since 6/16/2014
 */
public class UserStory extends Attachment {

    public UserStory(Builder builder) {
        super(builder);
        this.subject = builder.subject;
        this.object = builder.object;
        this.objectDescription = builder.objectDescription;
    }

    private final String subject;
    private final String object;
    private final String objectDescription;

    public String getSubject() {
        return subject;
    }

    public String getObject() {
        return object;
    }

    public String getObjectDescription() {
        return objectDescription;
    }

    public static Builder getBuilder(){
        return new Builder();
    }

    public static class Builder extends Attachment.Builder {
        private String subject;
        private String object;
        private String objectDescription;

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder object(String object) {
            this.object = object;
            return this;
        }

        public Builder objectDescription(String objectDescription) {
            this.objectDescription = objectDescription;
            return this;
        }

        @Override
        public UserStory build() {
            return new UserStory(this);
        }
    }
}
