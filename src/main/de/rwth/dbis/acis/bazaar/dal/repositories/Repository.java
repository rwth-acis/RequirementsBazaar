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
        public E delete(int id) throws Exception;


        /**
         * @return all the entities currently in the database
         */
        public List<E> findAll();


        /**
         * @param id of the entity we are looking for
         * @return the entity from the database with the given Id
         * @throws Exception
         */
        public E findById(int id) throws Exception;


        /**
         * @param entity object, which holds the new values of the database update
         * @return the entity after the database
         * @throws Exception
         */
        public E update(E entity) throws Exception;
}
