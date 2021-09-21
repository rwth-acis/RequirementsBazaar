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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.PersonalisationDataRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PersonalisationData;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.PERSONALISATION_DATA;

/**
 * @since 6/23/2014
 */
public class PersonalisationDataTransformer implements Transformer<PersonalisationData, PersonalisationDataRecord> {
    @Override
    public PersonalisationDataRecord createRecord(PersonalisationData entity) {
        PersonalisationDataRecord record = new PersonalisationDataRecord();
        record.setVersion(entity.getVersion());
        record.setUserId(entity.getUserId());
        record.setIdentifier(entity.getKey());
        record.setSetting(entity.getValue());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public PersonalisationData getEntityFromTableRecord(PersonalisationDataRecord record) {
        return PersonalisationData.builder()
                .id(record.getId())
                .userId(record.getUserId())
                .version(record.getVersion())
                .userId(record.getUserId())
                .value(record.getSetting())
                .build();
    }

    @Override
    public Table<PersonalisationDataRecord> getTable() {
        return PERSONALISATION_DATA;
    }

    @Override
    public TableField<PersonalisationDataRecord, Integer> getTableId() {
        return PERSONALISATION_DATA.ID;
    }

    @Override
    public Class<? extends PersonalisationDataRecord> getRecordClass() {
        return PersonalisationDataRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final PersonalisationData entity) {
        return new HashMap<Field, Object>() {{
            put(PERSONALISATION_DATA.SETTING, entity.getValue());
            //put(PERSONALISATION_DATA.LAST_UPDATED_DATE, time);
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts){
       /* if (sorts.isEmpty()) {
            return Collections.singletonList(VOTE.ID.asc());
        } */
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        throw new Exception("Search is not supported!");
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        //return new ArrayList<>();
        throw new Exception("Filtering is not supported!");
    }
}
