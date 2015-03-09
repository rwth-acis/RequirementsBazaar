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

package de.rwth.dbis.acis.bazaar.service.scoringprovider.comparators;

import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProvider;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.ScoringComparator;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.WeightedScoringComparator;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.metrics.VoteMetric;
import org.apache.commons.math3.stat.interval.WilsonScoreInterval;
import org.jooq.DSLContext;

import java.util.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public class VoteComparator<RequirementsRecord> extends ScoringComparator<RequirementsRecord> {

    public VoteComparator(List<RequirementsRecord> items, DSLContext db) {
        super(items, db);
    }

    @Override
    protected Map<String, MetricProvider<RequirementsRecord>> registerMetrics() {
        Map<String, MetricProvider<RequirementsRecord>> metrics = new HashMap<String, MetricProvider<RequirementsRecord>>();
        metrics.put("upvotes", (MetricProvider<RequirementsRecord>) new VoteMetric(true));
        metrics.put("downvotes", (MetricProvider<RequirementsRecord>) new VoteMetric(false));
        return metrics;
    }


    // Implemented by using Wilson score
    // http://www.evanmiller.org/how-not-to-sort-by-average-rating.html
    @Override
    protected Double calculateScore(RequirementsRecord item, Map<String, MetricProvider<RequirementsRecord>> metrics) {
        MetricProvider<RequirementsRecord> upvotes = metrics.get("upvotes");
        MetricProvider<RequirementsRecord> downvotes = metrics.get("downvotes");

        int voteSum =  (int) (upvotes.getMetric(item) + downvotes.getMetric(item));
        int positiveVotes = (int) upvotes.getMetric(item);
        double confidence = 0.95;
        WilsonScoreInterval wilsonScoreInterval = new WilsonScoreInterval();
        if (voteSum == 0) return 0.0;
        return wilsonScoreInterval.createInterval(voteSum, positiveVotes, confidence).getLowerBound();
    }
}
