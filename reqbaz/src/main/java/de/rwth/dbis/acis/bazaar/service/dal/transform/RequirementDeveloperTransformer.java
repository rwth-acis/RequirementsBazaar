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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementDeveloperMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementDeveloper;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_DEVELOPER_MAP;

/**
 * @since 6/23/2014
 */
public class RequirementDeveloperTransformer implements Transformer<RequirementDeveloper, RequirementDeveloperMapRecord> {
    @Override
    public RequirementDeveloperMapRecord createRecord(RequirementDeveloper entity) {
        RequirementDeveloperMapRecord record = new RequirementDeveloperMapRecord();
        record.setUserId(entity.getUserId());
        record.setRequirementId(entity.getRequirementId());
        return record;
    }

    @Override
    public RequirementDeveloper getEntityFromTableRecord(RequirementDeveloperMapRecord record) {
        return RequirementDeveloper.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<RequirementDeveloperMapRecord> getTable() {
        return REQUIREMENT_DEVELOPER_MAP;
    }

    @Override
    public TableField<RequirementDeveloperMapRecord, Integer> getTableId() {
        return REQUIREMENT_DEVELOPER_MAP.ID;
    }

    @Override
    public Class<? extends RequirementDeveloperMapRecord> getRecordClass() {
        return RequirementDeveloperMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final RequirementDeveloper entity) {
        return new HashMap<Field, Object>() {{
            put(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID, entity.getRequirementId());
            put(REQUIREMENT_DEVELOPER_MAP.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(REQUIREMENT_DEVELOPER_MAP.ID.asc());
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
