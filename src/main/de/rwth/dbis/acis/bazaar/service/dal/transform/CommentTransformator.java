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
import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord;
import org.jooq.*;

import java.sql.Timestamp;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments.COMMENTS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class CommentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Comment, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord> {
    @Override
    public CommentsRecord createRecord(Comment entity) {
        entity = this.cleanEntity(entity);

        CommentsRecord record = new CommentsRecord();
        record.setUserId(entity.getCreatorId());
        record.setMessage(entity.getMessage());
        record.setRequirementId(entity.getRequirementId());
        record.setBelongstocommentId(entity.getBelongsToComment());
        record.setCreationTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        return record;
    }

    @Override
    public Comment getEntityFromTableRecord(CommentsRecord record) {
        return Comment.getBuilder(record.getMessage())
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .creatorId(record.getUserId())
                .belongsToComment(record.getBelongstocommentId())
                .creationTime(record.getCreationTime())
                .lastupdatedTime(record.getLastupdatedTime())
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
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            put(COMMENTS.REQUIREMENT_ID, entity.getRequirementId());
            put(COMMENTS.USER_ID, entity.getCreatorId());
            put(COMMENTS.MESSAGE, entity.getMessage());
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(COMMENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(COMMENTS.CREATION_TIME.asc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return null;
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    private Comment cleanEntity(Comment comment) {
        if (comment.getMessage() != null) {
            comment.setMessage(EmojiParser.parseToAliases(comment.getMessage()));
        }
        return comment;
    }
}
