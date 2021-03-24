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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.CategoryRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Category;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CategoryTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;
import static org.jooq.impl.DSL.*;

public class CategoryRepositoryImpl extends RepositoryImpl<Category, CategoryRecord> implements CategoryRepository {

    // derived table for activities inside category
    public static final Table<?> ACTIVITY = table(
            select(CATEGORY.ID, CATEGORY.CREATION_DATE)
                    .from(CATEGORY)
                    .unionAll(
                            select(CATEGORY.ID, CATEGORY.LAST_UPDATED_DATE)
                                    .from(CATEGORY))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, REQUIREMENT.CREATION_DATE)
                                    .from(REQUIREMENT)
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, REQUIREMENT.LAST_UPDATED_DATE)
                                    .from(REQUIREMENT)
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, COMMENT.CREATION_DATE)
                                    .from(COMMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(COMMENT.REQUIREMENT_ID))
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, COMMENT.LAST_UPDATED_DATE)
                                    .from(COMMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(COMMENT.REQUIREMENT_ID))
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, ATTACHMENT.CREATION_DATE)
                                    .from(ATTACHMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(ATTACHMENT.REQUIREMENT_ID))
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .unionAll(
                            select(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID, ATTACHMENT.LAST_UPDATED_DATE)
                                    .from(ATTACHMENT)
                                    .leftOuterJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(ATTACHMENT.REQUIREMENT_ID))
                                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
    ).as("ACTIVITY");

    // derived table for last activity inside category
    public static final Table<?> LAST_ACTIVITY = table(
            select(
                    ACTIVITY.field(CATEGORY.ID),
                    max(ACTIVITY.field(CATEGORY.CREATION_DATE)).as("last_activity"))
                    .from(ACTIVITY)
                    .groupBy(ACTIVITY.field(CATEGORY.ID)))
            .as("last_activity");

    public static final Field<Object> REQUIREMENT_COUNT = select(DSL.count())
            .from(REQUIREMENT)
            .leftJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT.ID.equal(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
            .where(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
            .asField("requirementCount");

    public static final Field<Object> FOLLOWER_COUNT = select(DSL.count())
            .from(CATEGORY_FOLLOWER_MAP)
            .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID))
            .asField("followerCount");

    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public CategoryRepositoryImpl(DSLContext jooq) {
        super(jooq, new CategoryTransformer());
    }

    @Override
    public Category findById(int id, int userId) throws BazaarException {
        Category category = null;
        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID).and(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            Result<Record> queryResult = jooq.select(CATEGORY.fields())
                    .select(REQUIREMENT_COUNT)
                    .select(FOLLOWER_COUNT)
                    .select(isFollower)
                    .select(leaderUser.fields())
                    .from(CATEGORY)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(CATEGORY.LEADER_ID))
                    .where(transformer.getTableId().equal(id))
                    .fetch();

            if (queryResult == null || queryResult.size() == 0) {
                ExceptionHandler.getInstance().convertAndThrowException(
                        new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                        ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }

            Category.Builder builder = Category.builder()
                    .name(queryResult.getValues(CATEGORY.NAME).get(0))
                    .description(queryResult.getValues(CATEGORY.DESCRIPTION).get(0))
                    .projectId(queryResult.getValues(CATEGORY.PROJECT_ID).get(0))
                    .id(queryResult.getValues(CATEGORY.ID).get(0))
                    .creationDate(queryResult.getValues(CATEGORY.CREATION_DATE).get(0))
                    .lastUpdatedDate(queryResult.getValues(CATEGORY.LAST_UPDATED_DATE).get(0));

            UserTransformer userTransformer = new UserTransformer();
            //Filling up LeadDeveloper
            builder.creator(userTransformer.getEntityFromQueryResult(leaderUser, queryResult));

            category = builder.build();

            // Filling additional information
            category.setNumberOfRequirements((Integer) queryResult.getValues(REQUIREMENT_COUNT).get(0));
            category.setNumberOfFollowers((Integer) queryResult.getValues(FOLLOWER_COUNT).get(0));
            if (userId != 1) {
                category.setIsFollower(0 != (Integer) queryResult.getValues(isFollower).get(0));
            }

        } catch (BazaarException be) {
            ExceptionHandler.getInstance().convertAndThrowException(be);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return category;
    }

    @Override
    public PaginationResult<Category> findByProjectId(int projectId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Category> result = null;
        List<Category> categories;
        try {
            categories = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(CATEGORY)
                    .where(CATEGORY.PROJECT_ID.equal(projectId))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID).and(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            List<Record> queryResults = jooq.select(CATEGORY.fields())
                    .select(idCount)
                    .select(REQUIREMENT_COUNT)
                    .select(FOLLOWER_COUNT)
                    .select(isFollower)
                    .select(leaderUser.fields())
                    .from(CATEGORY)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(CATEGORY.LEADER_ID))
                    .leftOuterJoin(LAST_ACTIVITY).on(CATEGORY.ID.eq(LAST_ACTIVITY.field(CATEGORY.ID)))
                    .where(CATEGORY.PROJECT_ID.equal(projectId))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                CategoryRecord categoryRecord = queryResult.into(CATEGORY);
                Category category = transformer.getEntityFromTableRecord(categoryRecord);
                UserTransformer userTransformer = new UserTransformer();
                UserRecord userRecord = queryResult.into(leaderUser);
                category.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
                category.setNumberOfRequirements((Integer) queryResult.getValue(REQUIREMENT_COUNT));
                category.setNumberOfFollowers((Integer) queryResult.getValue(FOLLOWER_COUNT));
                if (userId != 1) {
                    category.setIsFollower(0 != (Integer) queryResult.getValue(isFollower));
                }
                categories.add(category);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, categories);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public PaginationResult<Category> findAll(Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Category> result = null;
        List<Category> categories;
        try {
            categories = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(CATEGORY)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .asField("idCount");

            Field<Object> requirementCount = jooq.select(DSL.count())
                    .from(REQUIREMENT)
                    .leftJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT.ID.equal(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                    .where(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .asField("requirementCount");

            Field<Object> followerCount = jooq.select(DSL.count())
                    .from(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .asField("followerCount");

            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID).and(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            List<Record> queryResults = jooq.select(CATEGORY.fields())
                    .select(idCount)
                    .select(requirementCount)
                    .select(followerCount)
                    .select(isFollower)
                    .select(leaderUser.fields())
                    .from(CATEGORY)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(CATEGORY.LEADER_ID))
                    .leftOuterJoin(LAST_ACTIVITY).on(CATEGORY.ID.eq(LAST_ACTIVITY.field(CATEGORY.ID)))
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                CategoryRecord categoryRecord = queryResult.into(CATEGORY);
                Category category = transformer.getEntityFromTableRecord(categoryRecord);
                UserTransformer userTransformer = new UserTransformer();
                UserRecord userRecord = queryResult.into(leaderUser);
                category.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
                category.setNumberOfRequirements((Integer) queryResult.getValue(requirementCount));
                category.setNumberOfFollowers((Integer) queryResult.getValue(followerCount));
                if (userId != 1) {
                    category.setIsFollower(0 != (Integer) queryResult.getValue(isFollower));
                }
                categories.add(category);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, categories);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }


    @Override
    public List<Integer> listAllCategoryIds(Pageable pageable, int userId) throws BazaarException {
        List<Integer> categoryIds = new ArrayList<>();
        try {
            categoryIds = jooq.select()
                    .from(CATEGORY)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    .and(transformer.getSearchCondition(pageable.getSearch()))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    //       .limit(pageable.getPageSize())
                    //       .offset(pageable.getOffset())
                    .fetch(CATEGORY.ID);

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return categoryIds;
    }




    @Override
    public PaginationResult<Category> findByRequirementId(int requirementId, Pageable pageable, int userId) throws BazaarException {
        PaginationResult<Category> result = null;
        List<Category> categories;
        try {
            categories = new ArrayList<>();
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User leaderUser = USER.as("leaderUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(CATEGORY)
                    .join(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .where(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            Field<Object> isFollower = DSL.select(DSL.count())
                    .from(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(CATEGORY.ID).and(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId)))
                    .asField("isFollower");

            List<Record> queryResults = jooq.select(CATEGORY.fields())
                    .select(idCount)
                    .select(REQUIREMENT_COUNT)
                    .select(FOLLOWER_COUNT)
                    .select(isFollower)
                    .select(leaderUser.fields())
                    .from(CATEGORY)
                    .leftOuterJoin(leaderUser).on(leaderUser.ID.equal(CATEGORY.LEADER_ID))
                    .leftOuterJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .where(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID.equal(requirementId))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                CategoryRecord categoryRecord = queryResult.into(CATEGORY);
                Category category = transformer.getEntityFromTableRecord(categoryRecord);
                UserTransformer userTransformer = new UserTransformer();
                UserRecord userRecord = queryResult.into(leaderUser);
                category.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
                category.setNumberOfRequirements((Integer) queryResult.getValue(REQUIREMENT_COUNT));
                category.setNumberOfFollowers((Integer) queryResult.getValue(FOLLOWER_COUNT));
                if (userId != 1) {
                    category.setIsFollower((Integer) queryResult.getValue(isFollower) != 0);
                }
                categories.add(category);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, categories);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {
            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformer.getTable())
                    .join(PROJECT).on(PROJECT.ID.eq(CATEGORY.PROJECT_ID))
                    .where(transformer.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }

    @Override
    public Statistic getStatisticsForCategory(int userId, int categoryId, LocalDateTime timestamp) throws BazaarException {
        Statistic result = null;
        try {
            // If you want to change something here, please know what you are doing! Its SQL and even worse JOOQ :-|
            Record record1 = jooq
                    .select(DSL.countDistinct(PROJECT.ID).as("numberOfProjects"))
                    .from(CATEGORY)
                    .leftJoin(PROJECT).on(PROJECT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(PROJECT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(PROJECT.ID.equal(CATEGORY.PROJECT_ID)))
                    .where(CATEGORY.ID.eq(categoryId))
                    .fetchOne();

            Record record2 = jooq
                    .select(DSL.countDistinct(CATEGORY.ID).as("numberOfCategories"))
                    .from(CATEGORY)
                    .where(CATEGORY.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(CATEGORY.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(CATEGORY.ID.eq(categoryId)))
                    .fetchOne();

            Record record3 = jooq
                    .select(DSL.countDistinct(REQUIREMENT.ID).as("numberOfRequirements"))
                    .from(CATEGORY)
                    .leftJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(REQUIREMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(REQUIREMENT.ID.equal(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID)))
                    .where(CATEGORY.ID.eq(categoryId))
                    .fetchOne();

            Record record4 = jooq
                    .select(DSL.countDistinct(COMMENT.ID).as("numberOfComments"))
                    .select(DSL.countDistinct(ATTACHMENT.ID).as("numberOfAttachments"))
                    .select(DSL.countDistinct(VOTE.ID).as("numberOfVotes"))
                    .from(CATEGORY)
                    .leftJoin(REQUIREMENT_CATEGORY_MAP).on(REQUIREMENT_CATEGORY_MAP.CATEGORY_ID.equal(CATEGORY.ID))
                    .leftJoin(REQUIREMENT).on(REQUIREMENT.ID.equal(REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID))
                    .leftJoin(COMMENT).on(COMMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(COMMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(COMMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(ATTACHMENT).on(ATTACHMENT.CREATION_DATE.greaterOrEqual(timestamp)
                            .or(ATTACHMENT.LAST_UPDATED_DATE.greaterOrEqual(timestamp))
                            .and(ATTACHMENT.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .leftJoin(VOTE).on(VOTE.CREATION_DATE.greaterOrEqual(timestamp)
                            .and(VOTE.REQUIREMENT_ID.equal(REQUIREMENT.ID)))
                    .where(CATEGORY.ID.eq(categoryId))
                    .fetchOne();

            result = Statistic.builder()
                    .numberOfProjects((Integer) record1.get("numberOfProjects"))
                    .numberOfCategories((Integer) record2.get("numberOfCategories"))
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
