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

package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.USER;

public class UserTransformer implements Transformer<User, UserRecord> {
    @Override
    public UserRecord createRecord(User entity) {
        UserRecord record = new UserRecord();
        record.setLas2peerId(entity.getLas2peerId());
        record.setAdmin(entity.getAdmin());
        record.setEmail(entity.getEMail());
        record.setFirstName(entity.getFirstName());
        record.setLastName(entity.getLastName());
        record.setUserName(entity.getUserName());
        record.setProfileImage(entity.getProfileImage());
        record.setEmailLeadSubscription((byte) (entity.isEmailLeadSubscription() ? 1 : 0));
        record.setEmailFollowSubscription((byte) (entity.isEmailFollowSubscription() ? 1 : 0));
        record.setPersonalizationEnabled((byte) (entity.isPersonalizationEnabled() ? 1:0));
        record.setCreationDate(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));

        return record;
    }

    @Override
    public User getEntityFromTableRecord(UserRecord record) {
        return User.getBuilder(record.getEmail())
                .id(record.getId())
                .admin(record.getAdmin())
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .las2peerId(record.getLas2peerId())
                .profileImage(record.getProfileImage())
                .userName(record.getUserName())
                .emailLeadSubscription(record.getEmailLeadSubscription() != 0)
                .emailFollowSubscription(record.getEmailFollowSubscription() != 0)
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .lastLoginDate(record.getLastLoginDate())
                .personalizationEnabled(record.getPersonalizationEnabled() != 0)
                .build();
    }

    public User getEntityFromQueryResult(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User user, Result<Record> queryResult) {
        return User.getBuilder(queryResult.getValues(user.EMAIL).get(0))
                .id(queryResult.getValues(user.ID).get(0))
                .admin(queryResult.getValues(user.ADMIN).get(0))
                .firstName(queryResult.getValues(user.FIRST_NAME).get(0))
                .lastName(queryResult.getValues(user.LAST_NAME).get(0))
                .las2peerId(queryResult.getValues(user.LAS2PEER_ID).get(0))
                .userName(queryResult.getValues(user.USER_NAME).get(0))
                .profileImage(queryResult.getValues(user.PROFILE_IMAGE).get(0))
                .emailLeadSubscription(queryResult.getValues(user.EMAIL_LEAD_SUBSCRIPTION).get(0) != 0)
                .emailFollowSubscription(queryResult.getValues(user.EMAIL_FOLLOW_SUBSCRIPTION).get(0) != 0)
                .personalizationEnabled(queryResult.getValues(user.PERSONALIZATION_ENABLED).get(0) != 0)
                .build();
    }

    @Override
    public Table<UserRecord> getTable() {
        return USER;
    }

    @Override
    public TableField<UserRecord, Integer> getTableId() {
        return USER.ID;
    }

    @Override
    public Class<? extends UserRecord> getRecordClass() {
        return UserRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final User entity) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entity.getEMail() != null) {
                put(USER.EMAIL, entity.getEMail());
            }
            if (entity.getFirstName() != null) {
                put(USER.FIRST_NAME, entity.getFirstName());
            }
            if (entity.getLastName() != null) {
                put(USER.LAST_NAME, entity.getLastName());
            }
            if (entity.getUserName() != null) {
                put(USER.USER_NAME, entity.getUserName());
            }
            if (entity.getProfileImage() != null) {
                put(USER.PROFILE_IMAGE, entity.getProfileImage());
            }
            if (entity.isEmailLeadSubscription() != null) {
                put(USER.EMAIL_LEAD_SUBSCRIPTION, entity.isEmailLeadSubscription());
            }
            if (entity.isEmailFollowSubscription() != null) {
                put(USER.EMAIL_FOLLOW_SUBSCRIPTION, entity.isEmailFollowSubscription());
            }
            if (entity.isPersonalizationEnabled() != null) {
                put(USER.PERSONALIZATION_ENABLED, entity.isPersonalizationEnabled());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(USER.LAST_UPDATED_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(USER.USER_NAME.asc());
        }
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        return USER.USER_NAME.likeIgnoreCase("%" + search + "%")
                .or(USER.EMAIL.likeIgnoreCase("%" + search + "%"))
                .or(USER.FIRST_NAME.likeIgnoreCase("%" + search + "%"))
                .or(USER.LAST_NAME.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}
