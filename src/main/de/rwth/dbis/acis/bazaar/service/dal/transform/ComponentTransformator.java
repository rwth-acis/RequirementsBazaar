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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentsRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ComponentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Component, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentsRecord> {
    @Override
    public ComponentsRecord createRecord(Component entry) {
        ComponentsRecord record = new ComponentsRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setProjectId(entry.getProjectId());
        record.setLeaderId(entry.getLeaderId());
        record.setCreationTime(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        return record;
    }

    @Override
    public Component getEntityFromTableRecord(ComponentsRecord record) {
        return Component.getBuilder(record.getName())
                .description(record.getDescription())
                .projectId(record.getProjectId())
                .id(record.getId())
                .leaderId(record.getLeaderId())
                .creationTime(record.getCreationTime())
                .lastupdated_time(record.getLastupdatedTime())
                .build();
    }

    @Override
    public Table<ComponentsRecord> getTable() {
        return COMPONENTS;
    }

    @Override
    public TableField<ComponentsRecord, Integer> getTableId() {
        return COMPONENTS.ID;
    }

    @Override
    public Class<? extends ComponentsRecord> getRecordClass() {
        return ComponentsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Component entry) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entry.getDescription() != null) {
                put(COMPONENTS.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(COMPONENTS.NAME, entry.getName());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(COMPONENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(COMPONENTS.NAME.asc());
            case ASC:
                return Arrays.asList(COMPONENTS.NAME.asc());
            case DESC:
                return Arrays.asList(COMPONENTS.NAME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(COMPONENTS.NAME.likeIgnoreCase(likeExpression)
                .or(COMPONENTS.DESCRIPTION.likeIgnoreCase(likeExpression)));
    }
}
