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

import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CommentsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProvider;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProviderBase;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments.COMMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements.REQUIREMENTS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/2/2015
 */
public abstract class SinceMetricBase extends MetricProviderBase<RequirementsRecord> implements MetricProvider<RequirementsRecord> {
    protected DateTime sinceTime;

    public SinceMetricBase(Duration duration) {
        sinceTime = DateTime.now().minus(duration);
    }

    @Override
    public void calculateMetric(List<RequirementsRecord> items, DSLContext db) {
        Collection<Integer> itemIds = new HashSet<Integer>();
        for (RequirementsRecord item : items) {
            itemIds.add(item.getId());
        }

        Timestamp since = new Timestamp(sinceTime.getMillis());

        SelectHavingStep<Record> query = db.select(REQUIREMENTS.ID)
                .select(DSL.count(getIDField()))
                .from(REQUIREMENTS)
                .leftOuterJoin(getTable()).on(getRequirementIdField().eq(REQUIREMENTS.ID))
                .where(REQUIREMENTS.ID.in(itemIds).and(getCreationTimeField().greaterOrEqual(since).or(getIDField().isNull())))
                .groupBy(REQUIREMENTS.ID);
        Result<Record> recentItems = query
                                          .fetch();

        this.measurements = new HashMap<RequirementsRecord, Double>();
        for (Record vote : recentItems) {
            Integer requirementId = vote.getValue(0, Integer.class);
            Double itemCount = vote.getValue(1, Double.class);

            for (RequirementsRecord item : items) {
                if (item.getId().equals(requirementId))
                {
                    measurements.put(item, itemCount);
                    break;
                }
            }
        }
    }

    public abstract Table<?> getTable();
    public abstract Field<Integer> getIDField();
    public abstract Field<Integer> getRequirementIdField();
    public abstract Field<Timestamp> getCreationTimeField();
}
