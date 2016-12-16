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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CommentTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
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

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments.COMMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users.USERS;

public class CommentRepositoryImpl extends RepositoryImpl<Comment, CommentsRecord> implements CommentRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public CommentRepositoryImpl(DSLContext jooq) {
        super(jooq, new CommentTransformator());
    }

    @Override
    public PaginationResult<Comment> findAllByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<Comment> result = null;
        List<Comment> comments;
        try {
            comments = new ArrayList<>();
            Users creatorUser = USERS.as("creatorUser");
            Comments childComment = COMMENTS.as("childComment");
            Users childCommentCreatorUser = USERS.as("childCommentCreatorUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMMENTS)
                    .where(COMMENTS.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(COMMENTS.fields())
                    .select(childComment.fields()).select(creatorUser.fields()).select(childCommentCreatorUser.fields()).select(idCount)
                    .from(COMMENTS)
                    .leftJoin(childComment).on(childComment.BELONGSTOCOMMENT_ID.equal(COMMENTS.ID))
                    .leftJoin(childCommentCreatorUser).on(childCommentCreatorUser.ID.equal(childComment.USER_ID))
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENTS.USER_ID))
                    .where(COMMENTS.REQUIREMENT_ID.equal(requirementId).and(COMMENTS.BELONGSTOCOMMENT_ID.isNull()))
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            Comment entry = null;
            for (Record record : queryResults) {
                if (entry == null || transformator.getEntityFromTableRecord(record.into(CommentsRecord.class)).getId() != entry.getId()) {
                    entry = convertToCommentWithUser(record, creatorUser);
                    comments.add(entry);
                }
                CommentsRecord childRecor = record.into(childComment);
                if (childRecor.getId() != null) {
                    Comment childEntry = convertToCommentWithUser(record, childComment, childCommentCreatorUser);
                    comments.add(childEntry);
                }
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, comments);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return result;
    }

    private Comment convertToCommentWithUser(Record record, Users creatorUser) {
        CommentsRecord commentsRecord = record.into(CommentsRecord.class);
        Comment entry = transformator.getEntityFromTableRecord(commentsRecord);
        UserTransformator userTransformator = new UserTransformator();
        UsersRecord usersRecord = record.into(creatorUser);
        entry.setCreator(userTransformator.getEntityFromTableRecord(usersRecord));
        return entry;
    }

    private Comment convertToCommentWithUser(Record record, Comments comment, Users creatorUser) {
        CommentsRecord commentsRecord = record.into(comment);
        Comment entry = transformator.getEntityFromTableRecord(commentsRecord);
        UserTransformator userTransformator = new UserTransformator();
        UsersRecord usersRecord = record.into(creatorUser);
        entry.setCreator(userTransformator.getEntityFromTableRecord(usersRecord));
        return entry;
    }

    @Override
    public Comment findById(int id) throws Exception {
        Comment returnComment = null;
        try {
            Users creatorUser = USERS.as("creatorUser");
            Record record = jooq.selectFrom(COMMENTS
                    .join(creatorUser).on(creatorUser.ID.equal(COMMENTS.USER_ID)))
                    .where(transformator.getTableId().equal(id))
                    .fetchOne();
            returnComment = convertToCommentWithUser(record, creatorUser);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                    ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
        return returnComment;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {

            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformator.getTable())
                    .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.ID.eq(COMMENTS.REQUIREMENT_ID))
                    .join(Projects.PROJECTS).on(Projects.PROJECTS.ID.eq(Requirements.REQUIREMENTS.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(Projects.PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }
}
