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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Statistic;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @since 6/9/2014
 */
public interface ProjectRepository extends Repository<Project> {

    Project findById(int id, int userId) throws BazaarException;

    PaginationResult<Project> findAllPublic(Pageable pageable, int userId) throws BazaarException;

    PaginationResult<Project> findAllPublicAndAuthorized(Pageable pageable, int userId) throws BazaarException;

    boolean belongsToPublicProject(int id) throws BazaarException;

    Statistic getStatisticsForVisibleProjects(int userId, OffsetDateTime timestamp) throws BazaarException;

    Statistic getStatisticsForProject(int userId, int projectId, OffsetDateTime timestamp) throws BazaarException;

    List<Integer> listAllProjectIds(Pageable pageable, int userId) throws BazaarException;

    List<Project> getFollowedProjects(int userId, int count) throws BazaarException;
}
