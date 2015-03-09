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

import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Followers;
import org.joda.time.Duration;
import org.jooq.Field;
import org.jooq.Table;

import java.sql.Timestamp;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments.COMMENTS;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Followers.FOLLOWERS;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 3/4/2015
 */
public class RecentFollowersMetric  extends SinceMetricBase{
    public RecentFollowersMetric(Duration duration) {
        super(duration);
    }

    @Override
    public Table<?> getTable() {
        return FOLLOWERS;
    }

    @Override
    public Field<Integer> getIDField() {
        return FOLLOWERS.ID;
    }

    @Override
    public Field<Integer> getRequirementIdField() {
        return FOLLOWERS.REQUIREMENT_ID;
    }

    @Override
    public Field<Timestamp> getCreationTimeField() {
        return FOLLOWERS.CREATION_TIME;
    }
}
