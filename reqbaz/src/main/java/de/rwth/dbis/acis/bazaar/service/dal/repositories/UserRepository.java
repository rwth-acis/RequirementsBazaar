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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import i5.las2peer.security.PassphraseAgentImpl;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @since 6/22/2014
 */
public interface UserRepository extends Repository<User> {

    Integer getIdByLas2PeerId(String las2PeerId) throws BazaarException;

    long hashAgentSub(PassphraseAgentImpl agent) throws BazaarException;

    void updateLas2peerId(int userId, String las2PeerId) throws BazaarException;

    void updateLastLoginDate(int userId) throws Exception;

    PaginationResult<User> findAllByDeveloping(int requirementId, Pageable pageable) throws BazaarException;

    RequirementContributors findRequirementContributors(int requirementId) throws BazaarException;

    CategoryContributors findCategoryContributors(int categoryId) throws BazaarException;

    ProjectContributors findProjectContributors(int projectId) throws BazaarException;

    PaginationResult<User> findAllByFollowing(int projectId, int categoryId, int requirementId, Pageable pageable) throws BazaarException;

    List<User> getEmailReceiverForProject(int projectId) throws BazaarException;

    List<User> getEmailReceiverForCategory(int categoryId) throws BazaarException;

    List<User> getEmailReceiverForRequirement(int requirementId) throws BazaarException;

    /**
     * Search with custom search and order logic
     *
     * @param pageable
     * @return
     * @throws BazaarException
     */
    List<User> search(Pageable pageable) throws BazaarException;

    /**
     * Returns user statistics in the given time interval.
     *
     * @param start interval start
     * @param end interval end
     *
     * @return statistics about the users during th given interval
     * @throws BazaarException
     */
    UserStatistics getUserStatistics(OffsetDateTime start, OffsetDateTime end) throws BazaarException;
}
