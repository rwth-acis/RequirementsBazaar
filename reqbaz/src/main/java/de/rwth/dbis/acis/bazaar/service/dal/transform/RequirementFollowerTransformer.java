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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementFollowerMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_FOLLOWER_MAP;

/**
 * @since 6/23/2014
 */
public class RequirementFollowerTransformer implements Transformer<RequirementFollower, RequirementFollowerMapRecord> {
    @Override
    public RequirementFollowerMapRecord createRecord(RequirementFollower entity) {
        RequirementFollowerMapRecord record = new RequirementFollowerMapRecord();
        record.setRequirementId(entity.getRequirementId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public RequirementFollower getEntityFromTableRecord(RequirementFollowerMapRecord record) {
        return RequirementFollower.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<RequirementFollowerMapRecord> getTable() {
        return REQUIREMENT_FOLLOWER_MAP;
    }

    @Override
    public TableField<RequirementFollowerMapRecord, Integer> getTableId() {
        return REQUIREMENT_FOLLOWER_MAP.ID;
    }

    @Override
    public Class<? extends RequirementFollowerMapRecord> getRecordClass() {
        return RequirementFollowerMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final RequirementFollower entity) {
        return new HashMap<>() {{
            put(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID, entity.getRequirementId());
            put(REQUIREMENT_FOLLOWER_MAP.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(REQUIREMENT_FOLLOWER_MAP.ID.asc());
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
