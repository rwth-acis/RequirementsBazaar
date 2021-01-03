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

import de.rwth.dbis.acis.bazaar.service.dal.entities.CategoryContributors;
import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectContributors;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementContributors;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import i5.las2peer.api.security.AgentLockedException;
import i5.las2peer.security.PassphraseAgentImpl;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class UserRepositoryImpl extends RepositoryImpl<User, UserRecord> implements UserRepository {
    private final byte ONE = 1;

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public UserRepositoryImpl(DSLContext jooq) {
        super(jooq, new UserTransformer());
    }

    @Override
    public Integer getIdByLas2PeerId(String las2PeerId) throws BazaarException {
        Integer id = null;
        try {
            id = jooq.selectFrom(USER).where(USER.LAS2PEER_ID.equal(las2PeerId)).fetchOne(USER.ID);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return id;
    }

    /**
     * Hash agent sub field. This was needed to update from las2peer 0.6.* to 0.7.*.
     * The las2peer id changed, and with the hashAgentSub method the old las2peer id can be generated.
     * @param agent
     * @return hashed agent sub
     * @throws BazaarException
     */
    @Override
    public long hashAgentSub(PassphraseAgentImpl agent) throws BazaarException {
        long h = 1125899906842597L;
        try {
            String string = agent.getPassphrase();

            int len = string.length();

            for (int i = 0; i < len; ++i) {
                h = 31L * h + (long) string.charAt(i);
            }
        } catch (AgentLockedException alEx) {
            ExceptionHandler.getInstance().convertAndThrowException(alEx, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return h;
    }

    @Override
    public void updateLas2peerId(int userId, String las2PeerId) throws BazaarException {
        try {
            jooq.update(USER).set(USER.LAS2PEER_ID, las2PeerId)
                    .where(USER.ID.equal(userId))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public void updateLastLoginDate(int userId) throws Exception {
        try {
            jooq.update(USER).set(USER.LAST_LOGIN_DATE, LocalDateTime.now())
                    .where(USER.ID.equal(userId))
                    .execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public RequirementContributors findRequirementContributors(int requirementId) throws BazaarException {
        RequirementContributors contributors = null;
        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User creator = USER.as("creator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leadDeveloper = USER.as("leadDeveloper");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User developer = USER.as("developer");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User commentCreator = USER.as("commentCreator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User attachmentCreator = USER.as("attachmentCreator");

            Result<Record> queryResult = jooq.select(REQUIREMENT.fields())
                    .select(creator.fields())
                    .select(leadDeveloper.fields())
                    .select(developer.fields())
                    .select(commentCreator.fields())
                    .select(attachmentCreator.fields())
                    .from(REQUIREMENT)
                    .leftOuterJoin(creator).on(creator.ID.equal(REQUIREMENT.CREATOR_ID))
                    .leftOuterJoin(leadDeveloper).on(leadDeveloper.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(developer).on(developer.ID.eq(REQUIREMENT_DEVELOPER_MAP.USER_ID))
                    .leftOuterJoin(COMMENT).on(COMMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(commentCreator).on(commentCreator.ID.eq(COMMENT.USER_ID))
                    .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(attachmentCreator).on(attachmentCreator.ID.eq(ATTACHMENT.USER_ID))
                    .where(REQUIREMENT.ID.equal(requirementId))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No requirement found with id: " + requirementId),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            UserTransformer userTransformer = new UserTransformer();
            RequirementContributors.Builder builder = new RequirementContributors.Builder();

            builder.creator(
                    userTransformer.getEntityFromQueryResult(creator, queryResult)
            );
            if (queryResult.getValues(leadDeveloper.ID).get(0) != null) {
                builder.leadDeveloper(
                        userTransformer.getEntityFromQueryResult(leadDeveloper, queryResult)
                );
            }
            List<User> developers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(developer.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                developers.add(
                        userTransformer.getEntityFromQueryResult(developer, records)
                );
            }
            builder.developers(developers);

            List<User> commentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(commentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                commentCreators.add(
                        userTransformer.getEntityFromQueryResult(commentCreator, records)
                );
            }
            builder.commentCreator(commentCreators);

            List<User> attachmentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(attachmentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                attachmentCreators.add(
                        userTransformer.getEntityFromQueryResult(attachmentCreator, records)
                );
            }
            builder.attachmentCreator(attachmentCreators);

            contributors = builder.build();

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return contributors;
    }

    @Override
    public CategoryContributors findCategoryContributors(int categoryId) throws BazaarException {
        CategoryContributors contributors = null;
        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leader = USER.as("leader");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User requirementCreator = USER.as("requirementCreator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leadDeveloper = USER.as("leadDeveloper");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User developer = USER.as("developer");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User commentCreator = USER.as("commentCreator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User attachmentCreator = USER.as("attachmentCreator");

            Result<Record> queryResult = jooq.select(CATEGORY.fields())
                    .select(leader.fields())
                    .select(requirementCreator.fields())
                    .select(leadDeveloper.fields())
                    .select(developer.fields())
                    .select(commentCreator.fields())
                    .select(attachmentCreator.fields())
                    .from(CATEGORY)
                    .leftOuterJoin(leader).on(leader.ID.equal(CATEGORY.LEADER_ID))
                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(CATEGORY.ID))
                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.eq(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                    .leftOuterJoin(requirementCreator).on(requirementCreator.ID.equal(REQUIREMENT.CREATOR_ID))
                    .leftOuterJoin(leadDeveloper).on(leadDeveloper.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(developer).on(developer.ID.eq(REQUIREMENT_DEVELOPER_MAP.USER_ID))
                    .leftOuterJoin(COMMENT).on(COMMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(commentCreator).on(commentCreator.ID.eq(COMMENT.USER_ID))
                    .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(attachmentCreator).on(attachmentCreator.ID.eq(ATTACHMENT.USER_ID))

                    .where(CATEGORY.ID.equal(categoryId))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No category found with id: " + categoryId),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            UserTransformer userTransformer = new UserTransformer();
            CategoryContributors.Builder builder = new CategoryContributors.Builder();

            builder.leader(
                    userTransformer.getEntityFromQueryResult(leader, queryResult)
            );

            List<User> requirementCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(requirementCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                requirementCreators.add(
                        userTransformer.getEntityFromQueryResult(requirementCreator, records)
                );
            }
            builder.requirementCreator(requirementCreators);

            List<User> leadDevelopers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(leadDeveloper.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                leadDevelopers.add(
                        userTransformer.getEntityFromQueryResult(leadDeveloper, records)
                );
            }
            builder.leadDeveloper(leadDevelopers);

            List<User> developers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(developer.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                developers.add(
                        userTransformer.getEntityFromQueryResult(developer, records)
                );
            }
            builder.developers(developers);

            List<User> commentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(commentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                commentCreators.add(
                        userTransformer.getEntityFromQueryResult(commentCreator, records)
                );
            }
            builder.commentCreator(commentCreators);

            List<User> attachmentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(attachmentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                attachmentCreators.add(
                        userTransformer.getEntityFromQueryResult(attachmentCreator, records)
                );
            }
            builder.attachmentCreator(attachmentCreators);

            contributors = builder.build();

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return contributors;
    }

    @Override
    public ProjectContributors findProjectContributors(int projectId) throws BazaarException {
        ProjectContributors contributors = null;
        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leader = USER.as("leader");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User categoryLeader = USER.as("categoryLeader");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User requirementCreator = USER.as("requirementCreator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leadDeveloper = USER.as("leadDeveloper");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User developer = USER.as("developer");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User commentCreator = USER.as("commentCreator");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User attachmentCreator = USER.as("attachmentCreator");

            Result<Record> queryResult = jooq.select(PROJECT.fields())
                    .select(leader.fields())
                    .select(categoryLeader.fields())
                    .select(requirementCreator.fields())
                    .select(leadDeveloper.fields())
                    .select(developer.fields())
                    .select(commentCreator.fields())
                    .select(attachmentCreator.fields())
                    .from(PROJECT)

                    .leftOuterJoin(leader).on(leader.ID.equal(PROJECT.LEADER_ID))

                    .leftOuterJoin(CATEGORY).on(CATEGORY.PROJECT_ID.eq(PROJECT.ID))
                    .leftOuterJoin(categoryLeader).on(categoryLeader.ID.equal(CATEGORY.LEADER_ID))

                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(CATEGORY.ID))
                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.eq(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                    .leftOuterJoin(requirementCreator).on(requirementCreator.ID.equal(REQUIREMENT.CREATOR_ID))
                    .leftOuterJoin(leadDeveloper).on(leadDeveloper.ID.equal(REQUIREMENT.LEAD_DEVELOPER_ID))
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(developer).on(developer.ID.eq(REQUIREMENT_DEVELOPER_MAP.USER_ID))
                    .leftOuterJoin(COMMENT).on(COMMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(commentCreator).on(commentCreator.ID.eq(COMMENT.USER_ID))
                    .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.REQUIREMENT_ID.eq(REQUIREMENT.ID))
                    .leftOuterJoin(attachmentCreator).on(attachmentCreator.ID.eq(ATTACHMENT.USER_ID))

                    .where(PROJECT.ID.equal(projectId))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No project found with id: " + projectId),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            UserTransformer userTransformer = new UserTransformer();
            ProjectContributors.Builder builder = new ProjectContributors.Builder();

            builder.leader(
                    userTransformer.getEntityFromQueryResult(leader, queryResult)
            );

            List<User> categoriesLeaders = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(categoryLeader.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                categoriesLeaders.add(
                        userTransformer.getEntityFromQueryResult(categoryLeader, records)
                );
            }
            builder.requirementCreator(categoriesLeaders);

            List<User> requirementCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(requirementCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                requirementCreators.add(
                        userTransformer.getEntityFromQueryResult(requirementCreator, records)
                );
            }
            builder.requirementCreator(requirementCreators);

            List<User> leadDevelopers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(leadDeveloper.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                leadDevelopers.add(
                        userTransformer.getEntityFromQueryResult(leadDeveloper, records)
                );
            }
            builder.leadDeveloper(leadDevelopers);

            List<User> developers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(developer.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                developers.add(
                        userTransformer.getEntityFromQueryResult(developer, records)
                );
            }
            builder.developers(developers);

            List<User> commentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(commentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                commentCreators.add(
                        userTransformer.getEntityFromQueryResult(commentCreator, records)
                );
            }
            builder.commentCreator(commentCreators);

            List<User> attachmentCreators = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(attachmentCreator.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                attachmentCreators.add(
                        userTransformer.getEntityFromQueryResult(attachmentCreator, records)
                );
            }
            builder.attachmentCreator(attachmentCreators);

            contributors = builder.build();

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return contributors;
    }

    @Override
    public PaginationResult<User> findAllByDeveloping(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<User> result = null;
        List<User> users;
        try {
            users = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(USER)
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                    .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(USER.fields())
                    .select(idCount)
                    .from(USER)
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                    .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                users.add(transformer.getEntityFromTableRecord(userRecord));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, users);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<User> findAllByFollowing(int projectId, int categoryId, int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<User> result = null;
        List<User> users;
        try {
            users = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(jooq.selectDistinct()
                            .from(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(PROJECT_FOLLOWER_MAP).on(PROJECT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .leftOuterJoin(CATEGORY_FOLLOWER_MAP).on(CATEGORY_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .leftOuterJoin(REQUIREMENT_FOLLOWER_MAP).on(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(projectId))
                                            .or(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId))
                                            .or(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId))
                                            .asTable("inner"))
                    ).asField("idCount");

            List<Record> queryResults = jooq.selectDistinct()
                    .from(
                            jooq.select(USER.fields())
                                    .select(idCount)
                                    .from(USER)
                                    .leftOuterJoin(PROJECT_FOLLOWER_MAP).on(PROJECT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .leftOuterJoin(CATEGORY_FOLLOWER_MAP).on(CATEGORY_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .leftOuterJoin(REQUIREMENT_FOLLOWER_MAP).on(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(projectId))
                                    .or(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId))
                                    .or(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId))
                                    .asTable("inner"))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                users.add(transformer.getEntityFromTableRecord(userRecord));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, users);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public List<User> getEmailReceiverForProject(int projectId) throws BazaarException {
        List<User> entries = null;
        try {
            entries = new ArrayList<>();

            // select distinct all project leader and follower
            List<Record> queryResults = jooq.selectDistinct(USER.fields())
                    .from(USER
                            .join(PROJECT).on(USER.ID.eq(PROJECT.LEADER_ID)))
                    .where(PROJECT.ID.eq(projectId))
                    .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE))

                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(PROJECT_FOLLOWER_MAP).on(USER.ID.eq(PROJECT_FOLLOWER_MAP.USER_ID)))
                            .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.eq(projectId))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))
                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                entries.add(transformer.getEntityFromTableRecord(userRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }

    @Override
    public List<User> getEmailReceiverForCategory(int categoryId) throws BazaarException {
        List<User> entries = null;
        try {
            entries = new ArrayList<>();

            // select distinct all followers union project leader union categories leader
            List<Record> queryResults = jooq.selectDistinct(USER.fields())
                    .from(USER
                            .join(CATEGORY).on(USER.ID.eq(CATEGORY.LEADER_ID)))
                    .where(CATEGORY.ID.eq(categoryId))
                    .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE))

                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(CATEGORY_FOLLOWER_MAP).on(USER.ID.eq(CATEGORY_FOLLOWER_MAP.USER_ID)))
                            .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.eq(categoryId))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))

                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(PROJECT).on(USER.ID.eq(PROJECT.LEADER_ID))
                                    .join(CATEGORY).on(CATEGORY.PROJECT_ID.eq(PROJECT.ID)))
                            .where(CATEGORY.ID.eq(categoryId))
                            .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE)))

                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(PROJECT_FOLLOWER_MAP).on(USER.ID.eq(PROJECT_FOLLOWER_MAP.USER_ID))
                                    .join(CATEGORY).on(CATEGORY.PROJECT_ID.eq(PROJECT_FOLLOWER_MAP.PROJECT_ID)))
                            .where(CATEGORY.ID.eq(categoryId))
                            .and(PROJECT_FOLLOWER_MAP.PROJECT_ID.eq(CATEGORY.PROJECT_ID))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))

                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                entries.add(transformer.getEntityFromTableRecord(userRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }

    @Override
    public List<User> getEmailReceiverForRequirement(int requirementId) throws BazaarException {
        List<User> entries = null;
        try {
            entries = new ArrayList<>();

            // select distinct all followers union project leader union categories leader union req leader
            List<Record> queryResults = jooq.selectDistinct(USER.fields())
                    // req leader
                    .from(USER
                            .join(REQUIREMENT).on(REQUIREMENT.LEAD_DEVELOPER_ID.eq(USER.ID)))
                    .where(REQUIREMENT.ID.eq(requirementId))
                    .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE))

                    // req follower
                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(REQUIREMENT_FOLLOWER_MAP).on(USER.ID.eq(REQUIREMENT_FOLLOWER_MAP.USER_ID)))
                            .where(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.eq(requirementId))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))

                    // category leader
                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(CATEGORY).on(USER.ID.eq(CATEGORY.LEADER_ID))
                                    .join(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(CATEGORY.ID)))
                            .where(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.eq(requirementId))
                            .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE)))

                    // category follower
                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(CATEGORY_FOLLOWER_MAP).on(USER.ID.eq(CATEGORY_FOLLOWER_MAP.USER_ID))
                                    .join(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.eq(CATEGORY_FOLLOWER_MAP.CATEGORY_ID))
                                    .join(REQUIREMENT).on(REQUIREMENT.ID.eq(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID)))
                            .where(REQUIREMENT.ID.eq(requirementId))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))

                    // project leader
                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(PROJECT).on(USER.ID.eq(PROJECT.LEADER_ID))
                                    .join(REQUIREMENT).on(REQUIREMENT.PROJECT_ID.eq(PROJECT.ID)))
                            .where(REQUIREMENT.ID.eq(requirementId))
                            .and(USER.EMAIL_LEAD_SUBSCRIPTION.eq(ONE)))

                    // project follower
                    .union(jooq.selectDistinct(USER.fields())
                            .from(USER
                                    .join(PROJECT_FOLLOWER_MAP).on(USER.ID.eq(PROJECT_FOLLOWER_MAP.USER_ID))
                                    .join(REQUIREMENT).on(REQUIREMENT.PROJECT_ID.eq(PROJECT_FOLLOWER_MAP.PROJECT_ID)))
                            .where(REQUIREMENT.ID.eq(requirementId))
                            .and(PROJECT_FOLLOWER_MAP.PROJECT_ID.eq(REQUIREMENT.PROJECT_ID))
                            .and(USER.EMAIL_FOLLOW_SUBSCRIPTION.eq(ONE)))

                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                entries.add(transformer.getEntityFromTableRecord(userRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }
}
