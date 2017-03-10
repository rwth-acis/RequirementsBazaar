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
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
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
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Component.COMPONENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollowerMap.COMPONENT_FOLLOWER_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Project.PROJECT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement.REQUIREMENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementComponentMap.REQUIREMENT_COMPONENT_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User.USER;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote.VOTE;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comment.COMMENT;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachment.ATTACHMENT;

public class ComponentRepositoryImpl extends RepositoryImpl<Component, ComponentRecord> implements ComponentRepository {
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
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User followerUsers = USER.as("followerUsers");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT.ID.equal(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID))
                    .where(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = DSL.select(DSL.count())
                    .from(COMPONENT_FOLLOWER_MAP)
                    .where(COMPONENT_FOLLOWER_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("followerCount");

            Result<Record> queryResult = jooq.select(COMPONENT.fields())
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(COMPONENT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(COMPONENT.LEADER_ID))
                    .leftOuterJoin(COMPONENT_FOLLOWER_MAP).on(COMPONENT_FOLLOWER_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .leftOuterJoin(followerUsers).on(COMPONENT_FOLLOWER_MAP.USER_ID.equal(followerUsers.ID))
                    .where(transformator.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Component.Builder builder = Component.getBuilder(queryResult.getValues(COMPONENT.NAME).get(0))
                    .description(queryResult.getValues(COMPONENT.DESCRIPTION).get(0))
                    .projectId(queryResult.getValues(COMPONENT.PROJECT_ID).get(0))
                    .id(queryResult.getValues(COMPONENT.ID).get(0))
                    .leaderId(queryResult.getValues(COMPONENT.LEADER_ID).get(0))
                    .creationTime(queryResult.getValues(COMPONENT.CREATION_TIME).get(0))
                    .lastupdated_time(queryResult.getValues(COMPONENT.LASTUPDATED_TIME).get(0));

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

            component = builder.build();

            // Filling additional information
            component.setNumberOfRequirements((Integer) queryResult.getValues(requirementCount).get(0));
            component.setNumberOfFollowers((Integer) queryResult.getValues(followerCount).get(0));

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
            components = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMPONENT)
                    .where(COMPONENT.PROJECT_ID.equal(projectId))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT.ID.equal(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID))
                    .where(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(COMPONENT_FOLLOWER_MAP)
                    .where(COMPONENT_FOLLOWER_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("followerCount");

            List<Record> queryResults = jooq.select(COMPONENT.fields())
                    .select(idCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(COMPONENT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(COMPONENT.LEADER_ID))
                    .where(COMPONENT.PROJECT_ID.equal(projectId))
                    .and(transformator.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ComponentRecord componentRecord = queryResult.into(COMPONENT);
                Component component = transformator.getEntityFromTableRecord(componentRecord);
                UserTransformator userTransformator = new UserTransformator();
                UserRecord userRecord = queryResult.into(leaderUser);
                component.setLeader(userTransformator.getEntityFromTableRecord(userRecord));
                component.setNumberOfRequirements((Integer) queryResult.getValue(requirementCount));
                component.setNumberOfFollowers((Integer) queryResult.getValue(followerCount));
                components.add(component);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, components);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Component> findByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<Component> result = null;
        List<Component> components;
        try {
            components = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(COMPONENT)
                    .join(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .where(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT.ID.equal(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID))
                    .where(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(COMPONENT_FOLLOWER_MAP)
                    .where(COMPONENT_FOLLOWER_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .asField("followerCount");

            List<Record> queryResults = jooq.select(COMPONENT.fields())
                    .select(idCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(leaderUser.fields())
                    .from(COMPONENT)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(COMPONENT.LEADER_ID))
                    .leftOuterJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .where(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID.equal(requirementId))
                    .orderBy(transformator.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                ComponentRecord componentRecord = queryResult.into(COMPONENT);
                Component component = transformator.getEntityFromTableRecord(componentRecord);
                UserTransformator userTransformator = new UserTransformator();
                UserRecord userRecord = queryResult.into(leaderUser);
                component.setLeader(userTransformator.getEntityFromTableRecord(userRecord));
                component.setNumberOfRequirements((Integer) queryResult.getValue(requirementCount));
                component.setNumberOfFollowers((Integer) queryResult.getValue(followerCount));
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
                    .join(PROJECT).on(PROJECT.ID.eq(COMPONENT.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public Statistic getStatisticsForComponent(int userId, int componentId, Timestamp timestamp) throws BazaarException {
        Statistic result = null;
        try {
            // If you want to change something here, please know what you are doing! Its SQL and even worse JOOQ :-|
            Record record1 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .from(COMPONENT)
                    .leftJoin(PROJECT).on(PROJECT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(PROJECT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(PROJECT.ID.equal(COMPONENT.PROJECT_ID)))
                    .where(COMPONENT.ID.eq(componentId))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(COMPONENT.ID).as("numberOfComponents"))
                    .from(COMPONENT)
                    .where(COMPONENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(COMPONENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(COMPONENT.ID.eq(componentId)))
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(COMPONENT)
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.ID.equal(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID)))
                    .where(COMPONENT.ID.eq(componentId))
                    .fetchOne();

            Record record4 = jooq
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(COMPONENT)
                    .leftJoin(REQUIREMENT_COMPONENT_MAP).on(REQUIREMENT_COMPONENT_MAP.COMPONENT_ID.equal(COMPONENT.ID))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(REQUIREMENT_COMPONENT_MAP.REQUIREMENT_ID))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(COMMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_TIME.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LASTUPDATED_TIME.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_TIME.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(COMPONENT.ID.eq(componentId))
                    .fetchOne();

            result = Statistic.getBuilder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfComponents((Integer) record2.get("numberOfComponents"))
                    .numberOfRequirements((Integer) record3.get("numberOfRequirements"))
                    .numberOfComments((Integer) record4.get("numberOfComments"))
                    .numberOfAttachments((Integer) record4.get("numberOfAttachments"))
                    .numberOfVotes((Integer) record4.get("numberOfVotes"))
                    .build();

        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }
}
