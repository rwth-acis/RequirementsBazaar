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
public class Image extends Attachment {

    private final String filePath;
    private final String description;

    public Image(Builder builder) {
        super(builder);
        this.filePath = builder.filePath;
        this.description = builder.description;
    }

    public static Builder getBuilder(){return new Builder();}

    public static class Builder extends Attachment.Builder {
        private String filePath;
        private String description;

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        @Override
        public Image build() {
            return new Image(this);
        }
    }
}
