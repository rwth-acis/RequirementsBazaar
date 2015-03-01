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

package de.rwth.dbis.acis.bazaar.service.scoringprovider.core;

import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public abstract class WeightedScoringComparator<T> extends ScoringComparator<T>{

    protected Map<MetricProvider<T>, Double> weightedMetrics;
    protected double sumOfWeights = 0;

    public WeightedScoringComparator(List<T> items, DSLContext db) {
        super(items, db);
    }

    protected abstract Map<MetricProvider<T>,Double> registerMetricsWithWeights();

    @Override
    protected final Set<MetricProvider<T>> registerMetrics() {
        weightedMetrics = registerMetricsWithWeights();

        for (Double weight : weightedMetrics.values()) {
            sumOfWeights += weight;
        }
        return weightedMetrics.keySet();
    }

    @Override
    protected Double calculateScore(T item, Set<MetricProvider<T>> metrics) {
        double weightedScoreSum = 0;

        for (MetricProvider<T> metric : metrics) {
            weightedScoreSum += (weightedMetrics.get(metric)/sumOfWeights) * metric.getNormalizedMetric(item);
        }

        return weightedScoreSum;
    }
}
