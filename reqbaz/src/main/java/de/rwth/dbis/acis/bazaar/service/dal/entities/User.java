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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.SerializerViews;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder")
public class User extends EntityBase {

    private int id;

    @NotNull(message = "Username can't be null")
    @Size(min = 1, max = 1000, message = "Username must have between 1 and 1000 characters")
    private String userName;

    @Size(min = 1, max = 1000, message = "first name must have between 1 and 1000 characters")
    @JsonView(SerializerViews.Private.class)
    private String firstName;

    @Size(min = 1, max = 1000, message = "last name must have between 1 and 1000 characters")
    @JsonView(SerializerViews.Private.class)
    private String lastName;

    @NotNull(message = "eMail can't be null")
    @Size(min = 1, max = 1000, message = "eMail must have between 1 and 1000 characters")
    private transient String eMail;

    @NotNull(message = "las2peerId can't be null")
    @Size(min = 1, max = 1000, message = "las2peerId must have between 1 and 1000 characters")
    @JsonView(SerializerViews.Private.class)
    private String las2peerId;

    private String profileImage;

    @JsonView(SerializerViews.Private.class)
    private Boolean emailLeadSubscription;

    @JsonView(SerializerViews.Private.class)
    private Boolean emailFollowSubscription;

    @JsonView(SerializerViews.Private.class)
    private Boolean personalizationEnabled;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Europe/Berlin")
    @JsonView(SerializerViews.Private.class)
    private OffsetDateTime creationDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Europe/Berlin")
    @JsonView(SerializerViews.Private.class)
    private OffsetDateTime lastUpdatedDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone = "Europe/Berlin")
    @JsonView(SerializerViews.Private.class)
    private OffsetDateTime lastLoginDate;

    @JsonIgnore
    private OffsetDateTime privacyPolicy;

    @JsonIgnore
    private OffsetDateTime lastPrivacyPolicyVersion;

    @JsonView(SerializerViews.Private.class)
    public String getEMail() {
        return eMail;
    }

    @JsonView(SerializerViews.Private.class)
    public Boolean isEmailLeadSubscription() {
        return emailLeadSubscription != null && emailLeadSubscription;
    }

    @JsonView(SerializerViews.Private.class)
    public Boolean isEmailFollowSubscription() {
        return emailFollowSubscription != null && emailFollowSubscription;
    }

    @JsonView(SerializerViews.Private.class)
    public Boolean isPersonalizationEnabled() {
        return personalizationEnabled != null && personalizationEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        User other = (User) o;

        return las2peerId.equals(other.las2peerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userName, firstName, lastName, eMail, las2peerId, profileImage, emailLeadSubscription, emailFollowSubscription, personalizationEnabled, creationDate, lastUpdatedDate, lastLoginDate);
    }

    @JsonProperty("privacyPolicyAccepted")
    @JsonView(SerializerViews.Private.class)
    private Boolean privacyPolicyAccepted() {
        if (privacyPolicy == null) {
            return false;
        }
        return privacyPolicy.isAfter(lastPrivacyPolicyVersion);
    }

}
