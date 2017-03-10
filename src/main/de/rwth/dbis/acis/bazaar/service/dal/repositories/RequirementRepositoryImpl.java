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

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.UserVote;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.AttachmentTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachment.ATTACHMENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comment.COMMENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Component.COMPONENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement.REQUIREMENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementDeveloperMap.REQUIREMENT_DEVELOPER_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementComponentMap.REQUIREMENT_COMPONENT_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementFollowerMap.REQUIREMENT_FOLLOWER_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Project.PROJECT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User.USER;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote.VOTE;

public class RequirementRepositoryImpl extends RepositoryImpl<Requirement, RequirementRecord> implements RequirementRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public RequirementRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementTransformator());
    }

    @Override
    public PaginationResult<RequirementEx> findAllByProject(int projectId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<RequirementEx> result = null;
        List<RequirementEx> requirements;
        try {
            requirements = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(REQUIREMENT)
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT.PROJECT_ID.eq(projectId))
                    .asField("idCount");

            Field<Object> voteCount = jooq.select(DSL.count(DSL.nullif(VOTE.IS_UPVOTE, 0)))
                    .from(VOTE)
                    .where(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("voteCount");

            Field<Object> commentCount = DSL.select(DSL.count())
                    .from(COMMENT)
                    .where(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("commentCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("followerCount");

            // last activity (if possible)

            List<Record> queryResults = jooq.select(REQUIREMENT.fields())
                    .select(idCount)
                    .select(voteCount)
                    .select(commentCount)
                    .select(followerCount)
                    .from(REQUIREMENT)
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT.PROJECT_ID.eq(projectId))
                    .groupBy(REQUIREMENT.ID)
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementRecord requirementRecord = queryResult.into(REQUIREMENT);
                Requirement requirement = transformator.getEntityFromTableRecord(requirementRecord);
                requirements.add(findById(requirement.getId(), userId));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, requirements);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    private UserVote transformToUserVoted(Integer userVotedInt) {
        UserVote userVoted;
        if (userVotedInt == null)
            return UserVote.NO_VOTE;
        switch (userVotedInt) {
            case 0:
                userVoted = UserVote.DOWN_VOTE;
                break;
            case 1:
                userVoted = UserVote.UP_VOTE;
                break;
            default:
                userVoted = UserVote.NO_VOTE;
        }
        return userVoted;
    }

    @Override
    public PaginationResult<RequirementEx> findAllByComponent(int componentId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<RequirementEx> result = null;
        List<RequirementEx> requirements;
        try {
            requirements = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(REQUIREMENT)
                    .join(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.eq(componentId))
                    .asField("idCount");

            Field<Object> voteCount = jooq.select(DSL.count(DSL.nullif(VOTE.IS_UPVOTE, 0)))
                    .from(VOTE)
                    .where(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("voteCount");

            Field<Object> commentCount = jooq.select(DSL.count())
                    .from(COMMENT)
                    .where(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("commentCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("followerCount");

            List<Record> queryResults = jooq.select(REQUIREMENT.fields())
                    .select(idCount)
                    .select(voteCount)
                    .select(commentCount)
                    .select(followerCount)
                    .from(REQUIREMENT)
                    .join(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.eq(componentId))
                    .groupBy(REQUIREMENT.ID)
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementRecord requirementRecord = queryResult.into(RequirementRecord.class);
                requirements.add(findById(requirementRecord.getId(), userId));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, requirements);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {
            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformator.getTable())
                    .join(PROJECT).on(PROJECT.ID.eq(REQUIREMENT.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);
            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public RequirementEx findById(int id, int userId) throws Exception {
        RequirementEx requirementEx = null;
        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User followerUser = USER.as("followerUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User developerUser = USER.as("developerUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leadDeveloperUser = USER.as("leadDeveloperUser");
            //Users contributorUsers = Users.USERS.as("contributorUsers");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote vote = VOTE.as("vote");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote userVote = VOTE.as("userVote");

            Field<Object> commentCount = jooq.select(DSL.count())
                    .from(COMMENT)
                    .where(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("commentCount");

            Field<Object> attachmentCount = jooq.select(DSL.count())
                    .from(ATTACHMENT)
                    .where(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("attachmentCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .asField("followerCount");

            Result<Record> queryResult = jooq.select(REQUIREMENT.fields())
                    .select(commentCount)
                    .select(attachmentCount)
                    .select(followerCount)
                    .select(followerUser.fields())
                    .select(developerUser.fields())
                    .select(creatorUser.fields())
                    .select(leadDeveloperUser.fields())
                    .select(ATTACHMENT.fields())
                    .select(COMPONENT.fields())

                    .from(REQUIREMENT)
                    //.leftOuterJoin(Comments.COMMENTS).on(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))

                    .leftOuterJoin(REQUIREMENT_FOLLOWER_MAP).on(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(followerUser).on(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(followerUser.ID))

                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(developerUser).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(developerUser.ID))

                    .join(creatorUser).on(creatorUser.ID.equal(REQUIREMENT.CREATOR_ID))
                    .leftOuterJoin(leadDeveloperUser).on(leadDeveloperUser.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))

                    //.leftOuterJoin(contributorUsers).on(Attachments.ATTACHMENTS.USER_ID.equal(contributorUsers.ID))

                    .leftOuterJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(COMPONENT).on(COMPONENT.ID.equal(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID))

                    .where(transformator.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            //Filling up Requirement fields
            RequirementEx.BuilderEx builder = RequirementEx.getBuilder(queryResult.getValues(REQUIREMENT.NAME).get(0));
            builder.id(queryResult.getValues(REQUIREMENT.ID).get(0))
                    .description(queryResult.getValues(REQUIREMENT.DESCRIPTION).get(0))
                    .realized(queryResult.getValues(REQUIREMENT.REALIZED).get(0))
                    .creationTime(queryResult.getValues(REQUIREMENT.CREATION_TIME).get(0))
                    .lastupdatedTime(queryResult.getValues(REQUIREMENT.LASTUPDATED_TIME).get(0))
                    .projectId(queryResult.getValues(REQUIREMENT.PROJECT_ID).get(0))
                    .creatorId(queryResult.getValues(REQUIREMENT.CREATOR_ID).get(0));

            UserTransformator userTransformator = new UserTransformator();
            //Filling up Creator
            builder.creator(
                    userTransformator.getEntityFromQueryResult(creatorUser, queryResult)
            );

            //Filling up LeadDeveloper
            if (queryResult.getValues(leadDeveloperUser.ID).get(0) != null) {
                builder.leadDeveloper(
                        userTransformator.getEntityFromQueryResult(leadDeveloperUser, queryResult)
                );
            }

            //Filling up developers list
            List<User> devList = new ArrayList<>();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(developerUser.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                devList.add(
                        userTransformator.getEntityFromQueryResult(developerUser, records)
                );
            }
            builder.developers(devList);

            //Filling up follower list
            List<User> followers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(followerUser.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                followers.add(
                        userTransformator.getEntityFromQueryResult(followerUser, records)
                );
            }
            builder.followers(followers);

            //Filling up contributors
            /*
            List<User> contributorList = new ArrayList<>();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(contributorUsers.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                contributorList.add(
                        userTransformator.getEntityFromQueryResult(contributorUsers, records)
                );
            }
            builder.contributors(contributorList);
            */

            //Filling up attachments
            List<Attachment> attachments = new ArrayList<>();

            AttachmentTransformator attachmentTransform = new AttachmentTransformator();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(ATTACHMENT.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                AttachmentRecord record = new AttachmentRecord(
                        records.getValues(ATTACHMENT.ID).get(0),
                        records.getValues(ATTACHMENT.CREATION_TIME).get(0),
                        records.getValues(ATTACHMENT.LASTUPDATED_TIME).get(0),
                        records.getValues(ATTACHMENT.REQUIREMENT_ID).get(0),
                        records.getValues(ATTACHMENT.USER_ID).get(0),
                        records.getValues(ATTACHMENT.NAME).get(0),
                        records.getValues(ATTACHMENT.DESCRIPTION).get(0),
                        records.getValues(ATTACHMENT.MIME_TYPE).get(0),
                        records.getValues(ATTACHMENT.IDENTIFIER).get(0),
                        records.getValues(ATTACHMENT.FILE_URL).get(0)
                );
                attachments.add(
                        attachmentTransform.getEntityFromTableRecord(record)
                );
            }
            builder.attachments(attachments);

            //Filling up votes
            Result<Record> voteQueryResult = jooq.select(DSL.count(DSL.nullif(vote.IS_UPVOTE, 0)).as("upVotes"))
                    .select(DSL.count(DSL.nullif(vote.IS_UPVOTE, 1)).as("downVotes"))
                    .select(userVote.IS_UPVOTE.as("userVoted"))
                    .from(REQUIREMENT)
                    .leftOuterJoin(vote).on(vote.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(userVote).on(userVote.REQUIREMENT_ID.eq(REQUIREMENT.ID).and(userVote.USER_ID.eq(userId)))
                    .where(transformator.getTableId().equal(id))
                    .fetch();

            builder.upVotes(voteQueryResult.get(0).getValue("upVotes", Integer.class));
            builder.downVotes(voteQueryResult.get(0).getValue("downVotes", Integer.class));
            builder.userVoted(transformToUserVoted(voteQueryResult.get(0).getValue("userVoted", Integer.class)));

            requirementEx = builder.build();

            //Filling up components
            List<Component> components = new ArrayList<>();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(COMPONENT.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                components.add(
                        Component.getBuilder(records.getValues(COMPONENT.NAME).get(0))
                                .projectId(records.getValues(COMPONENT.PROJECT_ID).get(0))
                                .id(records.getValues(COMPONENT.ID).get(0))
                                .description(records.getValues(COMPONENT.DESCRIPTION).get(0))
                                .build()
                );
            }
            requirementEx.setComponents(components);

            //Filling up additional information
            requirementEx.setNumberOfComments((Integer) queryResult.getValues(commentCount).get(0));
            requirementEx.setNumberOfAttachments((Integer) queryResult.getValues(attachmentCount).get(0));
            requirementEx.setNumberOfFollowers((Integer) queryResult.getValues(followerCount).get(0));


        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return requirementEx;
    }

    @Override
    public void setRealized(int id, Timestamp realized) throws BazaarException {
        try {
            jooq.update(REQUIREMENT)
                    .set(REQUIREMENT.REALIZED, realized)
                    .set(REQUIREMENT.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
                    .where(REQUIREMENT.ID.eq(id))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public void setLeadDeveloper(int id, Integer userId) throws BazaarException {
        try {
            jooq.update(REQUIREMENT)
                    .set(REQUIREMENT.LEAD_DEVELOPER_ID, userId)
                    .set(REQUIREMENT.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
                    .where(REQUIREMENT.ID.eq(id))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public Statistic getStatisticsForRequirement(int userId, int requirementId, Timestamp timestamp) throws BazaarException {
        Statistic result = null;
        try {
            // If you want to change something here, please know what you are doing! Its SQL and even worse JOOQ :-|
            Record record1 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .select(DSL.countDistinct(COMPONENT.ID).as("numberOfComponents"))
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(REQUIREMENT)
                    .leftJoin(PROJECT).on(PROJECT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(PROJECT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(PROJECT.ID.equal(REQUIREMENT.PROJECT_ID)))
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftJoin(COMPONENT).on(COMPONENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(COMPONENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(COMPONENT.ID.equal(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID)))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(COMMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_TIME.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(REQUIREMENT.ID.eq(requirementId))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(REQUIREMENT)
                    .where(REQUIREMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.ID.eq(requirementId)))
                    .fetchOne();

            result = Statistic.getBuilder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfComponents((Integer) record1.get("numberOfComponents"))
                    .numberOfRequirements((Integer) record2.get("numberOfRequirements"))
                    .numberOfComments((Integer) record1.get("numberOfComments"))
                    .numberOfAttachments((Integer) record1.get("numberOfAttachments"))
                    .numberOfVotes((Integer) record1.get("numberOfVotes"))
                    .build();

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }
}
