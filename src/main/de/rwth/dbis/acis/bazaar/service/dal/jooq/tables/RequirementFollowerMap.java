/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementFollowerMapRecord;

import java.sql.Timestamp;
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
public class RequirementFollowerMap extends TableImpl<RequirementFollowerMapRecord> {

    private static final long serialVersionUID = -1236442855;

    /**
     * The reference instance of <code>reqbaz.requirement_follower_map</code>
     */
    public static final RequirementFollowerMap REQUIREMENT_FOLLOWER_MAP = new RequirementFollowerMap();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RequirementFollowerMapRecord> getRecordType() {
        return RequirementFollowerMapRecord.class;
    }

    /**
     * The column <code>reqbaz.requirement_follower_map.id</code>.
     */
    public final TableField<RequirementFollowerMapRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement_follower_map.requirement_id</code>.
     */
    public final TableField<RequirementFollowerMapRecord, Integer> REQUIREMENT_ID = createField("requirement_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement_follower_map.user_id</code>.
     */
    public final TableField<RequirementFollowerMapRecord, Integer> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement_follower_map.creation_date</code>.
     */
    public final TableField<RequirementFollowerMapRecord, Timestamp> CREATION_DATE = createField("creation_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.inline("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * Create a <code>reqbaz.requirement_follower_map</code> table reference
     */
    public RequirementFollowerMap() {
        this("requirement_follower_map", null);
    }

    /**
     * Create an aliased <code>reqbaz.requirement_follower_map</code> table reference
     */
    public RequirementFollowerMap(String alias) {
        this(alias, REQUIREMENT_FOLLOWER_MAP);
    }

    private RequirementFollowerMap(String alias, Table<RequirementFollowerMapRecord> aliased) {
        this(alias, aliased, null);
    }

    private RequirementFollowerMap(String alias, Table<RequirementFollowerMapRecord> aliased, Field<?>[] parameters) {
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
    public Identity<RequirementFollowerMapRecord, Integer> getIdentity() {
        return Keys.IDENTITY_REQUIREMENT_FOLLOWER_MAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RequirementFollowerMapRecord> getPrimaryKey() {
        return Keys.KEY_REQUIREMENT_FOLLOWER_MAP_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RequirementFollowerMapRecord>> getKeys() {
        return Arrays.<UniqueKey<RequirementFollowerMapRecord>>asList(Keys.KEY_REQUIREMENT_FOLLOWER_MAP_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<RequirementFollowerMapRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<RequirementFollowerMapRecord, ?>>asList(Keys.REQUIREMENT_FOLLOWER_MAP_REQUIREMENT, Keys.REQUIREMENT_FOLLOWER_MAP_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequirementFollowerMap as(String alias) {
        return new RequirementFollowerMap(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RequirementFollowerMap rename(String name) {
        return new RequirementFollowerMap(name, null);
    }
}