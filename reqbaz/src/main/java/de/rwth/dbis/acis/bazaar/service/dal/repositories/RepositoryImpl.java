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

import de.rwth.dbis.acis.bazaar.service.dal.entities.EntityBase;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.transform.Transformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 6/9/2014
 */
public class RepositoryImpl<E extends EntityBase, R extends Record> implements Repository<E> {

    protected final DSLContext jooq;
    protected final Transformer<E, R> transformer;


    /**
     * @param jooq        DSLContext for JOOQ connection
     * @param transformer Transformer object to create mapping between JOOQ record and our entities
     */
    public RepositoryImpl(DSLContext jooq, Transformer<E, R> transformer) {
        this.jooq = jooq;
        this.transformer = transformer;
    }

    /**
     * @param entity to add
     * @return the persisted entity
     */
    @Override
    public E add(E entity) throws BazaarException {
        E transformedEntity = null;
        try {
            R persisted;
            persisted = jooq.insertInto(transformer.getTable())
                    .set(transformer.createRecord(entity))
                    .returning()
                    .fetchOne();

            transformedEntity = transformer.getEntityFromTableRecord(persisted);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN, e.getMessage());
        }
        return transformedEntity;
    }


    /**
     * @param id of an entity, which should be deleted
     * @return the deleted entity. It is not anymore in the database!
     * @throws Exception
     */
    //TODO transaction (findById,delete)
    @Override
    public E delete(int id) throws Exception {
        E deleted = null;
        try {
            deleted = findById(id);

            int deletedRecordCount = jooq.delete(transformer.getTable())
                    .where(transformer.getTableId().equal(id))
                    .execute();
        } catch (BazaarException ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return deleted;
    }

    /**
     * @return all the entities currently in the database
     */
    @Override
    public List<E> findAll() throws BazaarException {
        List<E> entries = null;
        try {
            entries = new ArrayList<>();

            List<R> queryResults = jooq.selectFrom(transformer.getTable()).fetchInto(transformer.getRecordClass());


        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN, e.getMessage());
        }

        return entries;
    }

    @Override
    public List<E> findAll(Pageable pageable) throws BazaarException {
        List<E> entries = null;
        try {
            entries = new ArrayList<>();

            List<R> queryResults = jooq.selectFrom(transformer.getTable())
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(transformer.getRecordClass());

            for (R queryResult : queryResults) {
                E entry = transformer.getEntityFromTableRecord(queryResult);
                entries.add(entry);
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN, e.getMessage());
        }

        return entries;
    }

    @Override
    public List<E> searchAll(String searchTerm, Pageable pageable) throws Exception {
        List<E> entries = null;
        try {
            entries = new ArrayList<>();
            String likeExpression = "%" + searchTerm + "%";

            List<R> queryResults = jooq.selectFrom(transformer.getTable())
                    .where(transformer.getSearchCondition(likeExpression))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetchInto(transformer.getRecordClass());

            for (R queryResult : queryResults) {
                E entry = transformer.getEntityFromTableRecord(queryResult);
                entries.add(entry);
            }
        } catch (BazaarException ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return entries;
    }

    /**
     * @param id of the entity we are looking for
     * @return the entity from the database with the given Id
     * @throws Exception
     */
    @Override
    public E findById(int id) throws Exception {
        R queryResult = null;
        try {
            queryResult = jooq.selectFrom(transformer.getTable())
                    .where(transformer.getTableId().equal(id))
                    .fetchOne();

            if (queryResult == null) {
                throw new Exception("No " + transformer.getRecordClass() + " found with id: " + id);
            }
        } catch (BazaarException ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }

        return transformer.getEntityFromTableRecord(queryResult);
    }

    /**
     * @param entity object, which holds the new values of the database update
     * @return the entity after the database
     * @throws Exception
     */
    //TODO transaction(update,findById)
    @Override
    public E update(E entity) throws Exception {
        E byId = null;
        try {
            UpdateSetFirstStep<R> update = jooq.update(transformer.getTable());
            Map<Field, Object> map = transformer.getUpdateMap(entity);
            UpdateSetMoreStep moreStep = null;
            for (Map.Entry<Field, Object> item : map.entrySet()) {
                Field key = item.getKey();
                Object value = item.getValue();
                if (moreStep == null) {
                    moreStep = update.set(key, value);
                } else {
                    moreStep.set(key, value);
                }
            }
            assert moreStep != null;
            moreStep.where(transformer.getTableId().equal(entity.getId())).execute();
            byId = findById(entity.getId());
        } catch (BazaarException ex) {
            ExceptionHandler.getInstance().convertAndThrowException(ex);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return byId;
    }
}
