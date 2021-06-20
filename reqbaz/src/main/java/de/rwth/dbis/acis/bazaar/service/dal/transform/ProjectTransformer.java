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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.ProjectRepositoryImpl;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

public class ProjectTransformer implements Transformer<Project, ProjectRecord> {

    @Override
    public ProjectRecord createRecord(Project entry) {
        ProjectRecord record = new ProjectRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setLeaderId(entry.getLeader().getId());
        record.setVisibility(entry.getVisibility());
        record.setDefaultCategoryId(entry.getDefaultCategoryId());
        record.setCreationDate(LocalDateTime.now());
        if (entry.getAdditionalProperties() != null) {
            record.setAdditionalProperties(JSONB.jsonb(entry.getAdditionalProperties().toString()));
        }
        return record;
    }

    @Override
    public Project getEntityFromTableRecord(ProjectRecord record) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Project.Builder projectBuilder = Project.builder()
                .name(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .defaultCategoryId(record.getDefaultCategoryId())
                .visibility(record.getVisibility())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate());

        try {
            return projectBuilder
                    .additionalProperties(
                            mapper.readTree(record.getAdditionalProperties().data())
                    )
                    .build();
        } catch (Exception e) {
            return projectBuilder.build();
        }
    }

    @Override
    public Table<ProjectRecord> getTable() {
        return PROJECT;
    }

    @Override
    public TableField<ProjectRecord, Integer> getTableId() {
        return PROJECT.ID;
    }

    @Override
    public Class<? extends ProjectRecord> getRecordClass() {
        return ProjectRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(Project entry) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{
            if (entry.getDescription() != null) {
                put(PROJECT.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(PROJECT.NAME, entry.getName());
            }
            if (entry.getLeader() != null) {
                put(PROJECT.LEADER_ID, entry.getLeader().getId());
            }
            if (entry.getDefaultCategoryId() != null) {
                put(PROJECT.DEFAULT_CATEGORY_ID, entry.getDefaultCategoryId());
            }
            if (entry.getVisibility() != null) {
                put(PROJECT.VISIBILITY, entry.getVisibility());
            }
            if (entry.getAdditionalProperties() != null) {
                put(PROJECT.ADDITIONAL_PROPERTIES, entry.getAdditionalProperties());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(PROJECT.LAST_UPDATED_DATE, LocalDateTime.now());
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(ProjectRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            switch (sort.getField()) {
                case "name":
                    if (sort.getSortDirection() == Pageable.SortDirection.DESC) {
                        // 50 is derived from the max length of the project name
                        sortFields.add(PROJECT.NAME.desc());
                    } else {
                        sortFields.add(PROJECT.NAME.asc());

                    }
                    break;
                case "date":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(PROJECT.CREATION_DATE.asc());
                    } else {
                        sortFields.add(PROJECT.CREATION_DATE.desc());
                    }
                    break;
                case "last_activity":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(ProjectRepositoryImpl.LAST_ACTIVITY.field("last_activity").asc());
                    } else {
                        sortFields.add(ProjectRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
                    }
                    break;
                case "requirement":

                    Field<Object> requirementCount = DSL.select(DSL.count())
                            .from(REQUIREMENT)
                            .where(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
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
                    break;
                case "follower":

                    Field<Object> followerCount = DSL.select(DSL.count())
                            .from(PROJECT_FOLLOWER_MAP)
                            .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
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
                    break;
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        return PROJECT.NAME.likeIgnoreCase("%" + search + "%")
                .or(PROJECT.DESCRIPTION.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
            if (filterEntry.getKey().equals("all")) {
                conditions.add(
                        DSL.trueCondition()
                );
            } else if (filterEntry.getKey().equals("created")) {
                conditions.add(
                        PROJECT.LEADER_ID.eq(Integer.parseInt(filterEntry.getValue()))
                );
            } else if (filterEntry.getKey().equals("following")) {
                conditions.add(
                        PROJECT.ID.in(
                                DSL.<Integer>select(PROJECT_FOLLOWER_MAP.PROJECT_ID)
                                        .from(PROJECT_FOLLOWER_MAP)
                                        .where(PROJECT_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                        )
                );
            } else {
                conditions.add(DSL.falseCondition());
            }
        }
        return conditions;
    }
}
