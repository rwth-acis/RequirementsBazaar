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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord;
import org.jooq.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments.COMMENTS;

import java.sql.Timestamp;
import java.util.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class CommentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Comment, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord> {
    @Override
    public CommentsRecord createRecord(Comment entity) {
        CommentsRecord record = new CommentsRecord();
//        record.setId(entity.getId());
        record.setUserId(entity.getCreatorId());
        record.setMessage(entity.getMessage());
        record.setRequirementId(entity.getRequirementId());
        record.setCreationTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
        return record;
    }

    @Override
    public Comment mapToEntity(CommentsRecord record) {
        return Comment.getBuilder(record.getMessage())
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .creatorId(record.getUserId())
                .creationTime(record.getCreationTime())
                .build();
    }

    @Override
    public Table<CommentsRecord> getTable() {
        return COMMENTS;
    }

    @Override
    public TableField<CommentsRecord, Integer> getTableId() {
        return COMMENTS.ID;
    }

    @Override
    public Class<? extends CommentsRecord> getRecordClass() {
        return CommentsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Comment entity) {
        return new HashMap<Field, Object>() {{
            put(COMMENTS.REQUIREMENT_ID, entity.getRequirementId());
            put(COMMENTS.USER_ID, entity.getCreatorId());
            put(COMMENTS.MESSAGE, entity.getMessage());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(COMMENTS.CREATION_TIME.desc());
            case ASC:
                return Arrays.asList(COMMENTS.CREATION_TIME.asc());
            case DESC:
                return Arrays.asList(COMMENTS.CREATION_TIME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return null;
    }
}
