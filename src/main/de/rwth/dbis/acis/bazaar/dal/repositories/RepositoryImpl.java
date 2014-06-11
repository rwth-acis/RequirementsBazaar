package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.*;
import de.rwth.dbis.acis.bazaar.dal.transform.Transformator;
import org.jooq.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public  class RepositoryImpl<E extends IdentifiedById,R extends Record> implements Repository<E>{

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
    //TODO Transaction
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
    public E delete(Integer id) throws Exception {
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

    /**
     * @param id of the entity we are looking for
     * @return the entity from the database with the given Id
     * @throws Exception
     */
    public E findById(Integer id) throws Exception {
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
