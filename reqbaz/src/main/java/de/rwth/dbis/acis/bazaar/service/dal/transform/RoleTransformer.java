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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RoleRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Role;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.ROLE;

/**
 * @since 2/17/2015
 */
public class RoleTransformer implements Transformer<Role, RoleRecord> {

    @Override
    public RoleRecord createRecord(Role entity) {
        RoleRecord record = new RoleRecord();
        record.setId(entity.getId());
        record.setName(entity.getName());
        return record;
    }

    @Override
    public Role getEntityFromTableRecord(RoleRecord record) {
        return Role.builder()
                .name(record.getName())
                .id(record.getId())
                .build();
    }

    @Override
    public Table<RoleRecord> getTable() {
        return ROLE;
    }

    @Override
    public TableField<RoleRecord, Integer> getTableId() {
        return ROLE.ID;
    }

    @Override
    public Class<? extends RoleRecord> getRecordClass() {
        return RoleRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Role entity) {
        return new HashMap<Field, Object>() {{
            put(ROLE.NAME, entity.getName());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(ROLE.ID.asc());
        }
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        throw new Exception("Search is not supported!");
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}
