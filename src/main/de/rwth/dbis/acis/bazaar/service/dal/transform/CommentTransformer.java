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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentRecord;
import org.jooq.*;

import java.sql.Timestamp;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.COMMENT;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class CommentTransformer implements Transformer<Comment, CommentRecord> {
    @Override
    public CommentRecord createRecord(Comment entity) {
        entity = this.cleanEntity(entity);

        CommentRecord record = new CommentRecord();
        record.setUserId(entity.getCreator().getId());
        record.setMessage(entity.getMessage());
        record.setRequirementId(entity.getRequirementId());
        record.setReplyToCommentId(entity.getReplyToComment());
        record.setCreationDate(new Timestamp(Calendar.getInstance().getTime().getTime()));
        return record;
    }

    @Override
    public Comment getEntityFromTableRecord(CommentRecord record) {
        return Comment.getBuilder(record.getMessage())
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .replyToComment(record.getReplyToCommentId())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .build();
    }

    @Override
    public Table<CommentRecord> getTable() {
        return COMMENT;
    }

    @Override
    public TableField<CommentRecord, Integer> getTableId() {
        return COMMENT.ID;
    }

    @Override
    public Class<? extends CommentRecord> getRecordClass() {
        return CommentRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Comment entity) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            put(COMMENT.REQUIREMENT_ID, entity.getRequirementId());
            put(COMMENT.MESSAGE, entity.getMessage());
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(COMMENT.LAST_UPDATED_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(COMMENT.CREATION_DATE.asc());
        }
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
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
