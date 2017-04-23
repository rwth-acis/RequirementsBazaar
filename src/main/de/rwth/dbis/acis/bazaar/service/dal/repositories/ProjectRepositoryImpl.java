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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectTransformer;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ProjectRepositoryImpl extends RepositoryImpl<Project, ProjectRecord> implements ProjectRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ProjectRepositoryImpl(DSLContext jooq) {
        super(jooq, new ProjectTransformer());
    }

    @Override
    public Project findById(int id) throws BazaarException {
        Project project = null;
        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User followerUsers = USER.as("followerUsers");

            Field<Object> categoryCount = jooq.select(DSL.count())
                    .from(CATEGORY)
                    .where(CATEGORY.PROJECT_ID.equal(PROJECT.ID))
                    .asField("categoryCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .where(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(PROJECT_FOLLOWER_MAP)
                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
                    .asField("followerCount");

            Result<Record> queryResult = jooq.select(PROJECT.fields())
                    .select(categoryCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .select(followerUsers.fields())
                    .from(PROJECT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECT.LEADER_ID))
                    .leftOuterJoin(PROJECT_FOLLOWER_MAP).on(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
                    .leftOuterJoin(followerUsers).on(followerUsers.ID.equal(PROJECT_FOLLOWER_MAP.USER_ID))
                    .where(transformer.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Project.Builder builder = Project.getBuilder(queryResult.getValues(PROJECT.NAME).get(0))
                    .description(queryResult.getValues(PROJECT.DESCRIPTION).get(0))
                    .id(queryResult.getValues(PROJECT.ID).get(0))
                    .defaultCategoryId(queryResult.getValues(PROJECT.DEFAULT_CATEGORY_ID).get(0))
                    .visibility(queryResult.getValues(PROJECT.VISIBILITY).get(0) == 1)
                    .creationDate(queryResult.getValues(PROJECT.CREATION_DATE).get(0))
                    .lastUpdatedDate(queryResult.getValues(PROJECT.LAST_UPDATED_DATE).get(0));

            UserTransformer userTransformer = new UserTransformer();
            //Filling up LeadDeveloper
            builder.leader(userTransformer.getEntityFromQueryResult(leaderUser, queryResult));

            //Filling up follower list
            List<User> followers = new ArrayList<>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(followerUsers.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                followers.add(
                        userTransformer.getEntityFromQueryResult(followerUsers, records)
                );
            }
            builder.followers(followers);

            project = builder.build();

            // Filling additional information TODO: add other additional information here (leaddev, followers)
            project.setNumberOfCategories((Integer) queryResult.getValues(categoryCount).get(0));
            project.setNumberOfRequirements((Integer) queryResult.getValues(requirementCount).get(0));
            project.setNumberOfFollowers((Integer) queryResult.getValues(followerCount).get(0));

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return project;
    }

    @Override
    public PaginationResult<Project> findAllPublic(Pageable pageable) throws BazaarException {
        PaginationResult<Project> result = null;
        List<Project> projects;
        try {
            projects = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(PROJECT)
                    .where(PROJECT.VISIBILITY.isTrue())
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> categoryCount = jooq.select(DSL.count())
                    .from(CATEGORY)
                    .where(CATEGORY.PROJECT_ID.equal(PROJECT.ID))
                    .asField("categoryCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .where(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(PROJECT_FOLLOWER_MAP)
                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
                    .asField("followerCount");

            Result<Record> queryResults = jooq.select(PROJECT.fields())
                    .select(idCount)
                    .select(categoryCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(PROJECT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECT.LEADER_ID))
                    .where(PROJECT.VISIBILITY.isTrue())
                    .and(transformer.getSearchCondition(pageable.getSearch()))
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
                project.setNumberOfCategories((Integer) queryResult.getValue(categoryCount));
                project.setNumberOfRequirements((Integer) queryResult.getValue(requirementCount));
                project.setNumberOfFollowers((Integer) queryResult.getValue(followerCount));
                projects.add(project);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, projects);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Project> findAllPublicAndAuthorized(PageInfo pageable, long userId) throws BazaarException {
        PaginationResult<Project> result = null;
        List<Project> projects;
        try {
            projects = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(PROJECT)
                    .where(transformer.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> categoryCount = jooq.select(DSL.count())
                    .from(CATEGORY)
                    .where(CATEGORY.PROJECT_ID.equal(PROJECT.ID))
                    .asField("categoryCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .where(REQUIREMENT.PROJECT_ID.equal(PROJECT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(PROJECT_FOLLOWER_MAP)
                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(PROJECT.ID))
                    .asField("followerCount");

            //TODO only authorized projects?
            List<Record> queryResults = jooq.select(PROJECT.fields())
                    .select(idCount)
                    .select(categoryCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(PROJECT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECT.LEADER_ID))
//                    .leftOuterJoin(AUTHORIZATIONS).on(AUTHORIZATIONS.PROJECT_ID.equal(PROJECTS.ID))
//                    .join(USERS).on(AUTHORIZATIONS.USER_ID.equal(USERS.ID))
//                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())
                    .where(transformer.getSearchCondition(pageable.getSearch()))
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
                project.setNumberOfCategories((Integer) queryResult.getValue(categoryCount));
                project.setNumberOfRequirements((Integer) queryResult.getValue(requirementCount));
                project.setNumberOfFollowers((Integer) queryResult.getValue(followerCount));
                projects.add(project);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, projects);
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
                    .where(transformer.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public Statistic getStatisticsForVisibleProjects(int userId, java.sql.Timestamp timestamp) throws BazaarException {
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

            result = Statistic.getBuilder()
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
    public Statistic getStatisticsForProject(int userId, int projectId, java.sql.Timestamp timestamp) throws BazaarException {
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

            result = Statistic.getBuilder()
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
