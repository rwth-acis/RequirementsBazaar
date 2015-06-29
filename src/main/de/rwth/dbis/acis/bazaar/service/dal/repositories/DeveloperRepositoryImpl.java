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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Developer;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.DevelopersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.VotesRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.DeveloperTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.UpdateSetFirstStep;
import org.jooq.UpdateSetMoreStep;
import org.jooq.exception.DataAccessException;

import java.util.Map;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Developers.DEVELOPERS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class DeveloperRepositoryImpl extends RepositoryImpl<Developer, DevelopersRecord> implements DeveloperRepository {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public DeveloperRepositoryImpl(DSLContext jooq) {
        super(jooq, new DeveloperTransformator());
    }

    @Override
    public void delete(int userId, int requirementId) throws BazaarException {
        try {
            jooq.delete(DEVELOPERS)
                    .where(DEVELOPERS.USER_ID.equal(userId).and(DEVELOPERS.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserAlreadyDevelops(int userId, int requirementId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(DEVELOPERS)
                    .where(DEVELOPERS.USER_ID.equal(userId).and(DEVELOPERS.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public CreationStatus addOrUpdate(Developer developer) throws BazaarException {
        DevelopersRecord record = jooq.selectFrom(DEVELOPERS)
                .where(DEVELOPERS.USER_ID.equal(developer.getUserId()).and(DEVELOPERS.REQUIREMENT_ID.equal(developer.getRequirementId())))
                .fetchOne();
        if (record != null){
            return CreationStatus.UNCHANGED;
        }
        else {
            try {
                this.add(developer);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }
}
