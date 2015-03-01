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
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.WeightedScoringComparator;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.metrics.VoteMetric;
import org.jooq.DSLContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public class VoteComparator<RequirementsRecord> extends WeightedScoringComparator<RequirementsRecord> {

    public VoteComparator(List<RequirementsRecord> items, DSLContext db) {
        super(items, db);
    }

    @Override
    protected Map<MetricProvider<RequirementsRecord> , Double> registerMetricsWithWeights() {
        HashMap<MetricProvider<RequirementsRecord> , Double> metrics = new HashMap<MetricProvider<RequirementsRecord> , Double>();

        metrics.put((MetricProvider<RequirementsRecord>) new VoteMetric(true), 0.5);
        metrics.put((MetricProvider<RequirementsRecord>) new VoteMetric(false), 0.5);

        return metrics;
    }

}
