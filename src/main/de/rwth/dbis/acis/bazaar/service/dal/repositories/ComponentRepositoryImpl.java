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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ComponentTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS;

public class ComponentRepositoryImpl extends RepositoryImpl<Component, ComponentsRecord> implements ComponentRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ComponentRepositoryImpl(DSLContext jooq) {
        super(jooq, new ComponentTransformator());
    }

    @Override
    public Component findById(int id) throws BazaarException {
        Component component = null;
        try {
            Users leaderUser = Users.USERS.as("leaderUser");

            Record queryResult = jooq.selectFrom(COMPONENTS
                    .join(leaderUser).on(leaderUser.ID.equal(COMPONENTS.LEADER_ID)))
                    .where(transformator.getTableId().equal(id))
                    .fetchOne();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            ComponentsRecord componentsRecord = queryResult.into(COMPONENTS);
            component = transformator.getEntityFromTableRecord(componentsRecord);
            UserTransformator userTransformator = new UserTransformator();
            UsersRecord usersRecord = queryResult.into(leaderUser);
            component.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return component;
    }

    @Override
    public List<Component> findByProjectId(int projectId, Pageable pageable) throws BazaarException {
        List<Component> components = null;
        try {
            components = new ArrayList<Component>();
            Users leaderUser = Users.USERS.as("leaderUser");

            List<Record> queryResults = jooq.selectFrom(COMPONENTS
                    .join(leaderUser).on(leaderUser.ID.equal(COMPONENTS.LEADER_ID)))
                    .where(COMPONENTS.PROJECT_ID.equal(projectId))
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ComponentsRecord componentsRecord = queryResult.into(COMPONENTS);
                Component component = transformator.getEntityFromTableRecord(componentsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = queryResult.into(leaderUser);
                component.setLeader(userTransformator.getEntityFromTableRecord(usersRecord));
                components.add(component);
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return components;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {

            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformator.getTable())
                    .join(Projects.PROJECTS).on(Projects.PROJECTS.ID.eq(COMPONENTS.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(Projects.PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }
}
