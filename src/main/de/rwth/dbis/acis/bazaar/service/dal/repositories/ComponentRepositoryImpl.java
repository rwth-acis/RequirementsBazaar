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
import de.rwth.dbis.acis.bazaar.service.dal.entities.ComponentFollower;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
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
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollower.COMPONENT_FOLLOWER;

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
            Users followerUsers = Users.USERS.as("followerUsers");

            Result<Record> queryResult = jooq.select()
                    .from(COMPONENTS)
                    .join(leaderUser).on(leaderUser.ID.equal(COMPONENTS.LEADER_ID))

                    .leftOuterJoin(COMPONENT_FOLLOWER).on(COMPONENT_FOLLOWER.COMPONENT_ID.equal(COMPONENTS.ID))
                    .leftOuterJoin(followerUsers).on(COMPONENT_FOLLOWER.USER_ID.equal(followerUsers.ID))

                    .where(transformator.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Component.Builder builder = Component.getBuilder(queryResult.getValues(COMPONENTS.NAME).get(0))
                    .description(queryResult.getValues(COMPONENTS.DESCRIPTION).get(0))
                    .projectId(queryResult.getValues(COMPONENTS.PROJECT_ID).get(0))
                    .id(queryResult.getValues(COMPONENTS.ID).get(0))
                    .leaderId(queryResult.getValues(COMPONENTS.LEADER_ID).get(0))
                    .creationTime(queryResult.getValues(COMPONENTS.CREATION_TIME).get(0))
                    .lastupdated_time(queryResult.getValues(COMPONENTS.LASTUPDATED_TIME).get(0));

            UserTransformator userTransformator = new UserTransformator();
            //Filling up LeadDeveloper
            builder.leader(userTransformator.getEntityFromQueryResult(leaderUser, queryResult));

            //Filling up follower list
            List<User> followers = new ArrayList<User>();
            for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(followerUsers.ID).entrySet()) {
                if (entry.getKey() == null) continue;
                Result<Record> records = entry.getValue();
                followers.add(
                        userTransformator.getEntityFromQueryResult(followerUsers, records)
                );
            }
            builder.followers(followers);

            component = builder.build();

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return component;
    }

    @Override
    public PaginationResult<Component> findByProjectId(int projectId, Pageable pageable) throws BazaarException {
        PaginationResult<Component> result = null;
        List<Component> components;
        try {
            components = new ArrayList<Component>();
            Users leaderUser = Users.USERS.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMPONENTS)
                    .where(COMPONENTS.PROJECT_ID.equal(projectId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(COMPONENTS.fields()).select(leaderUser.fields()).select(idCount)
                    .from(COMPONENTS)
                    .join(leaderUser).on(leaderUser.ID.equal(COMPONENTS.LEADER_ID))
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
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, components);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
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
