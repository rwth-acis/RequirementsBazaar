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

import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.*;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users.USERS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class UserRepositoryImpl extends RepositoryImpl<User, UsersRecord> implements UserRepository {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public UserRepositoryImpl(DSLContext jooq) {
        super(jooq, new UserTransformator());
    }

    final byte ONE = 1;

    @Override
    public Integer getIdByLas2PeerId(long las2PeerId) throws BazaarException {
        Integer id = null;
        try {
            id = jooq.selectFrom(USERS).where(USERS.LAS2PEER_ID.equal(las2PeerId)).fetchOne(USERS.ID);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return id;
    }

    @Override
    public List<User> getEmailReceiverForProject(int projectId) throws BazaarException {
        List<User> entries = null;
        try {
            entries = new ArrayList<>();

            // select distinct all project leader and follower
            List<Record> queryResults = jooq.selectDistinct(USERS.fields())
                    .from(USERS
                            .join(Projects.PROJECTS).on(USERS.ID.eq(Projects.PROJECTS.LEADER_ID)))
                    .where(Projects.PROJECTS.ID.eq(projectId))
                    .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE))

                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(ProjectFollower.PROJECT_FOLLOWER).on(USERS.ID.eq(ProjectFollower.PROJECT_FOLLOWER.USER_ID)))
                            .where(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.eq(projectId))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))
                    .fetch();

            for (Record queryResult : queryResults) {
                UsersRecord usersRecord = queryResult.into(UsersRecord.class);
                entries.add(transformator.getEntityFromTableRecord(usersRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }

    @Override
    public List<User> getEmailReceiverForComponent(int componentId) throws BazaarException {
        List<User> entries = null;
        try {
            entries = new ArrayList<>();

            // select distinct all followers union project leader union components leader
            List<Record> queryResults = jooq.selectDistinct(USERS.fields())
                    .from(USERS
                            .join(Components.COMPONENTS).on(USERS.ID.eq(Components.COMPONENTS.LEADER_ID)))
                    .where(Components.COMPONENTS.ID.eq(componentId))
                    .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE))

                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(ComponentFollower.COMPONENT_FOLLOWER).on(USERS.ID.eq(ComponentFollower.COMPONENT_FOLLOWER.USER_ID)))
                            .where(ComponentFollower.COMPONENT_FOLLOWER.COMPONENT_ID.eq(componentId))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))

                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(Projects.PROJECTS).on(USERS.ID.eq(Projects.PROJECTS.LEADER_ID))
                                    .join(Components.COMPONENTS).on(Components.COMPONENTS.PROJECT_ID.eq(Projects.PROJECTS.ID)))
                            .where(Components.COMPONENTS.ID.eq(componentId))
                            .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE)))

                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(ProjectFollower.PROJECT_FOLLOWER).on(USERS.ID.eq(ProjectFollower.PROJECT_FOLLOWER.USER_ID))
                                    .join(Components.COMPONENTS).on(Components.COMPONENTS.PROJECT_ID.eq(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID)))
                            .where(Components.COMPONENTS.ID.eq(componentId))
                            .and(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.eq(Components.COMPONENTS.PROJECT_ID))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))

                    .fetch();

            for (Record queryResult : queryResults) {
                UsersRecord usersRecord = queryResult.into(UsersRecord.class);
                entries.add(transformator.getEntityFromTableRecord(usersRecord));
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

            // select distinct all followers union project leader union components leader union req leader
            List<Record> queryResults = jooq.selectDistinct(USERS.fields())
                    // req leader
                    .from(USERS
                            .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.LEAD_DEVELOPER_ID.eq(USERS.ID)))
                    .where(Requirements.REQUIREMENTS.ID.eq(requirementId))
                    .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE))

                    // req follower
                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(RequirementFollower.REQUIREMENT_FOLLOWER).on(USERS.ID.eq(RequirementFollower.REQUIREMENT_FOLLOWER.USER_ID)))
                            .where(RequirementFollower.REQUIREMENT_FOLLOWER.REQUIREMENT_ID.eq(requirementId))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))

                    // component leader
                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(Components.COMPONENTS).on(USERS.ID.eq(Components.COMPONENTS.LEADER_ID))
                                    .join(Tags.TAGS).on(Tags.TAGS.COMPONENTS_ID.eq(Components.COMPONENTS.ID)))
                            .where(Tags.TAGS.REQUIREMENTS_ID.eq(requirementId))
                            .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE)))

                    // component follower
                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(ComponentFollower.COMPONENT_FOLLOWER).on(USERS.ID.eq(ComponentFollower.COMPONENT_FOLLOWER.USER_ID))
                                    .join(Tags.TAGS).on(Tags.TAGS.COMPONENTS_ID.eq(ComponentFollower.COMPONENT_FOLLOWER.COMPONENT_ID))
                                    .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.ID.eq(Tags.TAGS.REQUIREMENTS_ID)))
                            .where(Requirements.REQUIREMENTS.ID.eq(requirementId))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))

                    // project leader
                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(Projects.PROJECTS).on(USERS.ID.eq(Projects.PROJECTS.LEADER_ID))
                                    .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.PROJECT_ID.eq(Projects.PROJECTS.ID)))
                            .where(Requirements.REQUIREMENTS.ID.eq(requirementId))
                            .and(USERS.EMAIL_LEAD_ITEMS.eq(ONE)))

                    // project follower
                    .union(jooq.selectDistinct(USERS.fields())
                            .from(USERS
                                    .join(ProjectFollower.PROJECT_FOLLOWER).on(USERS.ID.eq(ProjectFollower.PROJECT_FOLLOWER.USER_ID))
                                    .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.PROJECT_ID.eq(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID)))
                            .where(Requirements.REQUIREMENTS.ID.eq(requirementId))
                            .and(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.eq(Requirements.REQUIREMENTS.PROJECT_ID))
                            .and(USERS.EMAIL_FOLLOW_ITEMS.eq(ONE)))

                    .fetch();

            for (Record queryResult : queryResults) {
                UsersRecord usersRecord = queryResult.into(UsersRecord.class);
                entries.add(transformator.getEntityFromTableRecord(usersRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }
}
