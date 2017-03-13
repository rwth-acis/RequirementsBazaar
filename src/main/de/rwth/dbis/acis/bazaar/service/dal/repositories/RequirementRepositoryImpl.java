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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.*;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
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

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements.REQUIREMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Tags.TAGS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementFollower.REQUIREMENT_FOLLOWER;

public class RequirementRepositoryImpl extends RepositoryImpl<Requirement, RequirementsRecord> implements RequirementRepository {
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
                    .from(REQUIREMENTS)
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENTS.PROJECT_ID.eq(projectId))
                    .asField("idCount");

            Field<Object> voteCount = jooq.select(DSL.count(DSL.nullif(Votes.VOTES.IS_UPVOTE, 0)))
                    .from(Votes.VOTES)
                    .where(Votes.VOTES.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("voteCount");

            Field<Object> commentCount = DSL.select(DSL.count())
                    .from(Comments.COMMENTS)
                    .where(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("commentCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER)
                    .where(REQUIREMENT_FOLLOWER.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("followerCount");

            // last activity (if possible)

            List<Record> queryResults = jooq.select(REQUIREMENTS.fields())
                    .select(idCount)
                    .select(voteCount)
                    .select(commentCount)
                    .select(followerCount)
                    .from(REQUIREMENTS)
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENTS.PROJECT_ID.eq(projectId))
                    .groupBy(REQUIREMENTS.ID)
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementsRecord requirementsRecord = queryResult.into(REQUIREMENTS);
                Requirement requirement = transformator.getEntityFromTableRecord(requirementsRecord);
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
                    .from(REQUIREMENTS)
                    .join(TAGS).on(TAGS.REQUIREMENTS_ID.eq(REQUIREMENTS.ID))
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(TAGS.COMPONENTS_ID.eq(componentId))
                    .asField("idCount");

            Field<Object> voteCount = jooq.select(DSL.count(DSL.nullif(Votes.VOTES.IS_UPVOTE, 0)))
                    .from(Votes.VOTES)
                    .where(Votes.VOTES.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("voteCount");

            Field<Object> commentCount = jooq.select(DSL.count())
                    .from(Comments.COMMENTS)
                    .where(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("commentCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER)
                    .where(REQUIREMENT_FOLLOWER.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("followerCount");

            List<Record> queryResults = jooq.select(REQUIREMENTS.fields())
                    .select(idCount)
                    .select(voteCount)
                    .select(commentCount)
                    .select(followerCount)
                    .from(REQUIREMENTS)
                    .join(TAGS).on(TAGS.REQUIREMENTS_ID.eq(REQUIREMENTS.ID))
                    .where(transformator.getFilterConditions(pageable.getFilters()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .and(TAGS.COMPONENTS_ID.eq(componentId))
                    .groupBy(REQUIREMENTS.ID)
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementsRecord requirementsRecord = queryResult.into(RequirementsRecord.class);
                requirements.add(findById(requirementsRecord.getId(), userId));
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
                    .join(Projects.PROJECTS).on(Projects.PROJECTS.ID.eq(Requirements.REQUIREMENTS.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(Projects.PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())))
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
            Users followerUsers = Users.USERS.as("followerUsers");
            Users developerUsers = Users.USERS.as("developerUsers");
            Users creatorUser = Users.USERS.as("creatorUser");
            Users leadDeveloperUser = Users.USERS.as("leadDeveloperUser");
            //Users contributorUsers = Users.USERS.as("contributorUsers");
            Votes votes = Votes.VOTES.as("votes");
            Votes userVotes = Votes.VOTES.as("userVotes");

            Field<Object> commentCount = jooq.select(DSL.count())
                    .from(Comments.COMMENTS)
                    .where(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("commentCount");

            Field<Object> attachmentCount = jooq.select(DSL.count())
                    .from(Attachments.ATTACHMENTS)
                    .where(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("attachmentCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER)
                    .where(REQUIREMENT_FOLLOWER.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .asField("followerCount");

            Result<Record> queryResult = jooq.select(REQUIREMENTS.fields())
                    .select(commentCount)
                    .select(attachmentCount)
                    .select(followerCount)
                    .select(followerUsers.fields())
                    .select(developerUsers.fields())
                    .select(creatorUser.fields())
                    .select(leadDeveloperUser.fields())
                    .select(Attachments.ATTACHMENTS.fields())
                    .select(Components.COMPONENTS.fields())

                    .from(REQUIREMENTS)
                    //.leftOuterJoin(Comments.COMMENTS).on(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .leftOuterJoin(Attachments.ATTACHMENTS).on(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))

                    .leftOuterJoin(REQUIREMENT_FOLLOWER).on(REQUIREMENT_FOLLOWER.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .leftOuterJoin(followerUsers).on(REQUIREMENT_FOLLOWER.USER_ID.equal(followerUsers.ID))

                    .leftOuterJoin(Developers.DEVELOPERS).on(Developers.DEVELOPERS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                    .leftOuterJoin(developerUsers).on(Developers.DEVELOPERS.USER_ID.equal(developerUsers.ID))

                    .join(creatorUser).on(creatorUser.ID.equal(REQUIREMENTS.CREATOR_ID))
                    .leftOuterJoin(leadDeveloperUser).on(leadDeveloperUser.ID.equal(REQUIREMENTS.LEAD_DEVELOPER_ID))

                    //.leftOuterJoin(contributorUsers).on(Attachments.ATTACHMENTS.USER_ID.equal(contributorUsers.ID))

                    .leftOuterJoin(TAGS).on(TAGS.REQUIREMENTS_ID.equal(REQUIREMENTS.ID))
                    .leftOuterJoin(Components.COMPONENTS).on(Components.COMPONENTS.ID.equal(TAGS.COMPONENTS_ID))

                    .where(transformator.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            //Filling up Requirement fields
            RequirementEx.BuilderEx builder = RequirementEx.getBuilder(queryResult.getValues(REQUIREMENTS.TITLE).get(0));
            builder.id(queryResult.getValues(REQUIREMENTS.ID).get(0))
                    .description(queryResult.getValues(REQUIREMENTS.DESCRIPTION).get(0))
                    .realized(queryResult.getValues(REQUIREMENTS.REALIZED).get(0))
                    .creationTime(queryResult.getValues(REQUIREMENTS.CREATION_TIME).get(0))
                    .lastupdatedTime(queryResult.getValues(REQUIREMENTS.LASTUPDATED_TIME).get(0))
                    .projectId(queryResult.getValues(REQUIREMENTS.PROJECT_ID).get(0))
                    .creatorId(queryResult.getValues(REQUIREMENTS.CREATOR_ID).get(0));

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

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(developerUsers.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                devList.add(
                        userTransformator.getEntityFromQueryResult(developerUsers, records)
                );
            }
            builder.developers(devList);

            //Filling up follower list
            List<User> followers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(followerUsers.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                followers.add(
                        userTransformator.getEntityFromQueryResult(followerUsers, records)
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

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(Attachments.ATTACHMENTS.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                AttachmentsRecord record = new AttachmentsRecord(
                        records.getValues(Attachments.ATTACHMENTS.ID).get(0),
                        records.getValues(Attachments.ATTACHMENTS.CREATION_TIME).get(0),
                        records.getValues(Attachments.ATTACHMENTS.LASTUPDATED_TIME).get(0),
                        records.getValues(Attachments.ATTACHMENTS.REQUIREMENT_ID).get(0),
                        records.getValues(Attachments.ATTACHMENTS.USER_ID).get(0),
                        records.getValues(Attachments.ATTACHMENTS.TITLE).get(0),
                        records.getValues(Attachments.ATTACHMENTS.DESCRIPTION).get(0),
                        records.getValues(Attachments.ATTACHMENTS.MIME_TYPE).get(0),
                        records.getValues(Attachments.ATTACHMENTS.IDENTIFIER).get(0),
                        records.getValues(Attachments.ATTACHMENTS.FILEURL).get(0)
                );
                attachments.add(
                        attachmentTransform.getEntityFromTableRecord(record)
                );
            }
            builder.attachments(attachments);

            //Filling up votes
            Result<Record> voteQueryResult = jooq.select(DSL.count(DSL.nullif(votes.IS_UPVOTE, 0)).as("upVotes"))
                    .select(DSL.count(DSL.nullif(votes.IS_UPVOTE, 1)).as("downVotes"))
                    .select(userVotes.IS_UPVOTE.as("userVoted"))
                    .from(REQUIREMENTS)
                    .leftOuterJoin(votes).on(votes.REQUIREMENT_ID.eq(REQUIREMENTS.ID))
                    .leftOuterJoin(userVotes).on(userVotes.REQUIREMENT_ID.eq(REQUIREMENTS.ID).and(userVotes.USER_ID.eq(userId)))
                    .where(transformator.getTableId().equal(id))
                    .groupBy(userVotes.IS_UPVOTE)
                    .fetch();

            builder.upVotes(voteQueryResult.get(0).getValue("upVotes", Integer.class));
            builder.downVotes(voteQueryResult.get(0).getValue("downVotes", Integer.class));
            builder.userVoted(transformToUserVoted(voteQueryResult.get(0).getValue("userVoted", Integer.class)));

            requirementEx = builder.build();

            //Filling up components
            List<Component> components = new ArrayList<Component>();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(Components.COMPONENTS.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                components.add(
                        Component.getBuilder(records.getValues(Components.COMPONENTS.NAME).get(0))
                                .projectId(records.getValues(Components.COMPONENTS.PROJECT_ID).get(0))
                                .id(records.getValues(Components.COMPONENTS.ID).get(0))
                                .description(records.getValues(Components.COMPONENTS.DESCRIPTION).get(0))
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
            jooq.update(REQUIREMENTS)
                    .set(REQUIREMENTS.REALIZED, realized)
                    .set(REQUIREMENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
                    .where(REQUIREMENTS.ID.eq(id))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public void setLeadDeveloper(int id, Integer userId) throws BazaarException {
        try {
            jooq.update(REQUIREMENTS)
                    .set(REQUIREMENTS.LEAD_DEVELOPER_ID, userId)
                    .set(REQUIREMENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
                    .where(REQUIREMENTS.ID.eq(id))
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
                    .select(DSL.countDistinct(Projects.PROJECTS.ID).as("numberOfProjects"))
                    .select(DSL.countDistinct(Components.COMPONENTS.ID).as("numberOfComponents"))
                    .select(DSL.countDistinct(Comments.COMMENTS.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(Attachments.ATTACHMENTS.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(Votes.VOTES.ID).as("numberOfVotes"))
                    .from(REQUIREMENTS)
                    .leftJoin(Projects.PROJECTS).on(Projects.PROJECTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Projects.PROJECTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Projects.PROJECTS.ID.equal(REQUIREMENTS.PROJECT_ID)))
                    .leftJoin(Tags.TAGS).on(Tags.TAGS.REQUIREMENTS_ID.equal(REQUIREMENTS.ID))
                    .leftJoin(Components.COMPONENTS).on(Components.COMPONENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Components.COMPONENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Components.COMPONENTS.ID.equal(Tags.TAGS.COMPONENTS_ID)))
                    .leftJoin(Comments.COMMENTS).on(Comments.COMMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Comments.COMMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Comments.COMMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Attachments.ATTACHMENTS).on(Attachments.ATTACHMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Attachments.ATTACHMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Votes.VOTES).on(Votes.VOTES.CREATION_TIME.greaterOrEqual(timestamp)
                            .and(Votes.VOTES.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .where(REQUIREMENTS.ID.eq(requirementId))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(REQUIREMENTS.ID).as("numberOfRequirements"))
                    .from(REQUIREMENTS)
                    .where(REQUIREMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(REQUIREMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(REQUIREMENTS.ID.eq(requirementId)))
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
