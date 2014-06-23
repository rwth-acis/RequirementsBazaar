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

package de.rwth.dbis.acis.bazaar.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CommentsRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comments.COMMENTS;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class CommentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.dal.entities.Comment,de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CommentsRecord> {
    @Override
    public CommentsRecord createRecord(Comment entity) {
        CommentsRecord record = new CommentsRecord();
        record.setId(entity.getId());
        record.setUserId(entity.getCreatorId());
        record.setMessage(entity.getMessage());
        record.setRequirementId(entity.getRequirementId());
        return record;
    }

    @Override
    public Comment mapToEntity(CommentsRecord record) {
        return Comment.getBuilder(record.getMessage())
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .creatorId(record.getUserId())
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
        return new HashMap<Field, Object>(){{
            put(COMMENTS.REQUIREMENT_ID, entity.getRequirementId());
            put(COMMENTS.USER_ID, entity.getCreatorId());
            put(COMMENTS.MESSAGE, entity.getMessage());
        }};
    }
}
