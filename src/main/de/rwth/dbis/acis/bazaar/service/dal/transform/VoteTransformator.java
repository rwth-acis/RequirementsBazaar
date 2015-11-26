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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.VotesRecord;
import org.jooq.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class VoteTransformator implements de.rwth.dbis.acis.bazaar.service.dal.transform.Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Vote, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.VotesRecord> {
    @Override
    public VotesRecord createRecord(Vote entity) {
        VotesRecord record = new VotesRecord();
        record.setUserId(entity.getUserId());
        record.setRequirementId(entity.getRequirementId());
        record.setIsUpvote((byte) (entity.isUpvote() ? 1 : 0));
        return record;
    }

    @Override
    public Vote getEntityFromTableRecord(VotesRecord record) {
        return Vote.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .requirementId(record.getRequirementId())
                .isUpvote(record.getIsUpvote() != 0)
                .build();
    }

    @Override
    public Vote getEntityFromRecord(Record record) {
        return null;
    }

    @Override
    public Table<VotesRecord> getTable() {
        return VOTES;
    }

    @Override
    public TableField<VotesRecord, Integer> getTableId() {
        return VOTES.ID;
    }

    @Override
    public Class<? extends VotesRecord> getRecordClass() {
        return VotesRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Vote entity) {
        return new HashMap<Field, Object>() {{
            put(VOTES.IS_UPVOTE, entity.isUpvote());
            put(VOTES.REQUIREMENT_ID, entity.getRequirementId());
            put(VOTES.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(VOTES.ID.asc());
            case ASC:
                return Arrays.asList(VOTES.ID.asc());
            case DESC:
                return Arrays.asList(VOTES.ID.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        throw new Exception("Search is not supported!");
    }
}
