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

package de.rwth.dbis.acis.bazaar.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.entities.User;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UsersRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Users.USERS;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class UserTransformator implements Transformator<de.rwth.dbis.acis.bazaar.dal.entities.User,de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UsersRecord> {
    @Override
    public UsersRecord createRecord(User entity) {
        UsersRecord record = new UsersRecord();
        record.setId(entity.getId());
        record.setUserId(entity.getUserId());
        record.setAdmin((byte)(entity.getAdmin()? 1:0));
        record.setEmail(entity.geteMail());
        record.setFristName(entity.getFirstName());
        record.setLastName(entity.getLastName());
        record.setUserName(entity.getUserName());
        record.setOpenidIdentifier(entity.getOpenIdIdentifier());
        return record;
    }

    @Override
    public User mapToEntity(UsersRecord record) {
        return User.geBuilder(record.getEmail())
                .id(record.getId())
                .admin(record.getAdmin() !=0 )
                .firstName(record.getFristName())
                .lastName(record.getLastName())
                .openIdIdentifier(record.getOpenidIdentifier())
                .userId(record.getUserId())
                .openIdIdentifier(record.getOpenidIdentifier())
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
            put(USERS.ADMIN,entity.getAdmin());
            put(USERS.EMAIL,entity.geteMail());
            put(USERS.OPENID_IDENTIFIER,entity.getOpenIdIdentifier());
            put(USERS.FRIST_NAME,entity.getFirstName());
            put(USERS.LAST_NAME,entity.getLastName());
            put(USERS.USER_ID,entity.getUserId());
            put(USERS.USER_NAME,entity.getUserName());
        }};
    }
}
