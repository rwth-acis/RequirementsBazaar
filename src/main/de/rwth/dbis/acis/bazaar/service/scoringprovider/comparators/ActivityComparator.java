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

import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProvider;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.ScoringComparator;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.WeightedScoringComparator;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.metrics.*;
import org.apache.commons.lang3.tuple.Pair;
import org.joda.time.Duration;
import org.jooq.DSLContext;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/2/2015
 */
public class ActivityComparator<RequirementsRecord> extends WeightedScoringComparator<RequirementsRecord> {
    public ActivityComparator(List<RequirementsRecord> items, DSLContext db) {
        super(items, db);
    }

    @Override
    protected Map<String, Pair<MetricProvider<RequirementsRecord>, Double>> registerMetricsWithWeights() {
        Map<String, Pair<MetricProvider<RequirementsRecord>, Double>> metrics = new HashMap<String, Pair<MetricProvider<RequirementsRecord>, Double>>();
        Duration recentLimit = Duration.standardDays(7);

        metrics.put("recentAttachments" , Pair.of((MetricProvider<RequirementsRecord>) new RecentAttachmentsMetric(recentLimit), 0.333));
        metrics.put("recentComments"    , Pair.of((MetricProvider<RequirementsRecord>) new RecentCommentsMetric(recentLimit), 0.1));
        metrics.put("recentDevelopers"  , Pair.of((MetricProvider<RequirementsRecord>) new RecentDevelopersMetric(recentLimit), 0.333));
        metrics.put("recentFollowers"   , Pair.of((MetricProvider<RequirementsRecord>) new RecentFollowersMetric(recentLimit), 0.167));
        metrics.put("recentVotes"   , Pair.of((MetricProvider<RequirementsRecord>) new RecentVotesMetric(recentLimit), 0.067));
        return metrics;
    }
}
