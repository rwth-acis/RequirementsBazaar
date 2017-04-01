/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RoleRoleMapRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


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
public class RoleRoleMap extends TableImpl<RoleRoleMapRecord> {

    private static final long serialVersionUID = 308433545;

    /**
     * The reference instance of <code>reqbaz.role_role_map</code>
     */
    public static final RoleRoleMap ROLE_ROLE_MAP = new RoleRoleMap();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RoleRoleMapRecord> getRecordType() {
        return RoleRoleMapRecord.class;
    }

    /**
     * The column <code>reqbaz.role_role_map.id</code>.
     */
    public final TableField<RoleRoleMapRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.role_role_map.child_id</code>.
     */
    public final TableField<RoleRoleMapRecord, Integer> CHILD_ID = createField("child_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.role_role_map.parent_id</code>.
     */
    public final TableField<RoleRoleMapRecord, Integer> PARENT_ID = createField("parent_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>reqbaz.role_role_map</code> table reference
     */
    public RoleRoleMap() {
        this("role_role_map", null);
    }

    /**
     * Create an aliased <code>reqbaz.role_role_map</code> table reference
     */
    public RoleRoleMap(String alias) {
        this(alias, ROLE_ROLE_MAP);
    }

    private RoleRoleMap(String alias, Table<RoleRoleMapRecord> aliased) {
        this(alias, aliased, null);
    }

    private RoleRoleMap(String alias, Table<RoleRoleMapRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Reqbaz.REQBAZ;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Identity<RoleRoleMapRecord, Integer> getIdentity() {
        return Keys.IDENTITY_ROLE_ROLE_MAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RoleRoleMapRecord> getPrimaryKey() {
        return Keys.KEY_ROLE_ROLE_MAP_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RoleRoleMapRecord>> getKeys() {
        return Arrays.<UniqueKey<RoleRoleMapRecord>>asList(Keys.KEY_ROLE_ROLE_MAP_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<RoleRoleMapRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<RoleRoleMapRecord, ?>>asList(Keys.ROLE_ROLE_MAP_CHILD, Keys.ROLE_ROLE_MAP_PARENT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RoleRoleMap as(String alias) {
        return new RoleRoleMap(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RoleRoleMap rename(String name) {
        return new RoleRoleMap(name, null);
    }
}