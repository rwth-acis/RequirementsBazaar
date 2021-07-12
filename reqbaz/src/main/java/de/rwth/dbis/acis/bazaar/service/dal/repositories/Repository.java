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
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

import java.util.List;

/**
 * @param <E> Type of the Entity, which should be added, deleted, updated, got using the repo.
 * @since 6/9/2014
 */
public interface Repository<E extends EntityBase> {
    /**
     * @param entity to add
     * @return the persisted entity
     */
    E add(E entity) throws BazaarException;


    /**
     * @param id of an entity, which should be deleted
     * @return the deleted entity. It is not anymore in the database!
     * @throws Exception
     */
    E delete(int id) throws Exception;


    /**
     * @return all the entities currently in the database
     */
    List<E> findAll() throws BazaarException;


    /**
     * @param pageable
     * @return
     */
    List<E> findAll(Pageable pageable) throws BazaarException;

    /**
     * @param searchTerm
     * @param pageable
     * @return
     */
    List<E> searchAll(String searchTerm, Pageable pageable) throws Exception;

    /**
     * @param id of the entity we are looking for
     * @return the entity from the database with the given Id
     * @throws Exception
     */
    E findById(int id) throws Exception;


    /**
     * @param entity object, which holds the new values of the database update
     * @return the entity after the database
     * @throws Exception
     */
    E update(E entity) throws Exception;
}
