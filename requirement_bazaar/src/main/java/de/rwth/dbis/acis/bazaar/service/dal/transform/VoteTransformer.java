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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Vote;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.VoteRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.VOTE;

/**
 * @since 6/23/2014
 */
public class VoteTransformer implements Transformer<Vote, VoteRecord> {
    @Override
    public VoteRecord createRecord(Vote entity) {
        VoteRecord record = new VoteRecord();
        record.setUserId(entity.getUserId());
        record.setRequirementId(entity.getRequirementId());
        record.setIsUpvote((byte) (entity.isUpvote() ? 1 : 0));
        return record;
    }

    @Override
    public Vote getEntityFromTableRecord(VoteRecord record) {
        return Vote.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .requirementId(record.getRequirementId())
                .isUpvote(record.getIsUpvote() != 0)
                .build();
    }

    @Override
    public Table<VoteRecord> getTable() {
        return VOTE;
    }

    @Override
    public TableField<VoteRecord, Integer> getTableId() {
        return VOTE.ID;
    }

    @Override
    public Class<? extends VoteRecord> getRecordClass() {
        return VoteRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Vote entity) {
        return new HashMap<Field, Object>() {{
            put(VOTE.IS_UPVOTE, entity.isUpvote());
            put(VOTE.REQUIREMENT_ID, entity.getRequirementId());
            put(VOTE.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(VOTE.ID.asc());
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
