/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
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

package de.rwth.dbis.acis.bazaar.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;

import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Requirements.REQUIREMENTS;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class RequirementTransformator implements Transformator<de.rwth.dbis.acis.bazaar.dal.entities.Requirement,de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord> {
    @Override
    public RequirementsRecord createRecord(Requirement entry) {
        RequirementsRecord record = new RequirementsRecord();
        record.setDescription(entry.getDescription());
        record.setTitle(entry.getTitle());

        //TODO connections

        return record;
    }

    @Override
    public Requirement mapToEntity(RequirementsRecord record) {
        return Requirement.getBuilder(record.getTitle())
                .description(record.getDescription())
                .id(record.getId())
                .build();
    }

    @Override
    public Table<RequirementsRecord> getTable() {
        return REQUIREMENTS;
    }

    @Override
    public TableField<RequirementsRecord, Integer> getTableId() {
        return REQUIREMENTS.ID;
    }

    @Override
    public Class<? extends RequirementsRecord> getRecordClass() {
        return RequirementsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Requirement entry) {
        return new HashMap<Field, Object>(){{
            put(REQUIREMENTS.DESCRIPTION, entry.getDescription());
            put(REQUIREMENTS.TITLE, entry.getTitle());
        }};
    }
}
