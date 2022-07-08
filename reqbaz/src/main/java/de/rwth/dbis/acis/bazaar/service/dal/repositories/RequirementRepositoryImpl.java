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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementCategoryMapRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.TagRecord;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.*;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.TagTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import de.rwth.dbis.acis.bazaar.service.security.AuthorizationManager;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.web3j.abi.datatypes.Int;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public class RequirementRepositoryImpl extends RepositoryImpl<Requirement, RequirementRecord> implements RequirementRepository {

    // derived table for activities inside requirement
    public static final Table<Record3<Integer, OffsetDateTime, Integer>> ACTIVITY = table(
            select(REQUIREMENT.ID, REQUIREMENT.CREATION_DATE.as("last_activity"), REQUIREMENT.CREATOR_ID.as("last_activity_user_id"))
                    .from(REQUIREMENT)
                    .unionAll(
                            select(REQUIREMENT.ID, REQUIREMENT.LAST_UPDATED_DATE, REQUIREMENT.LAST_UPDATING_USER_ID)
                                    .from(REQUIREMENT))
                    .unionAll(
                            select(COMMENT.REQUIREMENT_ID, COMMENT.CREATION_DATE, COMMENT.USER_ID)
                                    .from(COMMENT))
                    .unionAll(
                            select(COMMENT.REQUIREMENT_ID, COMMENT.LAST_UPDATED_DATE, COMMENT.USER_ID)
                                    .from(COMMENT))
                    .unionAll(
                            select(ATTACHMENT.REQUIREMENT_ID, ATTACHMENT.CREATION_DATE, ATTACHMENT.USER_ID)
                                    .from(ATTACHMENT))
                    .unionAll(
                            select(ATTACHMENT.REQUIREMENT_ID, ATTACHMENT.LAST_UPDATED_DATE, ATTACHMENT.USER_ID)
                                    .from(ATTACHMENT))
    ).as("ACTIVITY");

    public static final Select<Record2<Integer, OffsetDateTime>> LATEST_ACTIVITIES_SUB_QUERY = select(
            ACTIVITY.field(REQUIREMENT.ID),
            max(ACTIVITY.field("last_activity", OffsetDateTime.class)))
            .from(ACTIVITY)
            .groupBy(ACTIVITY.field(REQUIREMENT.ID));

    // derived table for last activity inside requirement
    public static final Table<?> LAST_ACTIVITY = table(
            select(
                    /*
                     * NOTE:
                     * We need to GROUP BY here AGAIN and use MAX last_activity and MAX last_activity_user_id!
                     *
                     * Otherwise we can get:
                     *  - SQL subquery errors (subquery returning more than one result)
                     *  - duplicate requirements in responses
                     * if the following holds (this was a problem e.g. with the ReqBaz project):
                     *  - a requirement has no known last_updating_user (legacy reasons: value is -1)
                     *  - the creation_date and last_updated_date are equal (exact)
                     * This results in the TWO candiates matching the MAX last_activity during the JOIN, which would
                     * not be a problem if the did not have different user IDs.
                     */
                    ACTIVITY.field(REQUIREMENT.ID),
                    max(ACTIVITY.field("last_activity", OffsetDateTime.class)).as("last_activity"),
                    max(ACTIVITY.field("last_activity_user_id", Integer.class)).as("last_activity_user_id") // ID of user who performed the lats activity
            )
                    .from(ACTIVITY)
                    .where(row(ACTIVITY.field(REQUIREMENT.ID), ACTIVITY.field("last_activity", OffsetDateTime.class))
                            .in(
                                    LATEST_ACTIVITIES_SUB_QUERY
                            ))
            .groupBy(ACTIVITY.field(REQUIREMENT.ID))
    );

    public static final Field<Object> VOTE_COUNT = select(DSL.count(DSL.nullif(VOTE.IS_UPVOTE, false)))
            .from(VOTE)
            .where(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID))
            .asField("voteCount");

    public static final Field<Object> COMMENT_COUNT = select(DSL.count())
            .from(COMMENT)
            .where(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
            .asField("commentCount");

    public static final Field<Object> ATTACHMENT_COUNT = select(DSL.count())
            .from(ATTACHMENT)
            .where(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID))
            .asField("attachmentCount");

    public static final Field<Object> FOLLOWER_COUNT = select(DSL.count())
            .from(REQUIREMENT_FOLLOWER_MAP)
            .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID))
            .asField("followerCount");

    de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
    de.rwth.dbis.acis.bazaar.dal.jooq.tables.User lastUpdatingUser = USER.as("lastUpdatingUser");
    de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leadDeveloperUser = USER.as("leadDeveloperUser");
    de.rwth.dbis.acis.bazaar.dal.jooq.tables.Vote vote = VOTE.as("vote");
    de.rwth.dbis.acis.bazaar.dal.jooq.tables.Vote userVote = VOTE.as("userVote");

    /**
     * Value used for last_updating_user_id in case the user is unknown.
     * (This value is used for legacy reasons because the user was not tracked form the beginning.)
     */
    public static final int LAST_UPDATING_USER_UNKNOWN = -1;

    private final DALFacade dalFacade;

    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public RequirementRepositoryImpl(DSLContext jooq, DALFacade dalFacade) {
        super(jooq, new RequirementTransformer(dalFacade));
        this.dalFacade = dalFacade;
    }

    private ImmutablePair<List<Requirement>, Integer> getFilteredRequirements(Collection<Condition> requirementFilter, Pageable pageable, int userId) throws Exception {
        List<Requirement> requirements = new ArrayList<>();

        Field<Object> idCount = jooq.selectCount()
                .from(REQUIREMENT)
                .where(requirementFilter)
                .asField("idCount");

        Field<Object> isFollower = DSL.select(DSL.count())
                .from(REQUIREMENT_FOLLOWER_MAP)
                .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID).and(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(userId)))
                .asField("isFollower");

        Field<Object> isDeveloper = DSL.select(DSL.count())
                .from(REQUIREMENT_DEVELOPER_MAP)
                .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID).and(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(userId)))
                .asField("isDeveloper");

        Condition isAuthorizedCondition = REQUIREMENT.PROJECT_ID.in(
                DSL.select(PROJECT.ID)
                        .from(PROJECT)
                        .where(PROJECT.ID.eq(REQUIREMENT.PROJECT_ID))
                        .and(PROJECT.VISIBILITY.isTrue().or(PROJECT.LEADER_ID.eq(userId)))
        );

        Field<Object> lastActivity = DSL.select(LAST_ACTIVITY.field("last_activity")).from(LAST_ACTIVITY)
                .where(LAST_ACTIVITY.field(REQUIREMENT.ID).equal(REQUIREMENT.ID))
                .asField("lastActivity");
        Field<Integer> lastActivityUserId = LAST_ACTIVITY.field("last_activity_user_id", Integer.class);
        de.rwth.dbis.acis.bazaar.dal.jooq.tables.User lastActivityUser = USER.as("lastActivityUser");

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
                .asField("isContributor");


        Result<Record> queryResults = jooq.select(REQUIREMENT.fields())
                .select(idCount)
                .select(COMMENT_COUNT)
                .select(ATTACHMENT_COUNT)
                .select(FOLLOWER_COUNT)
                .select(isFollower)
                .select(isDeveloper)
                .select(isContributor)
                .select(creatorUser.fields())
                .select(lastUpdatingUser.fields())
                .select(leadDeveloperUser.fields())
                .select(PROJECT.fields())
                .select(lastActivity)
                .select(lastActivityUserId)
                .select(lastActivityUser.fields())
                .from(REQUIREMENT)
                .join(creatorUser).on(creatorUser.ID.equal(REQUIREMENT.CREATOR_ID))
                .leftOuterJoin(lastUpdatingUser).on(lastUpdatingUser.ID.equal(REQUIREMENT.LAST_UPDATING_USER_ID))
                .leftOuterJoin(leadDeveloperUser).on(leadDeveloperUser.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))
                .leftOuterJoin(PROJECT).on(PROJECT.ID.equal(REQUIREMENT.PROJECT_ID))
                .leftOuterJoin(LAST_ACTIVITY).on(REQUIREMENT.ID.eq(LAST_ACTIVITY.field(REQUIREMENT.ID)))
                .leftOuterJoin(lastActivityUser).on(lastActivityUser.ID.equal(lastActivityUserId))
                .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT.ID.eq(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                .where(requirementFilter)
                .and(isAuthorizedCondition)
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        for (Record queryResult : queryResults) {
            RequirementRecord requirementRecord = queryResult.into(REQUIREMENT);
            Requirement requirement = transformer.getEntityFromTableRecord(requirementRecord);
            UserContext.Builder userContext = UserContext.builder();

            requirement.setLastActivity((OffsetDateTime) queryResult.getValue(lastActivity));

            UserTransformer userTransformer = new UserTransformer();
            //Filling up Creator
            requirement.setCreator(
                    userTransformer.getEntityFromTableRecord(queryResult.into(creatorUser))
            );

            // add last updating user, if not unknown
            if (requirementRecord.get(REQUIREMENT.LAST_UPDATING_USER_ID) != LAST_UPDATING_USER_UNKNOWN) {
                requirement.setLastUpdatingUser(userTransformer.getEntityFromTableRecord(queryResult.into(lastUpdatingUser)));
            }

            // add last activity user, if not unknown
            if (queryResult.get(lastActivityUserId) != LAST_UPDATING_USER_UNKNOWN) {
                requirement.setLastActivityUser(userTransformer.getEntityFromTableRecord(queryResult.into(lastActivityUser)));
            }

            //Filling up LeadDeveloper
            if (queryResult.getValue(leadDeveloperUser.ID) != null) {
                requirement.setLeadDeveloper(
                        userTransformer.getEntityFromTableRecord(queryResult.into(leadDeveloperUser))
                );
            }

            //Filling up votes
            Result<Record> voteQueryResult = jooq.select(DSL.count(DSL.nullif(vote.IS_UPVOTE, false)).as("upVotes"))
                    .select(DSL.count(DSL.nullif(vote.IS_UPVOTE, true)).as("downVotes"))
                    .select(userVote.IS_UPVOTE.as("userVoted"))
                    .from(REQUIREMENT)
                    .leftOuterJoin(vote).on(vote.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(userVote).on(userVote.REQUIREMENT_ID.eq(REQUIREMENT.ID).and(userVote.USER_ID.eq(userId)))
                    .where(transformer.getTableId().equal(requirement.getId()))
                    .groupBy(userVote.IS_UPVOTE)
                    .fetch();

            requirement.setUpVotes(voteQueryResult.get(0).getValue("upVotes", Integer.class));
            requirement.setDownVotes(voteQueryResult.get(0).getValue("downVotes", Integer.class));

            userContext.userVoted(transformToUserVoted(voteQueryResult.get(0).getValue("userVoted", Integer.class)));

            //Filling up categories
            List<Integer> categories = new ArrayList<>();

            Result<RequirementCategoryMapRecord> categoryRecord = jooq.selectFrom(REQUIREMENT_CATEGORY_MAP).where(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.eq(requirement.getId())).fetch();

            categoryRecord.forEach(record -> categories.add(record.getValue(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID)));

            requirement.setCategories(categories);

            // Filling up tags
            Result<TagRecord> tagRecords = jooq.select(TAG.fields())
                    .from(TAG)
                    .leftOuterJoin(REQUIREMENT_TAG_MAP).on(REQUIREMENT_TAG_MAP.TAG_ID.eq(TAG.ID))
                    .where(REQUIREMENT_TAG_MAP.REQUIREMENT_ID.eq(requirement.getId())).fetchInto(TAG);

            TagTransformer tagTransformer = new TagTransformer();
            List<Tag> tags = tagRecords.stream()
                    .map(tagTransformer::getEntityFromTableRecord)
                    .collect(Collectors.toList());

            requirement.setTags(tags);

            //Filling up additional information
            requirement.setNumberOfComments((Integer) queryResult.getValue(COMMENT_COUNT));
            requirement.setNumberOfAttachments((Integer) queryResult.getValue(ATTACHMENT_COUNT));
            requirement.setNumberOfFollowers((Integer) queryResult.getValue(FOLLOWER_COUNT));
            if (userId != 1) {
                userContext.isFollower(0 != (Integer) queryResult.getValue(isFollower));
                userContext.isDeveloper(0 != (Integer) queryResult.getValue(isDeveloper));
                userContext.isContributor(!Objects.equals(queryResult.getValue(isContributor), new BigDecimal(0)));
            }

            if (requirement.getNumberOfAttachments() > 0) {
                AttachmentRepository attachmentRepository = new AttachmentRepositoryImpl(jooq);
                List<Attachment> attachmentList = attachmentRepository.findAllByRequirementId(requirement.getId(), new PageInfo(0, 1000, new HashMap<>())).getElements();
                requirement.setAttachments(attachmentList);
            }

            // TODO We should refactor here: the same authorization check is made in the RequirementResource#moveRequirement(..) method
            boolean authorizedToModifyRequirement = new AuthorizationManager().isAuthorizedInContext(userId, PrivilegeEnum.Modify_REQUIREMENT, requirement.getProjectId(), dalFacade);
            userContext.isMoveAllowed(authorizedToModifyRequirement);

            userContext.isDeleteAllowed(authorizedToModifyRequirement || requirement.isOwner(userId));

            requirement.setContext(EntityContextFactory.create(pageable.getEmbed(), queryResult, dalFacade));
            requirement.setUserContext(userContext.build());
            requirements.add(requirement);
        }
        int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));

        return ImmutablePair.of(requirements, total);
    }

    private ImmutablePair<List<Requirement>, Integer> getFilteredRequirements(Condition requirementFilter, Pageable pageable, int userId) throws Exception {
        return getFilteredRequirements(Collections.singletonList(requirementFilter), pageable, userId);
    }

    private ImmutablePair<List<Requirement>, Integer> getFilteredRequirements(Condition requirementFilter, int userId) throws Exception {
        return getFilteredRequirements(requirementFilter, new PageInfo(0, 1000, new HashMap<>()), userId);
    }

    @Override
    public List<Integer> listAllRequirementIds(Pageable pageable, int userId) throws BazaarException {
        List<Integer> requirementIds = new ArrayList<>();
        try {
            requirementIds = jooq.select()
                    .from(REQUIREMENT)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    //       .limit(pageable.getPageSize())
                    //       .offset(pageable.getOffset())
                    .fetch(REQUIREMENT.ID);

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return requirementIds;
    }

    @Override
    public PaginationResult<Requirement> findAll(Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Requirement> result = null;
        try {
            Collection<Condition> filterCondition = (Collection<Condition>) transformer.getFilterConditions(pageable.getFilters());
            filterCondition.add(transformer.getSearchCondition(pageable.getSearch()));

            ImmutablePair<List<Requirement>, Integer> filteredRequirements = getFilteredRequirements(filterCondition, pageable, userId);

            result = new PaginationResult<>(filteredRequirements.right, pageable, filteredRequirements.left);

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Requirement> findAllByProject(int projectId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Requirement> result = null;
        try {
            Collection<Condition> filterCondition = (Collection<Condition>) transformer.getFilterConditions(pageable.getFilters());
            filterCondition.add(transformer.getSearchCondition(pageable.getSearch()));
            filterCondition.add(REQUIREMENT.PROJECT_ID.eq(projectId));

            ImmutablePair<List<Requirement>, Integer> filteredRequirements = getFilteredRequirements(filterCondition, pageable, userId);

            result = new PaginationResult<>(filteredRequirements.right, pageable, filteredRequirements.left);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    private UserVote transformToUserVoted(Integer userVotedInt) {
        UserVote userVoted;
        if (userVotedInt == null) {
            return UserVote.NO_VOTE;
        }
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
        try {
            Collection<Condition> filterCondition = (Collection<Condition>) transformer.getFilterConditions(pageable.getFilters());
            filterCondition.add(transformer.getSearchCondition(pageable.getSearch()));
            filterCondition.add(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(categoryId));

            ImmutablePair<List<Requirement>, Integer> filteredRequirements = getFilteredRequirements(filterCondition, pageable, userId);

            result = new PaginationResult<>(filteredRequirements.right, pageable, filteredRequirements.left);
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
        return findById(id, userId, null);
    }

    @Override
    public Requirement findById(int id, int userId, List<String> embed) throws Exception {
        Requirement requirement = null;
        try {

            Condition filterCondition = transformer.getTableId().equal(id);

            ImmutablePair<List<Requirement>, Integer> filteredRequirements = getFilteredRequirements(filterCondition, userId);

            if (filteredRequirements.left == null || filteredRequirements.left.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            requirement = filteredRequirements.left.get(0);

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return requirement;
    }

    @Override
    public void setRealized(int id, OffsetDateTime realized) throws BazaarException {
        try {
            jooq.update(REQUIREMENT)
                    .set(REQUIREMENT.REALIZED, realized)
                    .set(REQUIREMENT.LAST_UPDATED_DATE, OffsetDateTime.now())
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
                    .set(REQUIREMENT.LAST_UPDATED_DATE, OffsetDateTime.now())
                    .where(REQUIREMENT.ID.eq(id))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
    }

    @Override
    public Statistic getStatisticsForRequirement(int userId, int requirementId, OffsetDateTime timestamp) throws BazaarException {
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

            result = Statistic.builder()
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

    @Override
    public List<Requirement> getFollowedRequirements(int userId, int count) throws BazaarException {
        List<Requirement> requirements = null;
        try {
            List<Integer> requirementIds;
            requirementIds = jooq.select()
                    .from(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.eq(userId))
                    .fetch(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID);

            Condition filterCondition = transformer.getTableId().in(requirementIds);

            Pageable.SortField sortField = new Pageable.SortField("last_activity", "DESC");
            List<Pageable.SortField> sortList = new ArrayList<>();
            sortList.add(sortField);

            PageInfo filterPage = new PageInfo(0, count, new HashMap<>(), sortList);

            requirements = getFilteredRequirements(filterCondition, filterPage, userId).left;

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return requirements;
    }
}
