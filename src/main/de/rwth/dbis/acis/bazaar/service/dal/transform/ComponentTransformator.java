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

import com.vdurmont.emoji.EmojiParser;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollowerMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementComponentMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentRecord;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.COMPONENT;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ComponentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Component, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentRecord> {
    @Override
    public ComponentRecord createRecord(Component entry) {
        entry = this.cleanEntry(entry);

        ComponentRecord record = new ComponentRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setProjectId(entry.getProjectId());
        record.setLeaderId(entry.getLeaderId());
        record.setCreationTime(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        return record;
    }

    @Override
    public Component getEntityFromTableRecord(ComponentRecord record) {
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
    public Table<ComponentRecord> getTable() {
        return COMPONENT;
    }

    @Override
    public TableField<ComponentRecord, Integer> getTableId() {
        return COMPONENT.ID;
    }

    @Override
    public Class<? extends ComponentRecord> getRecordClass() {
        return ComponentRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Component entry) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entry.getDescription() != null) {
                put(COMPONENT.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(COMPONENT.NAME, entry.getName());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(COMPONENT.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(COMPONENT.NAME.asc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            if (sort.getField().equals("name")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(COMPONENT.NAME.asc());
                        break;
                    case DESC:
                        sortFields.add(COMPONENT.NAME.desc());
                        break;
                    default:
                        sortFields.add(COMPONENT.NAME.asc());
                        break;
                }
            } else if (sort.getField().equals("date")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(COMPONENT.CREATION_TIME.asc());
                        break;
                    case DESC:
                        sortFields.add(COMPONENT.CREATION_TIME.desc());
                        break;
                    default:
                        sortFields.add(COMPONENT.CREATION_TIME.desc());
                        break;
                }
            } else if (sort.getField().equals("requirement")) {

                Field<Object> requirementCount = DSL.select(DSL.count())
                        .from(Requirement.REQUIREMENT)
                        .leftJoin(RequirementComponentMap.REQUIREMENT_COMPONENT_MAP).on(Requirement.REQUIREMENT.ID.equal(RequirementComponentMap.REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID))
                        .where(RequirementComponentMap.REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                        .asField("requirementCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(requirementCount.asc());
                        break;
                    case DESC:
                        sortFields.add(requirementCount.desc());
                        break;
                    default:
                        sortFields.add(requirementCount.desc());
                        break;
                }
            } else if (sort.getField().equals("follower")) {

                Field<Object> followerCount = DSL.select(DSL.count())
                        .from(ComponentFollowerMap.COMPONENT_FOLLOWER_MAP)
                        .where(ComponentFollowerMap.COMPONENT_FOLLOWER_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                        .asField("followerCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(followerCount.asc());
                        break;
                    case DESC:
                        sortFields.add(followerCount.desc());
                        break;
                    default:
                        sortFields.add(followerCount.desc());
                        break;
                }
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        return COMPONENT.NAME.likeIgnoreCase("%" + search + "%")
                .or(COMPONENT.DESCRIPTION.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    public Component cleanEntry(Component component) {
        if (component.getName() != null) {
            component.setName(EmojiParser.parseToAliases(component.getName()));
        }
        if (component.getDescription() != null) {
            component.setDescription(EmojiParser.parseToAliases(component.getDescription()));
        }
        return component;
    }
}
