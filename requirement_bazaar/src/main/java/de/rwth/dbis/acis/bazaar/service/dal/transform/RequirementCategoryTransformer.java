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

import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementCategory;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementCategoryMapRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_CATEGORY_MAP;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class RequirementCategoryTransformer implements Transformer<RequirementCategory, RequirementCategoryMapRecord> {
    @Override
    public RequirementCategoryMapRecord createRecord(RequirementCategory entity) {
        RequirementCategoryMapRecord record = new RequirementCategoryMapRecord();
        record.setCategoryId(entity.getCategoryId());
        record.setRequirementId(entity.getRequirementId());
        return record;
    }

    @Override
    public RequirementCategory getEntityFromTableRecord(RequirementCategoryMapRecord record) {
        return RequirementCategory.getBuilder(record.getCategoryId())
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<RequirementCategoryMapRecord> getTable() {
        return REQUIREMENT_CATEGORY_MAP;
    }

    @Override
    public TableField<RequirementCategoryMapRecord, Integer> getTableId() {
        return REQUIREMENT_CATEGORY_MAP.ID;
    }

    @Override
    public Class<? extends RequirementCategoryMapRecord> getRecordClass() {
        return RequirementCategoryMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final RequirementCategory entity) {
        return new HashMap<Field, Object>() {{
            put(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, entity.getCategoryId());
            put(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID, entity.getRequirementId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(REQUIREMENT_CATEGORY_MAP.ID.asc());
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
