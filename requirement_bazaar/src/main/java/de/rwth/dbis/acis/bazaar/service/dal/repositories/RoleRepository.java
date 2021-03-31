/*
 *
 *  Copyright (c) 2015, RWTH Aachen University.
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

import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectMember;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Role;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

import java.util.List;

/**
 * @since 2/17/2015
 */
public interface RoleRepository extends Repository<Role> {
    List<Role> listParentsForRole(int roleId) throws BazaarException;

    List<Role> listRolesOfUser(int userId, Integer context) throws BazaarException;

    void addUserToRole(int userId, String roleName, Integer context) throws BazaarException;

    Role findByRoleName(String roleName) throws BazaarException;

    PaginationResult<ProjectMember> listProjectMembers(int projectId, Pageable pageable) throws BazaarException;
}
