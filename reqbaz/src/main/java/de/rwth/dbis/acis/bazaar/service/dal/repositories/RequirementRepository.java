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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

import java.time.OffsetDateTime;
import java.util.List;

public interface RequirementRepository extends Repository<Requirement> {

    PaginationResult<Requirement> findAllByProject(int projectId, Pageable pageable, int userId) throws BazaarException;

    PaginationResult<Requirement> findAllByCategory(int categoryId, Pageable pageable, int userId) throws BazaarException;

    PaginationResult<Requirement> findAll(Pageable pageable, int userId) throws BazaarException;

    List<Integer> listAllRequirementIds(Pageable pageable, int userId) throws BazaarException;

    boolean belongsToPublicProject(int id) throws BazaarException;

    Requirement findById(int id, int userId) throws Exception;

    Requirement findById(int id, int userId, List<String> embed) throws Exception;

    void setRealized(int id, OffsetDateTime realized) throws BazaarException;

    void setLeadDeveloper(int id, Integer userId) throws BazaarException;

    Statistic getStatisticsForRequirement(int userId, int requirementId, OffsetDateTime timestamp) throws BazaarException;

    List<Requirement> getFollowedRequirements(int userId, int count) throws BazaarException;
}
