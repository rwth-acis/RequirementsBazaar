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

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RoleRecord;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.UserRoleMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.transform.PrivilegeEnumConverter;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RoleTransformer;
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

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

/**
 * @since 2/17/2015
 */
public class RoleRepositoryImpl extends RepositoryImpl<Role, RoleRecord> implements RoleRepository {
    public RoleRepositoryImpl(DSLContext jooq) {
        super(jooq, new RoleTransformer());
    }

    @Override
    public List<Role> listRolesOfUser(int userId, Integer context) throws BazaarException {
        List<Role> roles = null;

        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Role roleTable = ROLE.as("role");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Privilege privilegeTable = PRIVILEGE.as("privilege");

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
    public void addUserToRole(int userId, String roleName, Integer context) throws BazaarException {
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
                throw new Exception("No " + transformer.getRecordClass() + " found with name: " + roleName);
            }
            role = transformer.getEntityFromTableRecord(rolesRecord);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return role;
    }

    @Override
    public PaginationResult<ProjectMember> listProjectMembers(int projectId, Pageable pageable) throws BazaarException {
        PaginationResult<ProjectMember> result = null;
        List<ProjectMember> projectMembers = null;
        int total = 0;

        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Role roleTable = ROLE.as("role");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.User userTable = USER.as("user");

            Result<Record> queryResult = jooq.selectFrom(
                    USER_ROLE_MAP
                            .join(roleTable).on(USER_ROLE_MAP.ROLE_ID.eq(roleTable.ID))
                            .leftOuterJoin(USER).on(USER.ID.eq(USER_ROLE_MAP.USER_ID))
            ).where(USER_ROLE_MAP.CONTEXT_INFO.equal(projectId)).fetch();

            if (queryResult != null && !queryResult.isEmpty()) {
                total = queryResult.size();
                projectMembers = new ArrayList<>();
                for (Record entry : queryResult) {
                    User user = User.builder()
                            .eMail(entry.getValue(userTable.EMAIL))
                            .id(entry.getValue(userTable.ID))
                            .firstName(entry.getValue(userTable.FIRST_NAME))
                            .lastName(entry.getValue(userTable.LAST_NAME))
                            .las2peerId(entry.getValue(userTable.LAS2PEER_ID))
                            .userName(entry.getValue(userTable.USER_NAME))
                            .profileImage(entry.getValue(userTable.PROFILE_IMAGE))
                            .emailLeadSubscription(entry.getValue(userTable.EMAIL_LEAD_SUBSCRIPTION) != 0)
                            .emailFollowSubscription(entry.getValue(userTable.EMAIL_FOLLOW_SUBSCRIPTION) != 0)
                            .personalizationEnabled(entry.getValue(userTable.PERSONALIZATION_ENABLED) != 0)
                            .build();
                    ProjectMember member = ProjectMember.builder()
                            .id(entry.getValue(USER_ROLE_MAP.ID))
                            .user(user)
                            .userId(user.getId())
                            .role(ProjectRole.valueOf(entry.getValue(roleTable.NAME)))
                            .build();
                    projectMembers.add(member);
                }

            }
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return new PaginationResult<>(total, pageable, projectMembers);
    }

    @Override
    public List<Role> listParentsForRole(int roleId) throws BazaarException {
        List<Role> roles = null;

        try {
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Role roleTable = ROLE.as("role");
            de.rwth.dbis.acis.bazaar.dal.jooq.tables.Privilege privilegeTable = PRIVILEGE.as("privilege");

            Result<Record> queryResult = jooq.selectFrom(
                    ROLE_ROLE_MAP
                            .join(roleTable).on(ROLE_ROLE_MAP.PARENT_ID.equal(roleTable.ID))
                            .leftOuterJoin(ROLE_PRIVILEGE_MAP).on(ROLE_PRIVILEGE_MAP.ROLE_ID.eq(roleTable.ID))
                            .leftOuterJoin(privilegeTable).on(privilegeTable.ID.eq(ROLE_PRIVILEGE_MAP.PRIVILEGE_ID))
            ).where(ROLE_ROLE_MAP.CHILD_ID.equal(roleId)).fetch();

            if (queryResult != null && !queryResult.isEmpty()) {
                roles = new ArrayList<>();
                convertToRoles(roles, roleTable, privilegeTable, queryResult);
            }

        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return roles;
    }

    private void convertToRoles(List<Role> roles, de.rwth.dbis.acis.bazaar.dal.jooq.tables.Role roleTable,
                                de.rwth.dbis.acis.bazaar.dal.jooq.tables.Privilege privilegeTable, Result<Record> queryResult) {
        for (Map.Entry<Integer, Result<Record>> entry : queryResult.intoGroups(roleTable.ID).entrySet()) {
            if (entry.getKey() == null) continue;
            Result<Record> records = entry.getValue();

            List<Privilege> rolesToAddPrivileges = new ArrayList<>();

            for (Map.Entry<Integer, Result<Record>> privilegeEntry : records.intoGroups(privilegeTable.ID).entrySet()) {
                if (privilegeEntry.getKey() == null) continue;
                Result<Record> privileges = privilegeEntry.getValue();

                Privilege privilege = Privilege.builder().name(new PrivilegeEnumConverter().from(privileges.getValues(privilegeTable.NAME).get(0)))
                        .id(privileges.getValues(privilegeTable.ID).get(0))
                        .build();
                rolesToAddPrivileges.add(privilege);
            }

            Role roleToAdd = Role.builder()
                    .name(records.getValues(roleTable.NAME).get(0))
                    .id(records.getValues(roleTable.ID).get(0))
                    .privileges(rolesToAddPrivileges)
                    .build();

            roles.add(roleToAdd);
        }
    }
}
