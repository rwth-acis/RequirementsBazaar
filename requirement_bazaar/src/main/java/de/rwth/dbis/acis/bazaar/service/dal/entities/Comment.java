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
import com.fasterxml.jackson.annotation.JsonProperty;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreateValidation;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @since 6/11/2014
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class Comment extends EntityBase implements Ownable {

    private int id;

    @NotNull(groups = CreateValidation.class)
    private String message;

    @Min(value = 0, groups = CreateValidation.class)
    private Integer replyToComment;

    @Min(value = 0, groups = CreateValidation.class)
    private int requirementId;

    private User creator;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Europe/Berlin")
    private LocalDateTime lastUpdatedDate;

    @JsonProperty("_context")
    private EntityContext context;

    @Override
    public boolean isOwner(User user) {
        return creator == user;
    }
}
