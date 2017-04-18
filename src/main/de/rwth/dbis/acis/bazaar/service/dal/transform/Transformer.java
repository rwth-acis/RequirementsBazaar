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

import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @param <E> type parameter for the entity
 * @param <R> type parameter for the record
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public interface Transformer<E, R extends Record> {
    /**
     * @param entity object, which holds the prototype values for the newly created record.
     * @return a record object, which has the same value of the entity
     */
    R createRecord(E entity);

    /**
     * @param record which holds the data from the database
     * @return an entity filled up with data from the record
     */
    E getEntityFromTableRecord(R record);

    /**
     * @return the JOOQ table representation, which holds all the records
     */
    Table<R> getTable();

    /**
     * @return identifier field for the table
     */
    TableField<R, Integer> getTableId();

    /**
     * @return the class object of the record
     */
    Class<? extends R> getRecordClass();

    /**
     * @param entity object, which holds the prototype values for the database update
     * @return a map with the fields from the table and the values from the entity. These fields will be updated.
     */
    Map<Field, Object> getUpdateMap(final E entity);

    /**
     * @param sorts
     * @return a collection of the fields, should be used for sorting. SortField also contains information about the sort direction.
     */
    Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts);

    /**
     * @param search
     * @return condition to search.
     */
    Condition getSearchCondition(String search) throws Exception;

    /**
     * @param filters
     * @return a collection of conditions to filter.
     */
    Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception;

}
