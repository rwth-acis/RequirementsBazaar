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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;

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
        super(jooq, new UserTransformer());
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
    public PaginationResult<User> findAllByContribution(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<User> result = null;
        List<User> users;
        try {
            users = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount().from(
                    jooq.select(USER.fields())
                            .from(USER)
                            .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.CREATOR_ID.equal(USER.ID))
                            .where(REQUIREMENT.ID.equal(requirementId))
                            .union(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.LEAD_DEVELOPER_ID.equal(USER.ID))
                                            .where(REQUIREMENT.ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                                            .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(COMMENT).on(COMMENT.USER_ID.equal(USER.ID))
                                            .where(COMMENT.REQUIREMENT_ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.USER_ID.equal(USER.ID))
                                            .where(ATTACHMENT.REQUIREMENT_ID.equal(requirementId))
                            )
            )
                    .asField("idCount");

            List<Record> queryResults =
                    jooq.select(USER.fields())
                            .select(idCount)
                            .from(USER)
                            .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.CREATOR_ID.equal(USER.ID))
                            .where(REQUIREMENT.ID.equal(requirementId))
                            .union(
                                    jooq.select(USER.fields())
                                            .select(idCount)
                                            .from(USER)
                                            .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.LEAD_DEVELOPER_ID.equal(USER.ID))
                                            .where(REQUIREMENT.ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .select(idCount)
                                            .from(USER)
                                            .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                                            .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .select(idCount)
                                            .from(USER)
                                            .leftOuterJoin(COMMENT).on(COMMENT.USER_ID.equal(USER.ID))
                                            .where(COMMENT.REQUIREMENT_ID.equal(requirementId))
                            )
                            .union(
                                    jooq.select(USER.fields())
                                            .select(idCount)
                                            .from(USER)
                                            .leftOuterJoin(ATTACHMENT).on(ATTACHMENT.USER_ID.equal(USER.ID))
                                            .where(ATTACHMENT.REQUIREMENT_ID.equal(requirementId))
                            )
                            .limit(pageable.getPageSize())
                            .offset(pageable.getOffset())
                            .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                users.add(transformer.getEntityFromTableRecord(userRecord));
            }

            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, users);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<User> findAllByDeveloping(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<User> result = null;
        List<User> users;
        try {
            users = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(USER)
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                    .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(USER.fields())
                    .select(idCount)
                    .from(USER)
                    .leftOuterJoin(REQUIREMENT_DEVELOPER_MAP).on(REQUIREMENT_DEVELOPER_MAP.USER_ID.equal(USER.ID))
                    .where(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID.equal(requirementId))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                users.add(transformer.getEntityFromTableRecord(userRecord));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, users);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<User> findAllByFollowing(int projectId, int categoryId, int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<User> result = null;
        List<User> users;
        try {
            users = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(jooq.selectDistinct()
                            .from(
                                    jooq.select(USER.fields())
                                            .from(USER)
                                            .leftOuterJoin(PROJECT_FOLLOWER_MAP).on(PROJECT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .leftOuterJoin(CATEGORY_FOLLOWER_MAP).on(CATEGORY_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .leftOuterJoin(REQUIREMENT_FOLLOWER_MAP).on(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                            .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(projectId))
                                            .or(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId))
                                            .or(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId))
                                            .asTable("inner"))
                    ).asField("idCount");

            List<Record> queryResults = jooq.selectDistinct()
                    .from(
                            jooq.select(USER.fields())
                                    .select(idCount)
                                    .from(USER)
                                    .leftOuterJoin(PROJECT_FOLLOWER_MAP).on(PROJECT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .leftOuterJoin(CATEGORY_FOLLOWER_MAP).on(CATEGORY_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .leftOuterJoin(REQUIREMENT_FOLLOWER_MAP).on(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(USER.ID))
                                    .where(PROJECT_FOLLOWER_MAP.PROJECT_ID.equal(projectId))
                                    .or(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId))
                                    .or(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId))
                                    .asTable("inner"))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                UserRecord userRecord = queryResult.into(UserRecord.class);
                users.add(transformer.getEntityFromTableRecord(userRecord));
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, users);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
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
                entries.add(transformer.getEntityFromTableRecord(userRecord));
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
                entries.add(transformer.getEntityFromTableRecord(userRecord));
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
                entries.add(transformer.getEntityFromTableRecord(userRecord));
            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return entries;
    }
}
