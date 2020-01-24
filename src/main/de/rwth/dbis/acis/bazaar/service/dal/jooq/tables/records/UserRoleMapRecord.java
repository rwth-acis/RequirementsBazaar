/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.UserRoleMap;

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
        "jOOQ version:3.9.1"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class UserRoleMapRecord extends UpdatableRecordImpl<UserRoleMapRecord> implements Record4<Integer, Integer, Integer, String> {

    private static final long serialVersionUID = -247032073;

    /**
     * Setter for <code>reqbaz.user_role_map.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>reqbaz.user_role_map.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>reqbaz.user_role_map.role_id</code>.
     */
    public void setRoleId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>reqbaz.user_role_map.role_id</code>.
     */
    public Integer getRoleId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>reqbaz.user_role_map.user_id</code>.
     */
    public void setUserId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>reqbaz.user_role_map.user_id</code>.
     */
    public Integer getUserId() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>reqbaz.user_role_map.context_info</code>.
     */
    public void setContextInfo(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>reqbaz.user_role_map.context_info</code>.
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
        return UserRoleMap.USER_ROLE_MAP.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return UserRoleMap.USER_ROLE_MAP.ROLE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return UserRoleMap.USER_ROLE_MAP.USER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return UserRoleMap.USER_ROLE_MAP.CONTEXT_INFO;
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
        return getRoleId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getUserId();
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
    public UserRoleMapRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleMapRecord value2(Integer value) {
        setRoleId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleMapRecord value3(Integer value) {
        setUserId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleMapRecord value4(String value) {
        setContextInfo(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRoleMapRecord values(Integer value1, Integer value2, Integer value3, String value4) {
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
     * Create a detached UserRoleMapRecord
     */
    public UserRoleMapRecord() {
        super(UserRoleMap.USER_ROLE_MAP);
    }

    /**
     * Create a detached, initialised UserRoleMapRecord
     */
    public UserRoleMapRecord(Integer id, Integer roleId, Integer userId, String contextInfo) {
        super(UserRoleMap.USER_ROLE_MAP);

        set(0, id);
        set(1, roleId);
        set(2, userId);
        set(3, contextInfo);
    }
}
