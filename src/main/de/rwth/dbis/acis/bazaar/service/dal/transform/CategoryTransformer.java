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
import de.rwth.dbis.acis.bazaar.service.dal.entities.Category;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.CategoryFollowerMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementCategoryMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CategoryRecord;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.CATEGORY;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class CategoryTransformer implements Transformer<Category, CategoryRecord> {
    @Override
    public CategoryRecord createRecord(Category entry) {
        entry = this.cleanEntry(entry);

        CategoryRecord record = new CategoryRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setProjectId(entry.getProjectId());
        record.setLeaderId(entry.getLeader().getId());
        record.setCreationDate(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        return record;
    }

    @Override
    public Category getEntityFromTableRecord(CategoryRecord record) {
        return Category.getBuilder(record.getName())
                .description(record.getDescription())
                .projectId(record.getProjectId())
                .id(record.getId())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .build();
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
    public Map<Field, Object> getUpdateMap(final Category entry) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entry.getDescription() != null) {
                put(CATEGORY.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(CATEGORY.NAME, entry.getName());
            }
            if (entry.getLeader() != null) {
                put(CATEGORY.LEADER_ID, entry.getLeader().getId());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(CATEGORY.LAST_UPDATED_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(CATEGORY.NAME.asc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            if (sort.getField().equals("name")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(CATEGORY.NAME.asc());
                        break;
                    case DESC:
                        sortFields.add(CATEGORY.NAME.desc());
                        break;
                    default:
                        sortFields.add(CATEGORY.NAME.asc());
                        break;
                }
            } else if (sort.getField().equals("date")) {
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
            } else if (sort.getField().equals("requirement")) {

                Field<Object> requirementCount = DSL.select(DSL.count())
                        .from(Requirement.REQUIREMENT)
                        .leftJoin(RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP).on(Requirement.REQUIREMENT.ID.equal(RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                        .where(RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
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
                        .from(CategoryFollowerMap.CATEGORY_FOLLOWER_MAP)
                        .where(CategoryFollowerMap.CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID))
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
        return CATEGORY.NAME.likeIgnoreCase("%" + search + "%")
                .or(CATEGORY.DESCRIPTION.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    public Category cleanEntry(Category category) {
        if (category.getName() != null) {
            category.setName(EmojiParser.parseToAliases(category.getName()));
        }
        if (category.getDescription() != null) {
            category.setDescription(EmojiParser.parseToAliases(category.getDescription()));
        }
        return category;
    }
}
