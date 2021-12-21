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
import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectRole;
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

    /**
     * Returns the roles of a user in the given context including 'globally' assigned roles (i.e., roles
     * stored with context 'null').<br>
     * <br>
     * NOTE: This is th higher level function which should be user for authorization / access control.
     *
     * @param userId
     * @param context
     * @return
     * @throws BazaarException
     */
    List<Role> listRolesOfUser(int userId, Integer context) throws BazaarException;

    /**
     * Returns <i>exactly</i> the roles that are assigned to a user in th given context.<br>
     * In contrast to 'listRolesOfUser' this is only a straightforward database query which does
     * not apply any knowledge about higher level roles (i.e. SystemAdmin which may override
     * locally assigned roles like 'ProjectMember')
     *
     * @param userId
     * @param context
     * @return
     */
    List<Integer> findRoleIdsByContext(int userId, Integer context);

    boolean hasUserRole(int userId, String roleName, Integer context) throws BazaarException;
    boolean hasUserRole(int userId, int roleId, Integer context) throws BazaarException;

    /**
     * Returns whether a user has any role in a certain context.
     *
     * @param userId
     * @param context (required)
     * @return
     */
    boolean hasUserAnyRoleInContext(int userId, int context);

    void addUserToRole(int userId, String roleName, Integer context) throws BazaarException;
    void addUserToRole(int userId, int roleId, Integer context) throws BazaarException;

    void removeUserFromRole(int userId, String roleName, Integer context) throws BazaarException;
    void removeUserFromRole(int userId, int roleId, Integer context) throws BazaarException;

    /**
     * Remove the user from all roles assigned in a certain context.
     *
     * @param userId
     * @param context (required for this methdod!)
     */
    void removeUserFromRolesByContext(int userId, int context) throws BazaarException;

    void replaceUserRole(int userId, String oldRoleName, String newRoleName, Integer context) throws BazaarException;
    void replaceUserRole(int userId, int oldRoleId, int newRoleId, Integer context) throws BazaarException;

    Role findByRoleName(String roleName) throws BazaarException;

    //
    // project member specific methods -> TODO move to some higher level service!
    //

    PaginationResult<ProjectMember> listProjectMembers(int projectId, Pageable pageable) throws BazaarException;

    /**
     * Return the project role or null
     *
     * @param userId    id of the user
     * @param projectId id of the project
     * @return projectrole or null
     * @throws BazaarException
     */
    ProjectRole getProjectRole(int userId, int projectId) throws BazaarException;
}
