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

import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/1/2015
 */
public abstract class MetricProviderBase<T> implements MetricProvider<T> {
    protected Map<T,Double> measurements;

    @Override
    public double getMetric(T item) {
        return measurements.get(item);
    }

    @Override
    public double getNormalizedMetric(T item) {
        double minValue = Double.MAX_VALUE;
        double maxValue = Double.MIN_VALUE;

        for (Double measurement : measurements.values()) {
            minValue = Math.min(minValue, measurement);
            maxValue = Math.max(maxValue, measurement);
        }

        //Feature scaling
        return lowerBoundOfRange() + ((getMetric(item) - minValue) * (upperBoundOfRange() - lowerBoundOfRange())) / (maxValue - minValue);
    }

    @Override
    public double lowerBoundOfRange() {
        return 0;
    }

    @Override
    public double upperBoundOfRange() {
        return 0;
    }
}
