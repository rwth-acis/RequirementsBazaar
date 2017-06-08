/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectFollowerMapRecord;

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
public class ProjectFollowerMap extends TableImpl<ProjectFollowerMapRecord> {

    private static final long serialVersionUID = -1701261507;

    /**
     * The reference instance of <code>reqbaz.project_follower_map</code>
     */
    public static final ProjectFollowerMap PROJECT_FOLLOWER_MAP = new ProjectFollowerMap();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProjectFollowerMapRecord> getRecordType() {
        return ProjectFollowerMapRecord.class;
    }

    /**
     * The column <code>reqbaz.project_follower_map.id</code>.
     */
    public final TableField<ProjectFollowerMapRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower_map.project_id</code>.
     */
    public final TableField<ProjectFollowerMapRecord, Integer> PROJECT_ID = createField("project_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower_map.user_id</code>.
     */
    public final TableField<ProjectFollowerMapRecord, Integer> USER_ID = createField("user_id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower_map.creation_date</code>.
     */
    public final TableField<ProjectFollowerMapRecord, Timestamp> CREATION_DATE = createField("creation_date", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.inline("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * Create a <code>reqbaz.project_follower_map</code> table reference
     */
    public ProjectFollowerMap() {
        this("project_follower_map", null);
    }

    /**
     * Create an aliased <code>reqbaz.project_follower_map</code> table reference
     */
    public ProjectFollowerMap(String alias) {
        this(alias, PROJECT_FOLLOWER_MAP);
    }

    private ProjectFollowerMap(String alias, Table<ProjectFollowerMapRecord> aliased) {
        this(alias, aliased, null);
    }

    private ProjectFollowerMap(String alias, Table<ProjectFollowerMapRecord> aliased, Field<?>[] parameters) {
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
    public Identity<ProjectFollowerMapRecord, Integer> getIdentity() {
        return Keys.IDENTITY_PROJECT_FOLLOWER_MAP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProjectFollowerMapRecord> getPrimaryKey() {
        return Keys.KEY_PROJECT_FOLLOWER_MAP_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProjectFollowerMapRecord>> getKeys() {
        return Arrays.<UniqueKey<ProjectFollowerMapRecord>>asList(Keys.KEY_PROJECT_FOLLOWER_MAP_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ProjectFollowerMapRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ProjectFollowerMapRecord, ?>>asList(Keys.PROJECT_FOLLOWER_MAP_PROJECT, Keys.PROJECT_FOLLOWER_MAP_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectFollowerMap as(String alias) {
        return new ProjectFollowerMap(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public ProjectFollowerMap rename(String name) {
        return new ProjectFollowerMap(name, null);
    }
}