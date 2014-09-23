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

package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import org.jooq.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements.REQUIREMENTS;


import java.util.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class RequirementTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord> {
    @Override
    public RequirementsRecord createRecord(Requirement entry) {
        RequirementsRecord record = new RequirementsRecord();
        record.setId(entry.getId());
        record.setDescription(entry.getDescription());
        record.setTitle(entry.getTitle());
        record.setCreationTime(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));

        record.setLeadDeveloperId(entry.getLeadDeveloperId());
        record.setCreatorId(entry.getCreatorId());
        record.setProjectId(entry.getProjectId());

        return record;
    }

    @Override
    public Requirement mapToEntity(RequirementsRecord record) {
        return Requirement.getBuilder(record.getTitle())
                .description(record.getDescription())
                .id(record.getId())
                .creationTime(record.getCreationTime())
                .leadDeveloperId(record.getLeadDeveloperId())
                .projectId(record.getProjectId())
                .creatorId(record.getCreatorId())
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

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(REQUIREMENTS.CREATION_TIME.desc());
            case ASC:
                return Arrays.asList(REQUIREMENTS.CREATION_TIME.asc());
            case DESC:
                return Arrays.asList(REQUIREMENTS.CREATION_TIME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                    REQUIREMENTS.TITLE.likeIgnoreCase(likeExpression)
                .or(REQUIREMENTS.DESCRIPTION.likeIgnoreCase(likeExpression))
        );
    }
}