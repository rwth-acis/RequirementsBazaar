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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges.PRIVILEGES;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Roles.ROLES;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/18/2015
 */
public class PrivilegeTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> {
    @Override
    public PrivilegesRecord createRecord(Privilege entity) {
        PrivilegesRecord record = new PrivilegesRecord();
        record.setId(entity.getId());
        record.setName(new PrivilegeEnumConverter().to(entity.getName()));
        return record;
    }

    @Override
    public Privilege getEntityFromTableRecord(PrivilegesRecord record) {
        return Privilege.getBuilder(new PrivilegeEnumConverter().from(record.getName()))
                .id(record.getId())
                .build();
    }

    @Override
    public Table<PrivilegesRecord> getTable() {
        return PRIVILEGES;
    }

    @Override
    public TableField<PrivilegesRecord, Integer> getTableId() {
        return PRIVILEGES.ID;
    }

    @Override
    public Class<? extends PrivilegesRecord> getRecordClass() {
        return PrivilegesRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Privilege entity) {
        return new HashMap<Field, Object>() {{
            put(PRIVILEGES.NAME, entity.getName());
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
        return Arrays.asList(
                PRIVILEGES.NAME.likeIgnoreCase(likeExpression)
        );
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}

