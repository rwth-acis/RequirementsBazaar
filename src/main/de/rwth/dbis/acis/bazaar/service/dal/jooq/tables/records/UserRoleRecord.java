/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.UserRole;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.8.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserRoleRecord extends UpdatableRecordImpl<UserRoleRecord> implements Record4<Integer, Integer, Integer, String> {

    private static final long serialVersionUID = -1711605739;

    /**
     * Setter for <code>reqbaz.user_role.Id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>reqbaz.user_role.Id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>reqbaz.user_role.Roles_Id</code>.
     */
    public void setRolesId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>reqbaz.user_role.Roles_Id</code>.
     */
    public Integer getRolesId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>reqbaz.user_role.Users_Id</code>.
     */
    public void setUsersId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>reqbaz.user_role.Users_Id</code>.
     */
    public Integer getUsersId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>reqbaz.user_role.context_info</code>.
     */
    public void setContextInfo(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>reqbaz.user_role.context_info</code>.
     */
    public String getContextInfo() {
        return (String) get(3);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Integer, String> fieldsRow() {
        return (Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row4<Integer, Integer, Integer, String> valuesRow() {
        return (Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return UserRole.USER_ROLE.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return UserRole.USER_ROLE.ROLES_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return UserRole.USER_ROLE.USERS_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return UserRole.USER_ROLE.CONTEXT_INFO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getRolesId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getUsersId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getContextInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRecord value2(Integer value) {
        setRolesId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRecord value3(Integer value) {
        setUsersId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRecord value4(String value) {
        setContextInfo(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleRecord values(Integer value1, Integer value2, Integer value3, String value4) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserRoleRecord
     */
    public UserRoleRecord() {
        super(UserRole.USER_ROLE);
    }

    /**
     * Create a detached, initialised UserRoleRecord
     */
    public UserRoleRecord(Integer id, Integer rolesId, Integer usersId, String contextInfo) {
        super(UserRole.USER_ROLE);

        set(0, id);
        set(1, rolesId);
        set(2, usersId);
        set(3, contextInfo);
    }
}