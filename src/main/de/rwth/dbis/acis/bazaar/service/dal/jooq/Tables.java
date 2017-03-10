/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachment;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comment;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Component;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollowerMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Project;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ProjectFollowerMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementComponentMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementDeveloperMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementFollowerMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RolePrivilegeMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RoleRoleMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.UserRoleMap;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote;

import javax.annotation.Generated;


/**
 * Convenience access to all tables in reqbaz
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Tables {

    /**
     * The table <code>reqbaz.attachment</code>.
     */
    public static final Attachment ATTACHMENT = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachment.ATTACHMENT;

    /**
     * The table <code>reqbaz.comment</code>.
     */
    public static final Comment COMMENT = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comment.COMMENT;

    /**
     * The table <code>reqbaz.component</code>.
     */
    public static final Component COMPONENT = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Component.COMPONENT;

    /**
     * The table <code>reqbaz.component_follower_map</code>.
     */
    public static final ComponentFollowerMap COMPONENT_FOLLOWER_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollowerMap.COMPONENT_FOLLOWER_MAP;

    /**
     * The table <code>reqbaz.privilege</code>.
     */
    public static final Privilege PRIVILEGE = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privilege.PRIVILEGE;

    /**
     * The table <code>reqbaz.project</code>.
     */
    public static final Project PROJECT = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Project.PROJECT;

    /**
     * The table <code>reqbaz.project_follower_map</code>.
     */
    public static final ProjectFollowerMap PROJECT_FOLLOWER_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ProjectFollowerMap.PROJECT_FOLLOWER_MAP;

    /**
     * The table <code>reqbaz.requirement</code>.
     */
    public static final Requirement REQUIREMENT = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirement.REQUIREMENT;

    /**
     * The table <code>reqbaz.requirement_component_map</code>.
     */
    public static final RequirementComponentMap REQUIREMENT_COMPONENT_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementComponentMap.REQUIREMENT_COMPONENT_MAP;

    /**
     * The table <code>reqbaz.requirement_developer_map</code>.
     */
    public static final RequirementDeveloperMap REQUIREMENT_DEVELOPER_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementDeveloperMap.REQUIREMENT_DEVELOPER_MAP;

    /**
     * The table <code>reqbaz.requirement_follower_map</code>.
     */
    public static final RequirementFollowerMap REQUIREMENT_FOLLOWER_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementFollowerMap.REQUIREMENT_FOLLOWER_MAP;

    /**
     * The table <code>reqbaz.role</code>.
     */
    public static final Role ROLE = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Role.ROLE;

    /**
     * The table <code>reqbaz.role_privilege_map</code>.
     */
    public static final RolePrivilegeMap ROLE_PRIVILEGE_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RolePrivilegeMap.ROLE_PRIVILEGE_MAP;

    /**
     * The table <code>reqbaz.role_role_map</code>.
     */
    public static final RoleRoleMap ROLE_ROLE_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RoleRoleMap.ROLE_ROLE_MAP;

    /**
     * The table <code>reqbaz.user</code>.
     */
    public static final User USER = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User.USER;

    /**
     * The table <code>reqbaz.user_role_map</code>.
     */
    public static final UserRoleMap USER_ROLE_MAP = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.UserRoleMap.USER_ROLE_MAP;

    /**
     * The table <code>reqbaz.vote</code>.
     */
    public static final Vote VOTE = de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Vote.VOTE;
}
