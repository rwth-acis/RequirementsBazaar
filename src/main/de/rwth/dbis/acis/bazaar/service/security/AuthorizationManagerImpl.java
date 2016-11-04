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

package de.rwth.dbis.acis.bazaar.service.security;

import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Role;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Ownable;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

import java.util.EnumSet;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.service.dal.entities.PrivilegeEnum.*;


public class AuthorizationManagerImpl implements AuthorizationManager {

    public boolean isAuthorized(int userId, PrivilegeEnum privilege, DALFacade facade) throws BazaarException {
        return isAuthorized(userId, privilege, null, null, facade);
    }

    public boolean isAuthorized(int userId, PrivilegeEnum privilege, Ownable element, DALFacade facade) throws BazaarException {
        return isAuthorized(userId, privilege, null, element, facade);
    }

    public boolean isAuthorized(int userId, PrivilegeEnum privilege, String context, DALFacade facade) throws BazaarException {
        return isAuthorized(userId, privilege, context, null, facade);
    }


    public boolean isAuthorized(int userId, PrivilegeEnum privilege, String context, Ownable element, DALFacade facade) throws BazaarException {
        List<Role> userRoles = facade.getRolesByUserId(userId, context);

        // check if owner
        if (privilege == Modify_PROJECT ||
                privilege == Modify_COMPONENT ||
                privilege == Modify_REQUIREMENT ||
                privilege == Modify_COMMENT ||
                privilege == Modify_ATTACHMENT) {
            if (!isOwnerOrLeader(userId, element)) {
                return false;
            }
        }

        // check privilege
        return checkPrivilege(userRoles, privilege, facade);
    }

    public void syncPrivileges(DALFacade facade) throws BazaarException {
        EnumSet<PrivilegeEnum> privileges = EnumSet.allOf(PrivilegeEnum.class);
        for (PrivilegeEnum privilege : privileges) {
            facade.createPrivilegeIfNotExists(privilege);
        }
    }

    @Override
    public boolean isOwnerOrLeader(int userId, Ownable element) {
        return element.isOwner(userId);
    }

    private boolean checkPrivilege(List<Role> userRoles, PrivilegeEnum privilege, DALFacade facade) throws BazaarException {
        if (userRoles == null || userRoles.isEmpty()) return false;
        for (Role role : userRoles) {
            if (hasPrivilege(role, privilege)) {
                return true;
            } else {
                List<Role> parents = facade.getParentsForRole(role.getId());
                if (parents != null && !parents.isEmpty()) {
                    if (checkPrivilege(parents, privilege, facade))
                        return true;
                }
            }
        }
        return false;
    }

    private boolean hasPrivilege(Role role, PrivilegeEnum demandedPrivilege) {
        List<Privilege> privileges = role.getPrivileges();
        if (privileges != null && !privileges.isEmpty()) {
            for (Privilege privilege : privileges) {
                if (privilege.getName() == demandedPrivilege)
                    return true;
            }
        }
        return false;
    }

}
