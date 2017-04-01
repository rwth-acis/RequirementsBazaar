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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQL;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.*;
/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class UserRepositoryImpl extends RepositoryImpl<User, UserRecord> implements UserRepository {
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
            id = jooq.selectFrom(USER).where(USER.LAS2PEER_ID.equal(las2PeerId)).fetchOne(USER.ID);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return id;
    }

    @Override
    public void updateLastLoginDate(int userId) throws Exception {
        try {
            jooq.update(USER).set(USER.LAST_LOGIN_DATE, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime())).execute();
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
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
                entries.add(transformator.getEntityFromTableRecord(userRecord));
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
                entries.add(transformator.getEntityFromTableRecord(userRecord));
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
                entries.add(transformator.getEntityFromTableRecord(userRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }
}
