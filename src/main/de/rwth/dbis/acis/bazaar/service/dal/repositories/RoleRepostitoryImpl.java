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

import de.rwth.dbis.acis.bazaar.service.dal.entities.Privilege;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Role;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.*;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RolesRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CommentTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.PrivilegeEnumConverter;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RoleTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/17/2015
 */
public class RoleRepostitoryImpl extends RepositoryImpl<Role, RolesRecord> implements RoleRepostitory {
    public RoleRepostitoryImpl(DSLContext jooq) {
        super(jooq, new RoleTransformator());
    }

    @Override
    public List<Role> listRolesOfUser(int userId, String context) throws BazaarException {
        List<Role> roles = null;

        try {
            Roles rolesTable = Roles.ROLES.as("roles");
            Privileges privilegesTable = Privileges.PRIVILEGES.as("privileges");

            Result<Record> queryResult = jooq.selectFrom(
                    UserRole.USER_ROLE
                            .join(rolesTable).on(UserRole.USER_ROLE.ROLES_ID.eq(rolesTable.ID))
                            .leftOuterJoin(RolePrivilege.ROLE_PRIVILEGE).on(RolePrivilege.ROLE_PRIVILEGE.ROLES_ID.eq(Roles.ROLES.ID))
                            .leftOuterJoin(privilegesTable).on(privilegesTable.ID.eq(RolePrivilege.ROLE_PRIVILEGE.PRIVILEGES_ID))
            ).where(UserRole.USER_ROLE.USERS_ID.equal(userId).and(UserRole.USER_ROLE.CONTEXT_INFO.eq(context).or(UserRole.USER_ROLE.CONTEXT_INFO.isNull()))).fetch();

            if (queryResult!= null && !queryResult.isEmpty()) {
                roles = new ArrayList<Role>();
                convertToRoles(roles,rolesTable,privilegesTable,queryResult);
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return roles;
    }

    @Override
    public List<Role> listParentsForRole(int roleId) throws BazaarException {
        List<Role> roles = null;

        try {
            Roles rolesTable = Roles.ROLES.as("roles");
            Privileges privilegesTable = Privileges.PRIVILEGES.as("privileges");

            Result<Record> queryResult = jooq.selectFrom(
                    RoleRole.ROLE_ROLE
                            .join(rolesTable).on(RoleRole.ROLE_ROLE.PARENT_ID.equal(rolesTable.ID))
                            .leftOuterJoin(RolePrivilege.ROLE_PRIVILEGE).on(RolePrivilege.ROLE_PRIVILEGE.ROLES_ID.eq(rolesTable.ID))
                            .leftOuterJoin(privilegesTable).on(privilegesTable.ID.eq(RolePrivilege.ROLE_PRIVILEGE.PRIVILEGES_ID))
            ).where(RoleRole.ROLE_ROLE.CHILD_ID.equal(roleId)).fetch();

            if (queryResult!= null && !queryResult.isEmpty()) {
                roles = new ArrayList<Role>();
                convertToRoles(roles,rolesTable,privilegesTable,queryResult);
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return roles;
    }

    private void convertToRoles(List<Role> roles, Roles rolesTable, Privileges privilegesTable, Result<Record> queryResult) {
        for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(rolesTable.ID).entrySet()) {
            if (entry.getKey() == null) continue;
            Result<Record> records = entry.getValue();

            List<Privilege> rolesToAddPrivileges = new ArrayList<Privilege>();

            for (Map.Entry<Integer, Result<Record>> priviligeEntry : records.intoGroups(privilegesTable.ID).entrySet()) {
                if (priviligeEntry.getKey() == null) continue;
                Result<Record> privileges = priviligeEntry.getValue();

                Privilege privilege = Privilege.getBuilder(new PrivilegeEnumConverter().from(privileges.getValues(privilegesTable.NAME).get(0)))
                        .id(privileges.getValues(privilegesTable.ID).get(0))
                        .build();
                rolesToAddPrivileges.add(privilege);
            }


            Role roleToAdd = Role.getBuilder(records.getValues(rolesTable.NAME).get(0))
                    .id(records.getValues(rolesTable.ID).get(0))
                    .privileges(rolesToAddPrivileges)
                    .build();

            roles.add(roleToAdd);
        }
    }
}
