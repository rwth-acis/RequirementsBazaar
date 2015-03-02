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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProvider;
import de.rwth.dbis.acis.bazaar.service.scoringprovider.core.MetricProviderBase;
import org.jooq.*;
import org.jooq.impl.DSL;

import org.joda.time.*;

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
public class CommentsSinceMetric extends SinceMetricBase {

    public CommentsSinceMetric(Duration duration) {
        super(duration);
    }

    @Override
    public Table<?> getTable() {
        return COMMENTS;
    }

    @Override
    public Field getIDField() {
        return COMMENTS.ID;
    }

    @Override
    public Field getRequirementIdField() {
        return COMMENTS.REQUIREMENT_ID;
    }

    @Override
    public Field getCreationTimeField() {
        return COMMENTS.CREATION_TIME;
    }

}
