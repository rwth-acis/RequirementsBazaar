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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementFollowerMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementFollowerTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_FOLLOWER_MAP;

/**
 * @since 6/23/2014
 */
public class RequirementFollowerRepositoryImpl extends RepositoryImpl<RequirementFollower, RequirementFollowerMapRecord> implements RequirementFollowerRepository {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public RequirementFollowerRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementFollowerTransformer());
    }

    @Override
    public void delete(int userId, int requirementId) throws BazaarException {
        try {
            jooq.delete(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(userId).and(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserAlreadyFollows(int userId, int requirementId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(REQUIREMENT_FOLLOWER_MAP)
                    .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(userId).and(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public CreationStatus addOrUpdate(RequirementFollower requirementFollower) throws BazaarException {
        RequirementFollowerMapRecord record = jooq.selectFrom(REQUIREMENT_FOLLOWER_MAP)
                .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.equal(requirementFollower.getUserId()).and(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID.equal(requirementFollower.getRequirementId())))
                .fetchOne();

        if (record != null) {
            return CreationStatus.UNCHANGED;
        } else {
            try {
                this.add(requirementFollower);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }
}
