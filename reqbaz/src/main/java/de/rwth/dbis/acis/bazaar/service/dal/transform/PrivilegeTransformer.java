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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.PrivilegeRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.PRIVILEGE;
import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.ROLE;

/**
 * @since 2/18/2015
 */
public class PrivilegeTransformer implements Transformer<Privilege, PrivilegeRecord> {
    @Override
    public PrivilegeRecord createRecord(Privilege entity) {
        PrivilegeRecord record = new PrivilegeRecord();
        record.setId(entity.getId());
        record.setName(new PrivilegeEnumConverter().to(entity.getName()));
        return record;
    }

    @Override
    public Privilege getEntityFromTableRecord(PrivilegeRecord record) {
        return Privilege.builder()
                .name(new PrivilegeEnumConverter().from(record.getName()))
                .id(record.getId())
                .build();
    }

    @Override
    public Table<PrivilegeRecord> getTable() {
        return PRIVILEGE;
    }

    @Override
    public TableField<PrivilegeRecord, Integer> getTableId() {
        return PRIVILEGE.ID;
    }

    @Override
    public Class<? extends PrivilegeRecord> getRecordClass() {
        return PrivilegeRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Privilege entity) {
        return new HashMap<Field, Object>() {{
            put(PRIVILEGE.NAME, entity.getName());
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
        return PRIVILEGE.NAME.likeIgnoreCase(search);
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}
