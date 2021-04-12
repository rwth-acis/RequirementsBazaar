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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

/**
 * @since 6/9/2014
 */
public class ProjectRepositoryImpl extends RepositoryImpl<Project, ProjectRecord> implements ProjectRepository {

    // derived table for activities inside project
    public static final Table<?> ACTIVITY = table(
            select(PROJECT.ID, PROJECT.CREATION_DATE)
                    .from(PROJECT)
                    .unionAll(
                            select(PROJECT.ID, PROJECT.LAST_UPDATED_DATE)
                                    .from(PROJECT))
                    .unionAll(
                            select(CATEGORY.PROJECT_ID, CATEGORY.CREATION_DATE)
                                    .from(CATEGORY))
                    .unionAll(
                            select(CATEGORY.PROJECT_ID, CATEGORY.LAST_UPDATED_DATE)
                                    .from(CATEGORY))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, REQUIREMENT.LAST_UPDATED_DATE)
                                    .from(REQUIREMENT))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, REQUIREMENT.LAST_UPDATED_DATE)
                                    .from(REQUIREMENT))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, COMMENT.CREATION_DATE)
                                    .from(COMMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(COMMENT.REQUIREMENT_ID)))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, COMMENT.LAST_UPDATED_DATE)
                                    .from(COMMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(COMMENT.REQUIREMENT_ID)))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, ATTACHMENT.CREATION_DATE)
                                    .from(ATTACHMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(ATTACHMENT.REQUIREMENT_ID)))
                    .unionAll(
                            select(REQUIREMENT.PROJECT_ID, ATTACHMENT.LAST_UPDATED_DATE)
                                    .from(ATTACHMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(ATTACHMENT.REQUIREMENT_ID)))
    ).as("ACTIVITY");

    // derived table for last activity inside project
    public static final Table<?> LAST_ACTIVITY = table(
            select(
                    ACTIVITY.field(PROJECT.ID),
                    max(ACTIVITY.field(PROJECT.CREATION_DATE)).as("last_activity"))
                    .from(ACTIVITY)
                    .groupBy(ACTIVITY.field(PROJECT.ID)))
            .as("last_activity");

    public static final Field<Object> CATEGORY_COUNT = select(DSL.count())
            .from(CATEGORY)
            .where(CATEGORY.PROJECT_ID.equal(PROJECT.ID))
            .asField("categoryCount");

    public final static Field<Object> REQUIREMENT_COUNT = select(DSL.count())
            .from(REQUIREMENT)
            .where(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
            .asField("requirementCount");

    public final static Field<Object> FOLLOWER_COUNT = DSL.select(DSL.count())
            .from(PROJECT_FOLLOWER_MAP)
            .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
            .asField("followerCount");

    private final static de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leaderUser = USER.as("leaderUser");


    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ProjectRepositoryImpl(DSLContext jooq) {
        super(jooq, new ProjectTransformer());
    }

    @Override
    public Project findById(int id, int userId) throws BazaarException {
        Project project = null;
        try {
            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(PROJECT_FOLLOWER_MAP)
                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID).and(PROJECT_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            Result<Record> queryResult = jooq.select(PROJECT.fields())
                    .select(CATEGORY_COUNT)
                    .select(REQUIREMENT_COUNT)
                    .select(FOLLOWER_COUNT)
                    .select(isFollower)
                    .select(leaderUser.fields())
                    .from(PROJECT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECT.LEADER_ID))
                    .where(transformer.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Project.Builder builder = Project.builder()
                    .name(queryResult.getValues(PROJECT.NAME).get(0))
                    .description(queryResult.getValues(PROJECT.DESCRIPTION).get(0))
                    .id(queryResult.getValues(PROJECT.ID).get(0))
                    .defaultCategoryId(queryResult.getValues(PROJECT.DEFAULT_CATEGORY_ID).get(0))
                    .visibility(queryResult.getValues(PROJECT.VISIBILITY).get(0) == 1)
                    .creationDate(queryResult.getValues(PROJECT.CREATION_DATE).get(0))
                    .lastUpdatedDate(queryResult.getValues(PROJECT.LAST_UPDATED_DATE).get(0));

            UserTransformer userTransformer = new UserTransformer();
            //Filling up LeadDeveloper
            builder.leader(userTransformer.getEntityFromQueryResult(leaderUser, queryResult));

            project = builder.build();

            // Filling additional information
            project.setNumberOfCategories((Integer) queryResult.getValues(CATEGORY_COUNT).get(0));
            project.setNumberOfRequirements((Integer) queryResult.getValues(REQUIREMENT_COUNT).get(0));
            project.setNumberOfFollowers((Integer) queryResult.getValues(FOLLOWER_COUNT).get(0));
            if (userId != 1) {
                project.setIsFollower(0 != (Integer) queryResult.getValues(isFollower).get(0));
            }

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return project;
    }

    private PaginationResult<Project> findProjects(Pageable pageable, int userId, Boolean queryPrivate) throws Exception {

        List<Project> projects = new ArrayList<>();
        Field<Object> idCount = jooq.selectCount()
                .from(PROJECT)
                .where(PROJECT.VISIBILITY.isTrue())
                .and(transformer.getSearchCondition(pageable.getSearch()))
                .asField("idCount");

        Field<Object> isFollower = DSL.select(DSL.count())
                .from(PROJECT_FOLLOWER_MAP)
                .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID).and(PROJECT_FOLLOWER_MAP.USER_ID.equal(userId)))
                .asField("isFollower");
        Field<Object> lastActivity = DSL.select(LAST_ACTIVITY.field("last_activity")).from(LAST_ACTIVITY)
                .where(LAST_ACTIVITY.field(PROJECT.ID).equal(PROJECT.ID))
                .asField("lastActivity");

        Condition searchConditions = transformer.getSearchCondition(pageable.getSearch())
                .and((pageable.getIds().size() > 0) ? PROJECT.ID.in(pageable.getIds()) : trueCondition());

        if (queryPrivate) {
            // TODO: Include permission check by project membership query
            searchConditions.and(
                    PROJECT.VISIBILITY.isTrue().or(leaderUser.ID.equal(userId))
            );
        }

        Result<Record> queryResults = jooq.select(PROJECT.fields())
                .select(idCount)
                .select(CATEGORY_COUNT)
                .select(REQUIREMENT_COUNT)
                .select(FOLLOWER_COUNT)
                .select(isFollower)
                .select(leaderUser.fields())
                .select(lastActivity)
                .from(PROJECT)
                .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECT.LEADER_ID))
                .leftOuterJoin(LAST_ACTIVITY).on(PROJECT.ID.eq(LAST_ACTIVITY.field(PROJECT.ID)))
                .where(searchConditions)
                .orderBy(transformer.getSortFields(pageable.getSorts()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();

        for (Record queryResult : queryResults) {
            ProjectRecord projectRecord = queryResult.into(PROJECT);
            Project project = transformer.getEntityFromTableRecord(projectRecord);
            UserTransformer userTransformer = new UserTransformer();
            UserRecord userRecord = queryResult.into(leaderUser);
            project.setLeader(userTransformer.getEntityFromTableRecord(userRecord));
            project.setNumberOfCategories((Integer) queryResult.getValue(CATEGORY_COUNT));
            project.setNumberOfRequirements((Integer) queryResult.getValue(REQUIREMENT_COUNT));
            project.setNumberOfFollowers((Integer) queryResult.getValue(FOLLOWER_COUNT));
            project.setLastActivity((LocalDateTime) queryResult.getValue(lastActivity));
            if (userId != 1) {
                project.setIsFollower(0 != (Integer) queryResult.getValue(isFollower));
            }
            projects.add(project);
        }
        int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));

        return new PaginationResult<>(total, pageable, projects);
    }

    @Override
    public PaginationResult<Project> findAllPublic(Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Project> result = null;
        try {
            result = findProjects(pageable, userId, false);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Project> findAllPublicAndAuthorized(Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Project> result = null;
        try {
            result = findProjects(pageable, userId, true);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public List<Integer> listAllProjectIds(Pageable pageable, int userId) throws BazaarException {
        List<Integer> projectIds = new ArrayList<>();
        try {
            projectIds = jooq.select()
                    .from(PROJECT)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                            .and(PROJECT.VISIBILITY.isTrue().or(PROJECT.LEADER_ID.equal(userId))
                            .and(transformer.getSearchCondition(pageable.getSearch())))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
             //       .limit(pageable.getPageSize())
             //       .offset(pageable.getOffset())
                    .fetch(PROJECT.ID);

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return projectIds;
    }


    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {
            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformer.getTable())
                    .where(transformer.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public Statistic getStatisticsForVisibleProjects(int userId, LocalDateTime timestamp) throws BazaarException {
        Statistic result = null;
        try {
            // If you want to change something here, please know what you are doing! Its SQL and even worse JOOQ :-|
            Record record1 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .from(PROJECT)
                    .where(PROJECT.VISIBILITY.isTrue())
                    .and(PROJECT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(PROJECT.LAST_UPDATED_DATE.greaterOrEqual(timestamp)))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(CATEGORY.ID).as("numberOfCategories"))
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(PROJECT)
                    .leftJoin(CATEGORY).on(CATEGORY.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(CATEGORY.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(CATEGORY.PROJECT_ID.equal(PROJECT.ID)))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID)))
                    .where(PROJECT.VISIBILITY.isTrue())
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(PROJECT)
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(COMMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_DATE.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(PROJECT.VISIBILITY.isTrue())
                    .fetchOne();

            result = Statistic.builder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfCategories((Integer) record2.get("numberOfCategories"))
                    .numberOfRequirements((Integer) record2.get("numberOfRequirements"))
                    .numberOfComments((Integer) record3.get("numberOfComments"))
                    .numberOfAttachments((Integer) record3.get("numberOfAttachments"))
                    .numberOfVotes((Integer) record3.get("numberOfVotes"))
                    .build();

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public Statistic getStatisticsForProject(int userId, int projectId, LocalDateTime timestamp) throws BazaarException {
        Statistic result = null;
        try {
            // If you want to change something here, please know what you are doing! Its SQL and even worse JOOQ :-|
            Record record1 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .from(PROJECT)
                    .where(PROJECT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(PROJECT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(PROJECT.ID.eq(projectId)))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .select(DSL.countDistinct(CATEGORY.ID).as("numberOfCategories"))
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(PROJECT)
                    .leftJoin(CATEGORY).on(CATEGORY.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(CATEGORY.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(CATEGORY.PROJECT_ID.equal(PROJECT.ID)))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID)))
                    .where(PROJECT.ID.eq(projectId))
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(PROJECT)
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(COMMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_DATE.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(PROJECT.ID.eq(projectId))
                    .fetchOne();

            result = Statistic.builder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfCategories((Integer) record2.get("numberOfCategories"))
                    .numberOfRequirements((Integer) record2.get("numberOfRequirements"))
                    .numberOfComments((Integer) record3.get("numberOfComments"))
                    .numberOfAttachments((Integer) record3.get("numberOfAttachments"))
                    .numberOfVotes((Integer) record3.get("numberOfVotes"))
                    .build();

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }
}
