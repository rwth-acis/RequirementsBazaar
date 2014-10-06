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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Authorization;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AuthorizationsRecord;
import org.jooq.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Authorizations.AUTHORIZATIONS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class AuthorizationTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Authorization, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AuthorizationsRecord> {
    @Override
    public AuthorizationsRecord createRecord(Authorization entity) {
        AuthorizationsRecord record = new AuthorizationsRecord();
        record.setId(entity.getId());
        record.setAccessRight(entity.getAccessRight());
        record.setProjectId(entity.getProjectId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public Authorization mapToEntity(AuthorizationsRecord record) {
        return Authorization.getBuilder().id(record.getId())
                .accessRight(record.getAccessRight())
                .projectId(record.getProjectId())
                .userId(record.getUserId())
                .build();
    }

    @Override
    public Table<AuthorizationsRecord> getTable() {
        return AUTHORIZATIONS;
    }

    @Override
    public TableField<AuthorizationsRecord, Integer> getTableId() {
        return AUTHORIZATIONS.ID;
    }

    @Override
    public Class<? extends AuthorizationsRecord> getRecordClass() {
        return AuthorizationsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Authorization entity) {
        return new HashMap<Field, Object>() {{
            put(AUTHORIZATIONS.ACCESS_RIGHT, entity.getAccessRight());
            put(AUTHORIZATIONS.USER_ID, entity.getUserId());
            put(AUTHORIZATIONS.PROJECT_ID, entity.getProjectId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(AUTHORIZATIONS.ID.asc());
            case ASC:
                return Arrays.asList(AUTHORIZATIONS.ID.asc());
            case DESC:
                return Arrays.asList(AUTHORIZATIONS.ID.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        throw new Exception("Search is not supported!");
    }
}
