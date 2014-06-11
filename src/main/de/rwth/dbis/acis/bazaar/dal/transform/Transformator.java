package de.rwth.dbis.acis.bazaar.dal.transform;

import org.jooq.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 * @param <E> type parameter for the entity
 * @param <R> type parameter for the record
 */
public interface Transformator<E, R extends Record> {
    /**
     * @param entity object, which holds the prototype values for the newly created record.
     * @return a record object, which has the same value of the entity
     */
    R createRecord(E entity);


    /**
     * @param record which holds the data from the database
     * @return an entity filled up with data from the record
     */
    E mapToEntity(R record);


    /**
     * @return the JOOQ table representation, which holds all the records
     */
    Table<R> getTable();


    /**
     * @return identifier field for the table
     */
    TableField<R,Integer> getTableId();

    /**
     * @return the class object of the record
     */
    Class<? extends R> getRecordClass();

    /**
     * @param entity object, which holds the prototype values for the database update
     * @return a map with the fields from the table and the values from the entity. These fields will be updated.
     */
    Map<Field,Object> getUpdateMap(final E entity);
}
