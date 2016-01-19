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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import org.jooq.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users.USERS;

public class UserTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.User, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord> {
    @Override
    public UsersRecord createRecord(User entity) {
        UsersRecord record = new UsersRecord();
        record.setLas2peerId(entity.getLas2peerId());
        record.setAdmin((byte) (entity.getAdmin() ? 1 : 0));
        record.setEmail(entity.geteMail());
        record.setFirstName(entity.getFirstName());
        record.setLastName(entity.getLastName());
        record.setUserName(entity.getUserName());
        record.setProfileImage(entity.getProfileImage());
        return record;
    }

    @Override
    public User getEntityFromTableRecord(UsersRecord record) {
        return User.geBuilder(record.getEmail())
                .id(record.getId())
                .admin(record.getAdmin() != 0)
                .firstName(record.getFirstName())
                .lastName(record.getLastName())
                .las2peerId(record.getLas2peerId())
                .profileImage(record.getProfileImage())
                .userName(record.getUserName())
                .build();
    }

    public User getEntityFromQueryResult(Users user, Result<Record> queryResult) {
        return User.geBuilder(queryResult.getValues(user.EMAIL).get(0))
                .id(queryResult.getValues(user.ID).get(0))
                .admin(queryResult.getValues(user.ADMIN).get(0) != 0)
                .firstName(queryResult.getValues(user.FIRST_NAME).get(0))
                .lastName(queryResult.getValues(user.LAST_NAME).get(0))
                .las2peerId(queryResult.getValues(user.LAS2PEER_ID).get(0))
                .userName(queryResult.getValues(user.USER_NAME).get(0))
                .profileImage(queryResult.getValues(user.PROFILE_IMAGE).get(0))
                .build();
    }

    @Override
    public Table<UsersRecord> getTable() {
        return USERS;
    }

    @Override
    public TableField<UsersRecord, Integer> getTableId() {
        return USERS.ID;
    }

    @Override
    public Class<? extends UsersRecord> getRecordClass() {
        return UsersRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final User entity) {
        return new HashMap<Field, Object>() {{
            put(USERS.ADMIN, entity.getAdmin());
            put(USERS.EMAIL, entity.geteMail());
            put(USERS.FIRST_NAME, entity.getFirstName());
            put(USERS.LAST_NAME, entity.getLastName());
            put(USERS.LAS2PEER_ID, entity.getLas2peerId());
            put(USERS.USER_NAME, entity.getUserName());
            put(USERS.PROFILE_IMAGE, entity.getProfileImage());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(USERS.USER_NAME.asc());
            case ASC:
                return Arrays.asList(USERS.USER_NAME.asc());
            case DESC:
                return Arrays.asList(USERS.USER_NAME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                USERS.USER_NAME.likeIgnoreCase(likeExpression)
                        .or(USERS.EMAIL.likeIgnoreCase(likeExpression))
                        .or(USERS.FIRST_NAME.likeIgnoreCase(likeExpression))
                        .or(USERS.LAST_NAME.likeIgnoreCase(likeExpression))
        );
    }
}
