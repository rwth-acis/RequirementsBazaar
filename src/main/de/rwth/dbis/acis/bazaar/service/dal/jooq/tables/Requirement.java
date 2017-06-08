/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementRecord;

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
public class Requirement extends TableImpl<RequirementRecord> {

    private static final long serialVersionUID = 507661299;

    /**
     * The reference instance of <code>reqbaz.requirement</code>
     */
    public static final Requirement REQUIREMENT = new Requirement();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RequirementRecord> getRecordType() {
        return RequirementRecord.class;
    }

    /**
     * The column <code>reqbaz.requirement.id</code>.
     */
    public final TableField<RequirementRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement.name</code>.
     */
    public final TableField<RequirementRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(255).nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement.description</code>.
     */
    public final TableField<RequirementRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.CLOB, this, "");

    /**
     * The column <code>reqbaz.requirement.realized</code>.
     */
    public final TableField<RequirementRecord, Timestamp> REALIZED = createField("realized", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>reqbaz.requirement.creation_date</code>.
     */
    public final TableField<RequirementRecord, Timestamp> CREATION_DATE = createField("creation_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.inline("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>reqbaz.requirement.last_updated_date</code>.
     */
    public final TableField<RequirementRecord, Timestamp> LAST_UPDATED_DATE = createField("last_updated_date", org.jooq.impl.SQLDataType.TIMESTAMP, this, "");

    /**
     * The column <code>reqbaz.requirement.lead_developer_id</code>.
     */
    public final TableField<RequirementRecord, Integer> LEAD_DEVELOPER_ID = createField("lead_developer_id", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>reqbaz.requirement.creator_id</code>.
     */
    public final TableField<RequirementRecord, Integer> CREATOR_ID = createField("creator_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.requirement.project_id</code>.
     */
    public final TableField<RequirementRecord, Integer> PROJECT_ID = createField("project_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * Create a <code>reqbaz.requirement</code> table reference
     */
    public Requirement() {
        this("requirement", null);
    }

    /**
     * Create an aliased <code>reqbaz.requirement</code> table reference
     */
    public Requirement(String alias) {
        this(alias, REQUIREMENT);
    }

    private Requirement(String alias, Table<RequirementRecord> aliased) {
        this(alias, aliased, null);
    }

    private Requirement(String alias, Table<RequirementRecord> aliased, Field<?>[] parameters) {
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
    public Identity<RequirementRecord, Integer> getIdentity() {
        return Keys.IDENTITY_REQUIREMENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<RequirementRecord> getPrimaryKey() {
        return Keys.KEY_REQUIREMENT_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<RequirementRecord>> getKeys() {
        return Arrays.<UniqueKey<RequirementRecord>>asList(Keys.KEY_REQUIREMENT_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<RequirementRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<RequirementRecord, ?>>asList(Keys.LEAD_DEVELOPER, Keys.CREATOR, Keys.REQUIREMENT_PROJECT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Requirement as(String alias) {
        return new Requirement(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public Requirement rename(String name) {
        return new Requirement(name, null);
    }
}