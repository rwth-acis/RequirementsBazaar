/*
 * This file is generated by jOOQ.
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.SchemaVersionRecord;
import org.jooq.*;
import org.jooq.impl.TableImpl;

import javax.annotation.Generated;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;


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
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class SchemaVersion extends TableImpl<SchemaVersionRecord> {

    private static final long serialVersionUID = -1750837905;

    /**
     * The reference instance of <code>reqbaz.schema_version</code>
     */
    public static final SchemaVersion SCHEMA_VERSION = new SchemaVersion();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<SchemaVersionRecord> getRecordType() {
        return SchemaVersionRecord.class;
    }

    /**
     * The column <code>reqbaz.schema_version.installed_rank</code>.
     */
    public final TableField<SchemaVersionRecord, Integer> INSTALLED_RANK = createField("installed_rank", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.version</code>.
     */
    public final TableField<SchemaVersionRecord, String> VERSION = createField("version", org.jooq.impl.SQLDataType.VARCHAR.length(50), this, "");

    /**
     * The column <code>reqbaz.schema_version.description</code>.
     */
    public final TableField<SchemaVersionRecord, String> DESCRIPTION = createField("description", org.jooq.impl.SQLDataType.VARCHAR.length(200).nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.type</code>.
     */
    public final TableField<SchemaVersionRecord, String> TYPE = createField("type", org.jooq.impl.SQLDataType.VARCHAR.length(20).nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.script</code>.
     */
    public final TableField<SchemaVersionRecord, String> SCRIPT = createField("script", org.jooq.impl.SQLDataType.VARCHAR.length(1000).nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.checksum</code>.
     */
    public final TableField<SchemaVersionRecord, Integer> CHECKSUM = createField("checksum", org.jooq.impl.SQLDataType.INTEGER, this, "");

    /**
     * The column <code>reqbaz.schema_version.installed_by</code>.
     */
    public final TableField<SchemaVersionRecord, String> INSTALLED_BY = createField("installed_by", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.installed_on</code>.
     */
    public final TableField<SchemaVersionRecord, Timestamp> INSTALLED_ON = createField("installed_on", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.inline("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * The column <code>reqbaz.schema_version.execution_time</code>.
     */
    public final TableField<SchemaVersionRecord, Integer> EXECUTION_TIME = createField("execution_time", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.schema_version.success</code>.
     */
    public final TableField<SchemaVersionRecord, Byte> SUCCESS = createField("success", org.jooq.impl.SQLDataType.TINYINT.nullable(false), this, "");

    /**
     * Create a <code>reqbaz.schema_version</code> table reference
     */
    public SchemaVersion() {
        this("schema_version", null);
    }

    /**
     * Create an aliased <code>reqbaz.schema_version</code> table reference
     */
    public SchemaVersion(String alias) {
        this(alias, SCHEMA_VERSION);
    }

    private SchemaVersion(String alias, Table<SchemaVersionRecord> aliased) {
        this(alias, aliased, null);
    }

    private SchemaVersion(String alias, Table<SchemaVersionRecord> aliased, Field<?>[] parameters) {
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
    public UniqueKey<SchemaVersionRecord> getPrimaryKey() {
        return Keys.KEY_SCHEMA_VERSION_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<SchemaVersionRecord>> getKeys() {
        return Arrays.<UniqueKey<SchemaVersionRecord>>asList(Keys.KEY_SCHEMA_VERSION_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SchemaVersion as(String alias) {
        return new SchemaVersion(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public SchemaVersion rename(String name) {
        return new SchemaVersion(name, null);
    }
}
