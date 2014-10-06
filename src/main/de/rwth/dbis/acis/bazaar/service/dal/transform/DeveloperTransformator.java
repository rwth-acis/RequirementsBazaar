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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Developer;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.DevelopersRecord;
import org.jooq.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Developers.DEVELOPERS;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/23/2014
 */
public class DeveloperTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Developer, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.DevelopersRecord> {
    @Override
    public DevelopersRecord createRecord(Developer entity) {
        DevelopersRecord record = new DevelopersRecord();
        record.setId(entity.getId());
        record.setUserId(entity.getUserId());
        record.setRequirementId(entity.getRequirementId());
        return record;
    }

    @Override
    public Developer mapToEntity(DevelopersRecord record) {
        return Developer.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<DevelopersRecord> getTable() {
        return DEVELOPERS;
    }

    @Override
    public TableField<DevelopersRecord, Integer> getTableId() {
        return DEVELOPERS.ID;
    }

    @Override
    public Class<? extends DevelopersRecord> getRecordClass() {
        return DevelopersRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Developer entity) {
        return new HashMap<Field, Object>() {{
            put(DEVELOPERS.REQUIREMENT_ID, entity.getRequirementId());
            put(DEVELOPERS.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(DEVELOPERS.ID.asc());
            case ASC:
                return Arrays.asList(DEVELOPERS.ID.asc());
            case DESC:
                return Arrays.asList(DEVELOPERS.ID.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        throw new Exception("Search is not supported!");
    }
}
