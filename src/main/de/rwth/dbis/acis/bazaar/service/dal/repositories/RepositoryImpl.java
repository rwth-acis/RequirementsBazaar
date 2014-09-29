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

package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.transform.Transformator;
import org.jooq.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public  class RepositoryImpl<E extends EntityBase,R extends Record> implements Repository<E>{

    protected final DSLContext jooq;
    protected final Transformator<E,R> transformator;


    /**
     * @param jooq DSLContext for JOOQ connection
     * @param transformator Transformator object to create mapping between JOOQ record and our entities
     */
    public RepositoryImpl(DSLContext jooq, Transformator<E, R> transformator) {
        this.jooq = jooq;
        this.transformator = transformator;
    }

    /**
     * @param entity to add
     * @return the persisted entity
     */
    public E add(E entity) {
        R persisted;
        persisted = jooq.insertInto(transformator.getTable())
                .set(transformator.createRecord(entity))
                .returning()
                .fetchOne();

        return transformator.mapToEntity(persisted);
    }


    /**
     * @param id of an entity, which should be deleted
     * @return the deleted entity. It is not anymore in the database!
     * @throws Exception
     */
    //TODO transaction (findById,delete)
    public E delete(int id) throws Exception {
        E deleted = this.findById(id);

        int deletedRecordCount = jooq.delete(transformator.getTable())
                .where(transformator.getTableId().equal(id))
                .execute();

        return deleted;
    }

    /**
     * @return all the entities currently in the database
     */
    public List<E> findAll() {
        List<E> entries = new ArrayList<E>();

        List<R> queryResults = jooq.selectFrom(transformator.getTable()).fetchInto(transformator.getRecordClass());

        for (R queryResult: queryResults) {
            E entry = transformator.mapToEntity(queryResult);
            entries.add(entry);
        }

        return entries;
    }

    @Override
    public List<E> findAll(Pageable pageable) {
        List<E> entries = new ArrayList<E>();

        List<R> queryResults = jooq.selectFrom(transformator.getTable())
                .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(transformator.getRecordClass());

        for (R queryResult: queryResults) {
            E entry = transformator.mapToEntity(queryResult);
            entries.add(entry);
        }

        return entries;
    }

    @Override
    public List<E> searchAll(String searchTerm, Pageable pageable) throws Exception {
        List<E> entries = new ArrayList<E>();
        String likeExpression = "%" + searchTerm + "%";

        List<R> queryResults = jooq.selectFrom(transformator.getTable())
                .where(transformator.getSearchFields(likeExpression))
                .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(transformator.getRecordClass());
        
        for (R queryResult: queryResults) {
            E entry = transformator.mapToEntity(queryResult);
            entries.add(entry);
        }

        return entries;
    }

    /**
     * @param id of the entity we are looking for
     * @return the entity from the database with the given Id
     * @throws Exception
     */
    public E findById(int id) throws Exception {
        R queryResult = jooq.selectFrom(transformator.getTable())
                .where(transformator.getTableId().equal(id))
                .fetchOne();

        if (queryResult == null) {
            throw new Exception("No "+ transformator.getRecordClass() +" found with id: " + id);
        }

        return transformator.mapToEntity(queryResult);
    }

    /**
     * @param entity object, which holds the new values of the database update
     * @return the entity after the database
     * @throws Exception
     */
    //TODO transaction(update,findById)
    @Override
    public E update(E entity) throws Exception {
        UpdateSetFirstStep<R> update = jooq.update(transformator.getTable());
        Map<Field,Object> map = transformator.getUpdateMap(entity);
        UpdateSetMoreStep moreStep = null;
        for (Map.Entry<Field, Object> item : map.entrySet()) {
            Field key = item.getKey();
            Object value = item.getValue();
            moreStep = (moreStep==null)?
                    update.set(key, value):
                    moreStep.set(key,value);
        }
        assert moreStep != null;
        moreStep.where(transformator.getTableId().equal(entity.getId())).execute();
        return findById(entity.getId());
    }
}
