/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectFollowerRecord;

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
        "jOOQ version:3.8.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ProjectFollower extends TableImpl<ProjectFollowerRecord> {

    private static final long serialVersionUID = 1537717373;

    /**
     * The reference instance of <code>reqbaz.project_follower</code>
     */
    public static final ProjectFollower PROJECT_FOLLOWER = new ProjectFollower();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<ProjectFollowerRecord> getRecordType() {
        return ProjectFollowerRecord.class;
    }

    /**
     * The column <code>reqbaz.project_follower.Id</code>.
     */
    public final TableField<ProjectFollowerRecord, Integer> ID = createField("Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower.Project_Id</code>.
     */
    public final TableField<ProjectFollowerRecord, Integer> PROJECT_ID = createField("Project_Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower.User_Id</code>.
     */
    public final TableField<ProjectFollowerRecord, Integer> USER_ID = createField("User_Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>reqbaz.project_follower.creation_time</code>.
     */
    public final TableField<ProjectFollowerRecord, Timestamp> CREATION_TIME = createField("creation_time", org.jooq.impl.SQLDataType.TIMESTAMP.nullable(false).defaultValue(org.jooq.impl.DSL.inline("CURRENT_TIMESTAMP", org.jooq.impl.SQLDataType.TIMESTAMP)), this, "");

    /**
     * Create a <code>reqbaz.project_follower</code> table reference
     */
    public ProjectFollower() {
        this("project_follower", null);
    }

    /**
     * Create an aliased <code>reqbaz.project_follower</code> table reference
     */
    public ProjectFollower(String alias) {
        this(alias, PROJECT_FOLLOWER);
    }

    private ProjectFollower(String alias, Table<ProjectFollowerRecord> aliased) {
        this(alias, aliased, null);
    }

    private ProjectFollower(String alias, Table<ProjectFollowerRecord> aliased, Field<?>[] parameters) {
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
    public Identity<ProjectFollowerRecord, Integer> getIdentity() {
        return Keys.IDENTITY_PROJECT_FOLLOWER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<ProjectFollowerRecord> getPrimaryKey() {
        return Keys.KEY_PROJECT_FOLLOWER_PRIMARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<ProjectFollowerRecord>> getKeys() {
        return Arrays.<UniqueKey<ProjectFollowerRecord>>asList(Keys.KEY_PROJECT_FOLLOWER_PRIMARY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ForeignKey<ProjectFollowerRecord, ?>> getReferences() {
        return Arrays.<ForeignKey<ProjectFollowerRecord, ?>>asList(Keys.PROJECT_FOLLOWER, Keys.PROJECT_FOLLOWER_USER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ProjectFollower as(String alias) {
        return new ProjectFollower(alias, this);
    }

    /**
     * Rename this table
     */
    public ProjectFollower rename(String name) {
        return new ProjectFollower(name, null);
    }
}