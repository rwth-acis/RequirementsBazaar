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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static org.jooq.impl.DSL.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.*;

public class RequirementRepositoryImpl extends RepositoryImpl<Requirement, RequirementRecord> implements RequirementRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public RequirementRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementTransformer());
    }

    @Override
    public PaginationResult<Requirement> findAllByProject(int projectId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Requirement> result = null;
        List<Requirement> requirements;
        try {
            requirements = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(REQUIREMENT)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
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

            List<Record> queryResults = jooq.select(REQUIREMENT.fields())
                    .select(idCount)
                    .select(voteCount)
                    .select(commentCount)
                    .select(followerCount)
                    .from(REQUIREMENT)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT.PROJECT_ID.eq(projectId))
                    .groupBy(REQUIREMENT.ID)
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementRecord requirementRecord = queryResult.into(REQUIREMENT);
                Requirement requirement = transformer.getEntityFromTableRecord(requirementRecord);
                requirements.add(findById(requirement.getId(), userId)); // TODO: Remove the getId call and create the objects themself here
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
    public PaginationResult<Requirement> findAllByCategory(int categoryId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Requirement> result = null;
        List<Requirement> requirements;
        try {
            requirements = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(REQUIREMENT)
                    .join(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(categoryId))
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
                    .join(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .and(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(categoryId))
                    .groupBy(REQUIREMENT.ID)
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                RequirementRecord requirementRecord = queryResult.into(RequirementRecord.class);
                requirements.add(findById(requirementRecord.getId(), userId)); // TODO: Remove the getId call and create the objects themself here
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
                    .from(transformer.getTable())
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
    public Requirement findById(int id, int userId) throws Exception {
        Requirement requirement = null;
        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leadDeveloperUser = USER.as("leadDeveloperUser");
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

            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID).and(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            Field<Object> isDeveloper = DSL.select(DSL.count())
                    .from(REQUIREMENT_DEVELOPER_MAP)
                    .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID).and(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(userId)))
                    .asField("isDeveloper");

            // Contributors = {Creator, Lead Developer, Developers, Comments creators,  Attachments creators}
            // This code could be improved so that not only "1" or "0" will return but how much contributions an user made
            // I tried this for 2-3 hours. SQL ... yeah ... I leave this to someone else. :->
            // TODO: Try the first idea from here: http://stackoverflow.com/questions/43717672/sum-over-multiple-count-field/43721212?noredirect=1#comment74498115_43721212
            Field<Object> isContributor = select(sum(choose()
                    .when(REQUIREMENT.CREATOR_ID.eq(userId), inline(1))
                    .when(REQUIREMENT.LEAD_DEVELOPER_ID.eq(userId), inline(1))
                    .when(REQUIREMENT_DEVELOPER_MAP.USER_ID.eq(userId), inline(1))
                    .when(COMMENT.USER_ID.eq(userId), inline(1))
                    .when(ATTACHMENT.USER_ID.eq(userId), inline(1))
                    .otherwise(inline(0))
            ))
                    .from(REQUIREMENT)
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(COMMENT).on(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .where(REQUIREMENT.ID.equal(id))
                    .asField("isContributor");

            Result<Record> queryResult = jooq.select(REQUIREMENT.fields())
                    .select(commentCount)
                    .select(attachmentCount)
                    .select(followerCount)
                    .select(isFollower)
                    .select(isDeveloper)
                    .select(isContributor)
                    .select(creatorUser.fields())
                    .select(leadDeveloperUser.fields())
                    .select(CATEGORY.fields())
                    .from(REQUIREMENT)
                    .join(creatorUser).on(creatorUser.ID.equal(REQUIREMENT.CREATOR_ID))
                    .leftOuterJoin(leadDeveloperUser).on(leadDeveloperUser.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))
                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftOuterJoin(CATEGORY).on(CATEGORY.ID.equal(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID))
                    .where(transformer.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            //Filling up Requirement fields
            Requirement.Builder builder = Requirement.getBuilder(queryResult.getValues(REQUIREMENT.NAME).get(0));
            builder.id(queryResult.getValues(REQUIREMENT.ID).get(0))
                    .description(queryResult.getValues(REQUIREMENT.DESCRIPTION).get(0))
                    .realized(queryResult.getValues(REQUIREMENT.REALIZED).get(0))
                    .creationDate(queryResult.getValues(REQUIREMENT.CREATION_DATE).get(0))
                    .lastUpdatedDate(queryResult.getValues(REQUIREMENT.LAST_UPDATED_DATE).get(0))
                    .projectId(queryResult.getValues(REQUIREMENT.PROJECT_ID).get(0));

            UserTransformer userTransformer = new UserTransformer();
            //Filling up Creator
            builder.creator(
                    userTransformer.getEntityFromQueryResult(creatorUser, queryResult)
            );

            //Filling up LeadDeveloper
            if (queryResult.getValues(leadDeveloperUser.ID).get(0) != null) {
                builder.leadDeveloper(
                        userTransformer.getEntityFromQueryResult(leadDeveloperUser, queryResult)
                );
            }

            //Filling up votes
            Result<Record> voteQueryResult = jooq.select(DSL.count(DSL.nullif(vote.IS_UPVOTE, 0)).as("upVotes"))
                    .select(DSL.count(DSL.nullif(vote.IS_UPVOTE, 1)).as("downVotes"))
                    .select(userVote.IS_UPVOTE.as("userVoted"))
                    .from(REQUIREMENT)
                    .leftOuterJoin(vote).on(vote.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(userVote).on(userVote.REQUIREMENT_ID.eq(REQUIREMENT.ID).and(userVote.USER_ID.eq(userId)))
                    .where(transformer.getTableId().equal(id))
                    .groupBy(userVote.IS_UPVOTE)
                    .fetch();

            builder.upVotes(voteQueryResult.get(0).getValue("upVotes", Integer.class));
            builder.downVotes(voteQueryResult.get(0).getValue("downVotes", Integer.class));
            builder.userVoted(transformToUserVoted(voteQueryResult.get(0).getValue("userVoted", Integer.class)));

            requirement = builder.build();

            //Filling up categories
            List<Category> categories = new ArrayList<>();

            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(CATEGORY.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                categories.add(
                        Category.getBuilder(records.getValues(CATEGORY.NAME).get(0))
                                .projectId(records.getValues(CATEGORY.PROJECT_ID).get(0))
                                .id(records.getValues(CATEGORY.ID).get(0))
                                .description(records.getValues(CATEGORY.DESCRIPTION).get(0))
                                .build()
                );
            }
            requirement.setCategories(categories);

            //Filling up additional information
            requirement.setNumberOfComments((Integer) queryResult.getValues(commentCount).get(0));
            requirement.setNumberOfAttachments((Integer) queryResult.getValues(attachmentCount).get(0));
            requirement.setNumberOfFollowers((Integer) queryResult.getValues(followerCount).get(0));
            if (userId != 1) {
                requirement.setFollower((Integer) queryResult.getValues(isFollower).get(0) == 0 ? false : true);
                requirement.setDeveloper((Integer) queryResult.getValues(isDeveloper).get(0) == 0 ? false : true);
                requirement.setContributor(queryResult.getValues(isContributor).get(0).equals(new BigDecimal(0)) ? false : true);
            }

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return requirement;
    }

    @Override
    public void setRealized(int id, Timestamp realized) throws BazaarException {
        try {
            jooq.update(REQUIREMENT)
                    .set(REQUIREMENT.REALIZED, realized)
                    .set(REQUIREMENT.LAST_UPDATED_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
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
                    .set(REQUIREMENT.LAST_UPDATED_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()))
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
                    .select(DSL.countDistinct(CATEGORY.ID).as("numberOfCategories"))
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(REQUIREMENT)
                    .leftJoin(PROJECT).on(PROJECT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(PROJECT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(PROJECT.ID.equal(REQUIREMENT.PROJECT_ID)))
                    .leftJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
                    .leftJoin(CATEGORY).on(CATEGORY.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(CATEGORY.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(CATEGORY.ID.equal(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID)))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(COMMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_DATE.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(REQUIREMENT.ID.eq(requirementId))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(REQUIREMENT)
                    .where(REQUIREMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.ID.eq(requirementId)))
                    .fetchOne();

            result = Statistic.getBuilder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfCategories((Integer) record1.get("numberOfCategories"))
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
