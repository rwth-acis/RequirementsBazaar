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
import org.jooq.DSLContext;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/8/2015
 */
public class CombinedComparators<T> implements Comparator<T> {
    Map<ScoringComparator<T>, Double> weightedComparators;
    protected double sumOfWeights = 0;

    public CombinedComparators(Map<ScoringComparator<T>, Double> weightedComparators) {
        this.weightedComparators = weightedComparators;
        sumOfWeights = 0;
        for (Double weight : weightedComparators.values()) {
            sumOfWeights += weight;
        }

    }

    @Override
    public int compare(T item1, T item2) {
        double item1WeightedScore = 0;
        double item2WeightedScore = 0;

        for (ScoringComparator<T> scoringComparator : weightedComparators.keySet()) {
            double weight = weightedComparators.get(scoringComparator) / sumOfWeights;
            item1WeightedScore += weight * scoringComparator.getScores().get(item1);
            item1WeightedScore += weight * scoringComparator.getScores().get(item2);
        }

        return Double.compare(item1WeightedScore, item2WeightedScore);
    }
}
