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
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CommentRecord;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.ProjectRepositoryImpl;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

/**
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
        record.setCreationDate(LocalDateTime.now());
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
            updateMap.put(COMMENT.LAST_UPDATED_DATE, LocalDateTime.now());
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(COMMENT.CREATION_DATE.asc());
        }


        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            switch (sort.getField()) {
                case "date":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(COMMENT.CREATION_DATE.asc());
                            break;
                        case DESC:
                            sortFields.add(COMMENT.CREATION_DATE.desc());
                            break;
                        default:
                            sortFields.add(COMMENT.CREATION_DATE.desc());
                            break;
                    }
                    break;
                case "last_activity":
                    switch (sort.getSortDirection()) {
                        case ASC:
                            sortFields.add(COMMENT.LAST_UPDATED_DATE.asc());
                            break;
                        case DESC:
                            sortFields.add(COMMENT.LAST_UPDATED_DATE.desc());
                            break;
                        default:
                            sortFields.add(COMMENT.LAST_UPDATED_DATE.desc());
                            break;
                    }
                    break;
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        //throw new Exception("Search is not supported!");
        if(search != "")  return COMMENT.MESSAGE.likeIgnoreCase("%" + search + "%");
        return DSL.trueCondition();
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        List<Condition> conditions = new ArrayList<>();

        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {

            if (filterEntry.getKey().equals("created")) {
                conditions.add(
                        COMMENT.USER_ID.eq(Integer.parseInt(filterEntry.getValue()))
                );
            }else

            if(filterEntry.getKey().equals("following")){
                conditions.add(
                        COMMENT.REQUIREMENT_ID.in(
                                DSL.<Integer>select(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID)
                                        .from(REQUIREMENT_FOLLOWER_MAP)
                                        .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                                .union(
                                        DSL.select(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID)
                                                .from(REQUIREMENT_CATEGORY_MAP)
                                                .join(CATEGORY_FOLLOWER_MAP)
                                                .on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(CATEGORY_FOLLOWER_MAP.CATEGORY_ID)
                                                .and(CATEGORY_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue()))))
                                ).union(
                                        DSL.<Integer>select(REQUIREMENT.ID)
                                                .from(REQUIREMENT)
                                                .join(PROJECT_FOLLOWER_MAP)
                                                .on(REQUIREMENT.PROJECT_ID.eq(PROJECT_FOLLOWER_MAP.PROJECT_ID)
                                                        .and(PROJECT_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue()))))
                                )
                        )
                );

            }else
            if(filterEntry.getKey().equals("replies")) {
                de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment SUB_COMMENTS = COMMENT.as("sub_comments");
                de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment IN_COMMENTS = COMMENT.as("in_comments");
                conditions.add(
                        COMMENT.ID.in(
                                DSL.select(IN_COMMENTS.ID)
                                        .from(IN_COMMENTS)
                                        .leftSemiJoin(SUB_COMMENTS).on(
                                        (
                                                IN_COMMENTS.REPLY_TO_COMMENT_ID.eq(SUB_COMMENTS.REPLY_TO_COMMENT_ID)                //Refering same thread/base-comment
                                                        .and(
                                                                IN_COMMENTS.CREATION_DATE.greaterThan(SUB_COMMENTS.CREATION_DATE)   //replies have greater timestamp than the users comment
                                                        ).and(
                                                        SUB_COMMENTS.USER_ID.eq(Integer.parseInt(filterEntry.getValue()))       //Comments the user wrote
                                                )
                                        ).or(
                                                IN_COMMENTS.REPLY_TO_COMMENT_ID.eq(SUB_COMMENTS.ID).and(SUB_COMMENTS.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))   //User is Thread-Owner
                                        )
                                ).where(IN_COMMENTS.USER_ID.notEqual(Integer.parseInt(filterEntry.getValue())))             // Remove Users "Own" Comments
                        )
                );
            }

/*
            if(filterEntry.getKey().equals("developing")){
                conditions.add(
                        COMMENT.REQUIREMENT_ID.in(
                                DSL.<Integer>select(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID)
                                        .from(REQUIREMENT_DEVELOPER_MAP)
                                        .where(REQUIREMENT_DEVELOPER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                                .or(REQUIREMENT.LEAD_DEVELOPER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                        )
               );


            }else
  */
            else{

                conditions.add(
                        DSL.falseCondition()
                );
            }
        }
        return conditions;
    }

    private Comment cleanEntity(Comment comment) {
        if (comment.getMessage() != null) {
            comment.setMessage(EmojiParser.parseToAliases(comment.getMessage()));
        }
        return comment;
    }
}
