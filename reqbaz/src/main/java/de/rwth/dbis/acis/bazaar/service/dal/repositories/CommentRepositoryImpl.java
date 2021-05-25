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

package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.User;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CommentRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.EntityContextFactory;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CommentTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

public class CommentRepositoryImpl extends RepositoryImpl<Comment, CommentRecord> implements CommentRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public CommentRepositoryImpl(DSLContext jooq) {
        super(jooq, new CommentTransformer());
    }


    @Override
    public PaginationResult<Comment> findAllAnswers(Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Comment> result = null;
        List<Comment> comments;
        try {
            comments = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment SUB_COMMENTS = COMMENT.as("sub_comments");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMMENT)
                    .where(
                            //               transformer.getFilterConditions(pageable.getFilters()))
                            //       .and(
                            transformer.getSearchCondition(pageable.getSearch())
                    )
                    .asField("idCount");


            List<Record> queryResults = jooq.select(COMMENT.fields())
                    .select(creatorUser.fields()).select(idCount)
                    .from(COMMENT)
                    .leftSemiJoin(SUB_COMMENTS).on((
                                    COMMENT.REPLY_TO_COMMENT_ID.eq(SUB_COMMENTS.REPLY_TO_COMMENT_ID)                //Refering same thread/base-comment
                                            .and(
                                                    COMMENT.CREATION_DATE.greaterThan(SUB_COMMENTS.CREATION_DATE)   //replies have greater timestamp than the users comment
                                            ).and(
                                            SUB_COMMENTS.USER_ID.eq(userId)                                         //Comments the user wrote
                                    )).or(
                            COMMENT.REPLY_TO_COMMENT_ID.eq(SUB_COMMENTS.ID).and(SUB_COMMENTS.USER_ID.eq(userId))
                            )
                    )
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENT.USER_ID))
                    .where(COMMENT.USER_ID.notEqual(userId))                                                        //Hide "own" answers
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            Comment entry = null;
            for (Record record : queryResults) {
                if (entry == null || transformer.getEntityFromTableRecord(record.into(CommentRecord.class)).getId() != entry.getId()) {
                    entry = convertToCommentWithUser(record, creatorUser);
                    comments.add(entry);
                }
            }
            int total = (queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount")));
            result = new PaginationResult<>(total, pageable, comments);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return result;
    }


    @Override
    public PaginationResult<Comment> findAllComments(Pageable pageable) throws BazaarException {
        PaginationResult<Comment> result = null;
        List<Comment> comments;
        try {
            comments = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Requirement requirement = REQUIREMENT.as("requirement");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Category category = CATEGORY.as("category");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Project project = PROJECT.as("project");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMMENT)
                    .where(
                            transformer.getFilterConditions(pageable.getFilters()))
                    .and(
                            transformer.getSearchCondition(pageable.getSearch())
                    )
                    .asField("idCount");


            List<Record> queryResults = jooq.select(COMMENT.fields())
                    .select(creatorUser.fields()).select(idCount).select(requirement.fields()).select(project.fields()).select(category.fields())
                    .from(COMMENT)
                    .leftOuterJoin(requirement).on(COMMENT.REQUIREMENT_ID.eq(requirement.ID))
                    .leftOuterJoin(PROJECT).on(requirement.PROJECT_ID.eq(project.ID))
                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.eq(COMMENT.REQUIREMENT_ID))
                    .leftOuterJoin(category).on(category.ID.eq(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID))
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENT.USER_ID))
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            Comment entry = null;
            for (Record record : queryResults) {
                if (entry == null || transformer.getEntityFromTableRecord(record.into(CommentRecord.class)).getId() != entry.getId()) {
                    entry = convertToCommentWithUser(record, creatorUser);
                    entry.setContext(EntityContextFactory.create(pageable.getEmbed(), record));
                    comments.add(entry);
                }
            }
            int total = (queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount")));
            result = new PaginationResult<>(total, pageable, comments);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return result;
    }


    @Override
    public List<de.rwth.dbis.acis.bazaar.service.dal.entities.Comment> findAllByRequirementId(int requirementId) throws BazaarException {
        List<Comment> comments = new ArrayList<>();
        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment childComment = COMMENT.as("childComment");
            User childCommentCreatorUser = USER.as("childCommentCreatorUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMMENT)
                    .where(COMMENT.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(COMMENT.fields())
                    .select(childComment.fields()).select(creatorUser.fields()).select(childCommentCreatorUser.fields()).select(idCount)
                    .from(COMMENT)
                    .leftJoin(childComment).on(childComment.REPLY_TO_COMMENT_ID.equal(COMMENT.ID))
                    .leftJoin(childCommentCreatorUser).on(childCommentCreatorUser.ID.equal(childComment.USER_ID))
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENT.USER_ID))
                    .where(COMMENT.REQUIREMENT_ID.equal(requirementId).and(COMMENT.REPLY_TO_COMMENT_ID.isNull()))
                    .fetch();

            Comment entry = null;
            for (Record record : queryResults) {
                if (entry == null || transformer.getEntityFromTableRecord(record.into(CommentRecord.class)).getId() != entry.getId()) {
                    entry = convertToCommentWithUser(record, creatorUser);
                    comments.add(entry);
                }
                CommentRecord childRecord = record.into(childComment);
                if (childRecord.getId() != null) {
                    Comment childEntry = convertToCommentWithUser(record, childComment, childCommentCreatorUser);
                    comments.add(childEntry);
                }
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return comments;
    }


    private Comment convertToCommentWithUser(Record record, de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser) {
        CommentRecord commentRecord = record.into(CommentRecord.class);
        Comment entry = transformer.getEntityFromTableRecord(commentRecord);
        if (!entry.getDeleted()) {
            UserTransformer userTransformer = new UserTransformer();
            UserRecord userRecord = record.into(creatorUser);
            entry.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
        }
        return entry;
    }

    private Comment convertToCommentWithUser(Record record, de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comment comment, de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser) {
        CommentRecord commentRecord = record.into(comment);
        Comment entry = transformer.getEntityFromTableRecord(commentRecord);
        UserTransformer userTransformer = new UserTransformer();
        UserRecord userRecord = record.into(creatorUser);
        entry.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
        return entry;
    }

    @Override
    public de.rwth.dbis.acis.bazaar.service.dal.entities.Comment findById(int id) throws Exception {
        Comment returnComment = null;
        try {
            User creatorUser = USER.as("creatorUser");
            Record record = jooq.selectFrom(COMMENT
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENT.USER_ID)))
                    .where(transformer.getTableId().equal(id))
                    .fetchOne();
            returnComment = convertToCommentWithUser(record, creatorUser);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                    ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
        return returnComment;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {
            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformer.getTable())
                    .join(REQUIREMENT).on(REQUIREMENT.ID.eq(COMMENT.REQUIREMENT_ID))
                    .join(PROJECT).on(PROJECT.ID.eq(REQUIREMENT.PROJECT_ID))
                    .where(transformer.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public boolean hasAnswers(int id) throws BazaarException {
        try {
            Integer answerCount = jooq.selectCount()
                    .from(COMMENT)
                    .where(COMMENT.REPLY_TO_COMMENT_ID.eq(id))
                    .fetchOne(0, int.class);

            return answerCount > 0;

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }
}
