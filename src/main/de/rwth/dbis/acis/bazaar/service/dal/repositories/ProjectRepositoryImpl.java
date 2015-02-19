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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects.PROJECTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users.USERS;

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
    public List<Project> findAllPublic(Pageable pageable) throws BazaarException {
        List<Project> entries = null;
        try {
            entries = new ArrayList<Project>();

            List<ProjectsRecord> queryResults = jooq.selectFrom(transformator.getTable())
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()))
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(transformator.getRecordClass());

            for (ProjectsRecord queryResult : queryResults) {
                Project entry = transformator.mapToEntity(queryResult);
                entries.add(entry);
            }
        } catch (Exception e){
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return entries;
    }

    @Override
    public List<Project> findAllPublicAndAuthorized(PageInfo pageable, long userId) throws BazaarException {
        List<Project> entries = null;
        try {
            entries = new ArrayList<Project>();

            //TODO only authorized projects?
            List<ProjectsRecord> queryResults = jooq.selectFrom(transformator.getTable()
//                    .leftOuterJoin(AUTHORIZATIONS).on(AUTHORIZATIONS.PROJECT_ID.equal(PROJECTS.ID))
//                    .join(USERS).on(AUTHORIZATIONS.USER_ID.equal(USERS.ID))
                    )
                    .where(PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar()).or(USERS.LAS2PEER_ID.equal(userId)))
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(transformator.getRecordClass());

            for (ProjectsRecord queryResult : queryResults) {
                Project entry = transformator.mapToEntity(queryResult);
                entries.add(entry);
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return entries;
    }
}
