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
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RoleRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRoleMapRecord;
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

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role.ROLE;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RoleRoleMap.ROLE_ROLE_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege.PRIVILEGE;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.UserRoleMap.USER_ROLE_MAP;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RolePrivilegeMap.ROLE_PRIVILEGE_MAP;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 2/17/2015
 */
public class RoleRepostitoryImpl extends RepositoryImpl<Role, RoleRecord> implements RoleRepostitory {
    public RoleRepostitoryImpl(DSLContext jooq) {
        super(jooq, new RoleTransformator());
    }

    @Override
    public List<Role> listRolesOfUser(int userId, String context) throws BazaarException {
        List<Role> roles = null;

        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role roleTable = ROLE.as("role");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege privilegeTable = PRIVILEGE.as("privilege");

            Result<Record> queryResult = jooq.selectFrom(
                    USER_ROLE_MAP
                            .join(roleTable).on(USER_ROLE_MAP.ROLE_ID.eq(roleTable.ID))
                            .leftOuterJoin(ROLE_PRIVILEGE_MAP).on(ROLE_PRIVILEGE_MAP.ROLE_ID.eq(ROLE.ID))
                            .leftOuterJoin(PRIVILEGE).on(PRIVILEGE.ID.eq(ROLE_PRIVILEGE_MAP.PRIVILEGE_ID))
            ).where(USER_ROLE_MAP.USER_ID.equal(userId).and(USER_ROLE_MAP.CONTEXT_INFO.eq(context).or(USER_ROLE_MAP.CONTEXT_INFO.isNull()))).fetch();

            if (queryResult != null && !queryResult.isEmpty()) {
                roles = new ArrayList<>();
                convertToRoles(roles, roleTable, privilegeTable, queryResult);
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return roles;
    }

    @Override
    public void addUserToRole(int userId, String roleName, String context) throws BazaarException {
        Role role = findByRoleName(roleName);
        UserRoleMapRecord record = new UserRoleMapRecord();
        record.setRoleId(role.getId());
        record.setUserId(userId);
        record.setContextInfo(context);
        UserRoleMapRecord inserted = jooq.insertInto(USER_ROLE_MAP)
                .set(record)
                .returning()
                .fetchOne();

    }

    @Override
    public Role findByRoleName(String roleName) throws BazaarException {
        Role role = null;

        try {
            RoleRecord rolesRecord = jooq.selectFrom(ROLE).where(ROLE.NAME.eq(roleName)).fetchOne();
            if (rolesRecord == null) {
                throw new Exception("No " + transformator.getRecordClass() + " found with name: " + roleName);
            }
            role = transformator.getEntityFromTableRecord(rolesRecord);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return role;
    }

    @Override
    public List<Role> listParentsForRole(int roleId) throws BazaarException {
        List<Role> roles = null;

        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role roleTable = ROLE.as("role");
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege privilegeTable = PRIVILEGE.as("privilege");

            Result<Record> queryResult = jooq.selectFrom(
                    ROLE_ROLE_MAP
                            .join(roleTable).on(ROLE_ROLE_MAP.PARENT_ID.equal(roleTable.ID))
                            .leftOuterJoin(ROLE_PRIVILEGE_MAP).on(ROLE_PRIVILEGE_MAP.ROLE_ID.eq(roleTable.ID))
                            .leftOuterJoin(privilegeTable).on(privilegeTable.ID.eq(ROLE_PRIVILEGE_MAP.PRIVILEGE_ID))
            ).where(ROLE_ROLE_MAP.CHILD_ID.equal(roleId)).fetch();

            if (queryResult != null && !queryResult.isEmpty()) {
                roles = new ArrayList<Role>();
                convertToRoles(roles, roleTable, privilegeTable, queryResult);
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return roles;
    }

    private void convertToRoles(List<Role> roles, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role roleTable,
                                de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege privilegeTable, Result<Record> queryResult) {
        for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(roleTable.ID).entrySet()) {
            if (entry.getKey() == null) continue;
            Result<Record> records = entry.getValue();

            List<Privilege> rolesToAddPrivileges = new ArrayList<Privilege>();

            for (Map.Entry<Integer, Result<Record>> priviligeEntry : records.intoGroups(privilegeTable.ID).entrySet()) {
                if (priviligeEntry.getKey() == null) continue;
                Result<Record> privileges = priviligeEntry.getValue();

                Privilege privilege = Privilege.getBuilder(new PrivilegeEnumConverter().from(privileges.getValues(privilegeTable.NAME).get(0)))
                        .id(privileges.getValues(privilegeTable.ID).get(0))
                        .build();
                rolesToAddPrivileges.add(privilege);
            }


            Role roleToAdd = Role.getBuilder(records.getValues(roleTable.NAME).get(0))
                    .id(records.getValues(roleTable.ID).get(0))
                    .privileges(rolesToAddPrivileges)
                    .build();

            roles.add(roleToAdd);
        }
    }
}
