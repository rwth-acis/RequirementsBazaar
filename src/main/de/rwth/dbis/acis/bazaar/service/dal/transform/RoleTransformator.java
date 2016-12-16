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

package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Role;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RolesRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Roles.ROLES;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/17/2015
 */
public class RoleTransformator implements Transformator<Role, RolesRecord> {

    @Override
    public RolesRecord createRecord(Role entity) {
        RolesRecord record = new RolesRecord();
        record.setId(entity.getId());
        record.setName(entity.getName());
        return record;
    }

    @Override
    public Role getEntityFromTableRecord(RolesRecord record) {
        return Role.getBuilder(record.getName())
                .id(record.getId())
                .build();
    }

    @Override
    public Table<RolesRecord> getTable() {
        return ROLES;
    }

    @Override
    public TableField<RolesRecord, Integer> getTableId() {
        return ROLES.ID;
    }

    @Override
    public Class<? extends RolesRecord> getRecordClass() {
        return RolesRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Role entity) {
        return new HashMap<Field, Object>() {{
            put(ROLES.NAME, entity.getName());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(ROLES.ID.asc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return null;
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}