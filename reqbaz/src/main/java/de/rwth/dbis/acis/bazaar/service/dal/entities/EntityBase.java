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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.SerializerViews;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

/**
 * @since 9/16/2014
 */
public abstract class EntityBase implements IdentifiedById {

    private List<Map<String, Object>> gamificationNotifications;

    public String toJSON() throws JsonProcessingException {
        return new ObjectMapper().registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithView(SerializerViews.Public.class)
                .writeValueAsString(this);
    }

    public String toPrivateJSON() throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithView(SerializerViews.Private.class)
                .writeValueAsString(this);
    }

    public void setGamificationNotifications(List<Map<String, Object>> gamificationNotifications) {
        Validate.notNull(gamificationNotifications);
        // prevent field to be sent if its empty
        this.gamificationNotifications = gamificationNotifications.size() == 0 ? null : gamificationNotifications;
    }

    public List<Map<String, Object>> getGamificationNotifications() {
        return gamificationNotifications;
    }
}
