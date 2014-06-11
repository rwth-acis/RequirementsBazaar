package de.rwth.dbis.acis.bazaar.dal.repositories;


import de.rwth.dbis.acis.bazaar.dal.entities.IdentifiedById;

import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 * @param <E>   Type of the Entity, which should be added, deleted, updated, getted using the repo.
 */
public interface Repository<E extends IdentifiedById> {
        /**
         * @param entity to add
         * @return the persisted entity
         */
        public E add(E entity);


        /**
         * @param id of an entity, which should be deleted
         * @return the deleted entity. It is not anymore in the database!
         * @throws Exception
         */
        public E delete(Integer id) throws Exception;


        /**
         * @return all the entities currently in the database
         */
        public List<E> findAll();


        /**
         * @param id of the entity we are looking for
         * @return the entity from the database with the given Id
         * @throws Exception
         */
        public E findById(Integer id) throws Exception;


        /**
         * @param entity object, which holds the new values of the database update
         * @return the entity after the database
         * @throws Exception
         */
        public E update(E entity) throws Exception;
}
