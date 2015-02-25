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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Vote;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.VotesRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.VoteTransformator;
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

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class VoteRepostitoryImpl extends RepositoryImpl<Vote, VotesRecord> implements VoteRepostitory {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public VoteRepostitoryImpl(DSLContext jooq) {
        super(jooq, new VoteTransformator());
    }

    @Override
    public void delete(int userId, int requirementId) throws BazaarException {
        try {
            jooq.delete(VOTES)
                    .where(VOTES.USER_ID.equal(userId).and(VOTES.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserVotedForRequirement(int userId, int requirementId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(VOTES)
                    .where(VOTES.USER_ID.equal(userId).and(VOTES.REQUIREMENT_ID.equal(requirementId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public void addOrUpdate(Vote vote) throws BazaarException {
        if (hasUserVotedForRequirement(vote.getUserId(),vote.getRequirementId())){
            UpdateSetFirstStep<VotesRecord> update = jooq.update(transformator.getTable());
            Map<Field, Object> map = transformator.getUpdateMap(vote);
            UpdateSetMoreStep moreStep = null;
            for (Map.Entry<Field, Object> item : map.entrySet()) {
                Field key = item.getKey();
                Object value = item.getValue();
                if(moreStep == null)
                    moreStep = update.set(key, value);
                else
                    moreStep.set(key,value);
            }
            assert moreStep != null;
            moreStep.where(VOTES.USER_ID.equal(vote.getUserId()).and(VOTES.REQUIREMENT_ID.equal(vote.getRequirementId())))
                    .execute();
        }
        else {
            this.add(vote);
        }
    }
}
