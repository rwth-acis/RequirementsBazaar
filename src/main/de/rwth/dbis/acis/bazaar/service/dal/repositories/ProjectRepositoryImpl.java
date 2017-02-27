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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.*;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectTransformator;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.COMPONENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.REQUIREMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.PROJECT_FOLLOWER;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects.PROJECTS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ProjectRepositoryImpl extends RepositoryImpl<Project, ProjectsRecord> implements ProjectRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ProjectRepositoryImpl(DSLContext jooq) {
        super(jooq, new ProjectTransformator());
    }

    @Override
    public Project findById(int id) throws BazaarException {
        Project project = null;
        try {
            Users leaderUser = Users.USERS.as("leaderUser");
            Users followerUsers = Users.USERS.as("followerUsers");

            Field<Object> componentCount = jooq.select(DSL.count())
                    .from(COMPONENTS)
                    .where(COMPONENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("componentCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENTS)
                    .where(REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(ProjectFollower.PROJECT_FOLLOWER)
                    .where(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("followerCount");

            Result<Record> queryResult = jooq.select(PROJECTS.fields())
                    .select(componentCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(PROJECTS)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID))
                    .leftOuterJoin(PROJECT_FOLLOWER).on(PROJECT_FOLLOWER.PROJECT_ID.equal(PROJECTS.ID))
                    .leftOuterJoin(followerUsers).on(followerUsers.ID.equal(PROJECT_FOLLOWER.USER_ID))
                    .where(transformator.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Project.Builder builder = Project.getBuilder(queryResult.getValues(PROJECTS.NAME).get(0))
                    .description(queryResult.getValues(PROJECTS.DESCRIPTION).get(0))
                    .id(queryResult.getValues(PROJECTS.ID).get(0))
                    .leaderId(queryResult.getValues(PROJECTS.LEADER_ID).get(0))
                    .defaultComponentId(queryResult.getValues(PROJECTS.DEFAULT_COMPONENTS_ID).get(0))
                    .visibility(Project.ProjectVisibility.getVisibility(queryResult.getValues(PROJECTS.VISIBILITY).get(0)))
                    .creationTime(queryResult.getValues(PROJECTS.CREATION_TIME).get(0))
                    .lastupdatedTime(queryResult.getValues(PROJECTS.LASTUPDATED_TIME).get(0));

            UserTransformator userTransformator = new UserTransformator();
            //Filling up LeadDeveloper
            builder.leader(userTransformator.getEntityFromQueryResult(leaderUser, queryResult));

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

            project = builder.build();

            // Filling additional information TODO: add other additional informations here (leaddev, followers)
            project.setNumberOfComponents((Integer) queryResult.getValues(componentCount).get(0));
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
            Users leaderUser = Users.USERS.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(PROJECTS)
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> componentCount = jooq.select(DSL.count())
                    .from(COMPONENTS)
                    .where(COMPONENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("componentCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENTS)
                    .where(REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(ProjectFollower.PROJECT_FOLLOWER)
                    .where(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("followerCount");

            Result<Record> queryResults = jooq.select(PROJECTS.fields())
                    .select(idCount)
                    .select(componentCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(PROJECTS)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID))
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ProjectsRecord projectsRecord = queryResult.into(PROJECTS);
                Project project = transformator.getEntityFromTableRecord(projectsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = queryResult.into(leaderUser);
                project.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));
                project.setNumberOfComponents((Integer) queryResult.getValue(componentCount));
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
            Users leaderUser = Users.USERS.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(PROJECTS)
                    .where(transformator.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> componentCount = jooq.select(DSL.count())
                    .from(COMPONENTS)
                    .where(COMPONENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("componentCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENTS)
                    .where(REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(ProjectFollower.PROJECT_FOLLOWER)
                    .where(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.equal(PROJECTS.ID))
                    .asField("followerCount");

            //TODO only authorized projects?
            List<Record> queryResults = jooq.select(PROJECTS.fields())
                    .select(idCount)
                    .select(componentCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(PROJECTS)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID))
//                    .leftOuterJoin(AUTHORIZATIONS).on(AUTHORIZATIONS.PROJECT_ID.equal(PROJECTS.ID))
//                    .join(USERS).on(AUTHORIZATIONS.USER_ID.equal(USERS.ID))
//                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())
                    .where(transformator.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ProjectsRecord projectsRecord = queryResult.into(PROJECTS);
                Project project = transformator.getEntityFromTableRecord(projectsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = queryResult.into(leaderUser);
                project.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));
                project.setNumberOfComponents((Integer) queryResult.getValue(componentCount));
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
                    .from(transformator.getTable())
                    .where(transformator.getTableId().eq(id).and(Projects.PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())))
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
                    .select(DSL.countDistinct(PROJECTS.ID).as("numberOfProjects"))
                    .from(PROJECTS)
                    .where(PROJECTS.VISIBILITY.eq("+"))
                            .and(PROJECTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(PROJECTS.LASTUPDATED_TIME.greaterOrEqual(timestamp)))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(Components.COMPONENTS.ID).as("numberOfComponents"))
                    .select(DSL.countDistinct(Requirements.REQUIREMENTS.ID).as("numberOfRequirements"))
                    .from(PROJECTS)
                    .leftJoin(Components.COMPONENTS).on(Components.COMPONENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Components.COMPONENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Components.COMPONENTS.PROJECT_ID.equal(PROJECTS.ID)))
                    .leftJoin(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Requirements.REQUIREMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Requirements.REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID)))
                    .where(PROJECTS.VISIBILITY.eq("+"))
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(Comments.COMMENTS.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(Attachments.ATTACHMENTS.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(Votes.VOTES.ID).as("numberOfVotes"))
                    .from(PROJECTS)
                    .leftJoin(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .leftJoin(Comments.COMMENTS).on(Comments.COMMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Comments.COMMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Comments.COMMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Attachments.ATTACHMENTS).on(Attachments.ATTACHMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Attachments.ATTACHMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Votes.VOTES).on(Votes.VOTES.CREATION_TIME.greaterOrEqual(timestamp)
                            .and(Votes.VOTES.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .where(PROJECTS.VISIBILITY.eq("+"))
                    .fetchOne();

            result = Statistic.getBuilder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfComponents((Integer) record2.get("numberOfComponents"))
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
                    .select(DSL.countDistinct(PROJECTS.ID).as("numberOfProjects"))
                    .from(PROJECTS)
                    .where(PROJECTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(PROJECTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(PROJECTS.ID.eq(projectId)))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(PROJECTS.ID).as("numberOfProjects"))
                    .select(DSL.countDistinct(Components.COMPONENTS.ID).as("numberOfComponents"))
                    .select(DSL.countDistinct(Requirements.REQUIREMENTS.ID).as("numberOfRequirements"))
                    .from(PROJECTS)
                    .leftJoin(Components.COMPONENTS).on(Components.COMPONENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Components.COMPONENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Components.COMPONENTS.PROJECT_ID.equal(PROJECTS.ID)))
                    .leftJoin(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Requirements.REQUIREMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Requirements.REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID)))
                    .where(PROJECTS.ID.eq(projectId))
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(Comments.COMMENTS.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(Attachments.ATTACHMENTS.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(Votes.VOTES.ID).as("numberOfVotes"))
                    .from(PROJECTS)
                    .leftJoin(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                    .leftJoin(Comments.COMMENTS).on(Comments.COMMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Comments.COMMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Comments.COMMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Attachments.ATTACHMENTS).on(Attachments.ATTACHMENTS.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(Attachments.ATTACHMENTS.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .leftJoin(Votes.VOTES).on(Votes.VOTES.CREATION_TIME.greaterOrEqual(timestamp)
                            .and(Votes.VOTES.REQUIREMENT_ID.equal(Requirements.REQUIREMENTS.ID)))
                    .where(PROJECTS.ID.eq(projectId))
                    .fetchOne();

            result = Statistic.getBuilder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfComponents((Integer) record2.get("numberOfComponents"))
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
