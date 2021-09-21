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
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CategoryRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Category;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.CategoryRepositoryImpl;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.CATEGORY;
import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.CATEGORY_FOLLOWER_MAP;

/**
 * @since 6/9/2014
 */
public class CategoryTransformer implements Transformer<Category, CategoryRecord> {
    @Override
    public CategoryRecord createRecord(Category entry) {
        CategoryRecord record = new CategoryRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setProjectId(entry.getProjectId());
        record.setLeaderId(entry.getCreator().getId());
        record.setCreationDate(OffsetDateTime.now());
        if (entry.getAdditionalProperties() != null) {
            record.setAdditionalProperties(JSONB.jsonb(entry.getAdditionalProperties().toString()));
        }
        return record;
    }

    @Override
    public Category getEntityFromTableRecord(CategoryRecord record) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Category.Builder categoryBuilder = Category.builder()
                .name(record.getName())
                .description(record.getDescription())
                .projectId(record.getProjectId())
                .id(record.getId())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate());

        try {
            return categoryBuilder
                    .additionalProperties(
                            mapper.readTree(record.getAdditionalProperties().data())
                    )
                    .build();
        } catch (Exception e) {
            return categoryBuilder.build();
        }
    }

    @Override
    public Table<CategoryRecord> getTable() {
        return CATEGORY;
    }

    @Override
    public TableField<CategoryRecord, Integer> getTableId() {
        return CATEGORY.ID;
    }

    @Override
    public Class<? extends CategoryRecord> getRecordClass() {
        return CategoryRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(Category entry) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{
            if (entry.getDescription() != null) {
                put(CATEGORY.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(CATEGORY.NAME, entry.getName());
            }
            if (entry.getCreator() != null) {
                put(CATEGORY.LEADER_ID, entry.getCreator().getId());
            }
            if (entry.getAdditionalProperties() != null) {
                put(CATEGORY.ADDITIONAL_PROPERTIES, entry.getAdditionalProperties());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(CATEGORY.LAST_UPDATED_DATE, OffsetDateTime.now());
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(CATEGORY.NAME.asc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            switch (sort.getField()) {
                case "name":
                    switch (sort.getSortDirection()) {
                        case DESC:
                            // 50 is derived from the name max length
                            sortFields.add(CATEGORY.NAME.desc());
                            break;
                        default:
                            sortFields.add(CATEGORY.NAME.asc());
                            break;
                    }
                    break;
                case "date":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(CATEGORY.CREATION_DATE.asc());
                            break;
                        case DESC:
                            sortFields.add(CATEGORY.CREATION_DATE.desc());
                            break;
                        default:
                            sortFields.add(CATEGORY.CREATION_DATE.desc());
                            break;
                    }
                    break;
                case "last_activity":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(CategoryRepositoryImpl.LAST_ACTIVITY.field("last_activity").asc());
                            break;
                        case DESC:
                            sortFields.add(CategoryRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
                            break;
                        default:
                            sortFields.add(CategoryRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
                            break;
                    }
                    break;
                case "requirement":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(CategoryRepositoryImpl.REQUIREMENT_COUNT.asc());
                            break;
                        case DESC:
                            sortFields.add(CategoryRepositoryImpl.REQUIREMENT_COUNT.desc());
                            break;
                        default:
                            sortFields.add(CategoryRepositoryImpl.REQUIREMENT_COUNT.desc());
                            break;
                    }
                    break;
                case "follower":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(CategoryRepositoryImpl.FOLLOWER_COUNT.asc());
                            break;
                        case DESC:
                            sortFields.add(CategoryRepositoryImpl.FOLLOWER_COUNT.desc());
                            break;
                        default:
                            sortFields.add(CategoryRepositoryImpl.FOLLOWER_COUNT.desc());
                            break;
                    }
                    break;
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        return CATEGORY.NAME.likeIgnoreCase("%" + search + "%")
                .or(CATEGORY.DESCRIPTION.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {


            if (filterEntry.getKey().equals("created")) {
                conditions.add(
                        CATEGORY.LEADER_ID.eq(Integer.parseInt(filterEntry.getValue()))
                );
            } else if (filterEntry.getKey().equals("following")) {
                conditions.add(
                        CATEGORY.ID.in(
                                DSL.<Integer>select(CATEGORY_FOLLOWER_MAP.CATEGORY_ID)
                                        .from(CATEGORY_FOLLOWER_MAP)
                                        .where(CATEGORY_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                        )
                );
            } else {
                conditions.add(DSL.falseCondition());
            }
        }
        return conditions;
    }
}
