/*
 *
 *  Copyright (c) 2015, RWTH Aachen University.
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

package de.rwth.dbis.acis.bazaar.service.scoringprovider.metrics;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProvider;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProviderBase;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements.REQUIREMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public class VoteMetric extends MetricProviderBase<RequirementsRecord> implements MetricProvider<RequirementsRecord> {
    private boolean isUpvote;

    public VoteMetric(boolean forUpVotes){
        isUpvote = forUpVotes;
    }

    @Override
    public void calculateMetric(List<RequirementsRecord> items, DSLContext db) {
        Collection<Integer> itemIds = new HashSet<Integer>();
        for (RequirementsRecord item : items) {
            itemIds.add(item.getId());
        }


        Result<Record> votes = db.select(REQUIREMENTS.ID)
                .select(DSL.count(DSL.nullif(VOTES.IS_UPVOTE, isUpvote ? 0 : 1)).as("votes"))
                .from(REQUIREMENTS)
                .leftOuterJoin(VOTES).on(VOTES.REQUIREMENT_ID.eq(REQUIREMENTS.ID))
                .where(REQUIREMENTS.ID.in(itemIds))
                .groupBy(REQUIREMENTS.ID)
                .fetch();

        this.measurements = new HashMap<RequirementsRecord, Double>();
        for (Record vote : votes) {
            Integer requirementId = vote.getValue(0, Integer.class);
            Double numberOfVotes = vote.getValue(1, Double.class);

            for (RequirementsRecord item : items) {
                if (item.getId().equals(requirementId))
                {
                    measurements.put(item, numberOfVotes);
                    break;
                }
            }
        }
    }

    @Override
    public double lowerBoundOfRange() {
        return 0;
    }

    @Override
    public double upperBoundOfRange() {
        return 1;
    }
}
