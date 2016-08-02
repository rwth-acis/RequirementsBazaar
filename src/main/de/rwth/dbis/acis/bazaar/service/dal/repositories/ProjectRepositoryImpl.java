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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
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

            Record queryResult = jooq.selectFrom(PROJECTS
                    .join(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID)))
                    .where(transformator.getTableId().equal(id))
                    .fetchOne();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            ProjectsRecord projectsRecord = queryResult.into(PROJECTS);
            project = transformator.getEntityFromTableRecord(projectsRecord);
            UserTransformator userTransformator = new UserTransformator();
            UsersRecord usersRecord = queryResult.into(leaderUser);
            project.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));

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
        List<Project> projects = null;
        try {
            projects = new ArrayList<>();
            Users leaderUser = Users.USERS.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(PROJECTS)
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()))
                    .asField("idCount");

            Result<Record> queryResults = jooq.select(PROJECTS.fields()).select(leaderUser.fields()).select(idCount)
                    .from(PROJECTS)
                    .join(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID))
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()))
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ProjectsRecord projectsRecord = queryResult.into(PROJECTS);
                Project project = transformator.getEntityFromTableRecord(projectsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = queryResult.into(leaderUser);
                project.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));
                projects.add(project);
            }
            int total = ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, "", pageable, projects);

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Project> findAllPublicAndAuthorized(PageInfo pageable, long userId) throws BazaarException {
        List<Project> projects = null;
        try {
            projects = new ArrayList<>();
            Users leaderUser = Users.USERS.as("leaderUser");

            //TODO only authorized projects?
            List<Record> queryResults = jooq.selectFrom(PROJECTS
                    .join(leaderUser).on(leaderUser.ID.equal(PROJECTS.LEADER_ID)))
//                    .leftOuterJoin(AUTHORIZATIONS).on(AUTHORIZATIONS.PROJECT_ID.equal(PROJECTS.ID))
//                    .join(USERS).on(AUTHORIZATIONS.USER_ID.equal(USERS.ID))
//                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ProjectsRecord projectsRecord = queryResult.into(PROJECTS);
                Project project = transformator.getEntityFromTableRecord(projectsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = queryResult.into(leaderUser);
                project.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));
                projects.add(project);
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return null;
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
}
