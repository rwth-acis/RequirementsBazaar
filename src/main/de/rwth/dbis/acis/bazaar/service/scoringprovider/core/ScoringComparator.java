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

import java.util.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public abstract class ScoringComparator<T> implements Comparator<T> {

    protected Map<T,Double> scores;
    protected Map<String,MetricProvider<T>> metrics;

    public ScoringComparator(List<T> items, DSLContext db) {
        metrics = registerMetrics();

        for (MetricProvider<T> metric : metrics.values()) {
            metric.calculateMetric(items, db);
        }

        scores = new HashMap<T, Double>();

        for (T item : items) {
            scores.put(item, calculateScore(item,metrics));
        }
    }


    /**
     * @return the metrics to be used during the scoring
     */
    protected abstract Map<String,MetricProvider<T>> registerMetrics();


    /**
     * Calculate the final score for a certain item by combining the metrics
     *
     * @param item
     * @param metrics
     * @return
     */
    protected abstract Double calculateScore(T item, Map<String,MetricProvider<T>> metrics);

    @Override
    public int compare(T item1, T item2) {
        return scores.get(item1).compareTo(scores.get(item2));
    }
}
