/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value = {"http://www.jooq.org", "3.4.2"},
        comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({"all", "unchecked", "rawtypes"})
public class ComponentsRecord extends org.jooq.impl.UpdatableRecordImpl<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentsRecord> implements org.jooq.Record7<java.lang.Integer, java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> {

    private static final long serialVersionUID = -1828233081;

    /**
     * Setter for <code>reqbaz.components.Id</code>.
     */
    public void setId(java.lang.Integer value) {
        setValue(0, value);
    }

    /**
     * Getter for <code>reqbaz.components.Id</code>.
     */
    public java.lang.Integer getId() {
        return (java.lang.Integer) getValue(0);
    }

    /**
     * Setter for <code>reqbaz.components.name</code>.
     */
    public void setName(java.lang.String value) {
        setValue(1, value);
    }

    /**
     * Getter for <code>reqbaz.components.name</code>.
     */
    public java.lang.String getName() {
        return (java.lang.String) getValue(1);
    }

    /**
     * Setter for <code>reqbaz.components.description</code>.
     */
    public void setDescription(java.lang.String value) {
        setValue(2, value);
    }

    /**
     * Getter for <code>reqbaz.components.description</code>.
     */
    public java.lang.String getDescription() {
        return (java.lang.String) getValue(2);
    }

    /**
     * Setter for <code>reqbaz.components.creation_time</code>.
     */
    public void setCreationTime(java.sql.Timestamp value) {
        setValue(3, value);
    }

    /**
     * Getter for <code>reqbaz.components.creation_time</code>.
     */
    public java.sql.Timestamp getCreationTime() {
        return (java.sql.Timestamp) getValue(3);
    }

    /**
     * Setter for <code>reqbaz.components.lastupdated_time</code>.
     */
    public void setLastupdatedTime(java.sql.Timestamp value) {
        setValue(4, value);
    }

    /**
     * Getter for <code>reqbaz.components.lastupdated_time</code>.
     */
    public java.sql.Timestamp getLastupdatedTime() {
        return (java.sql.Timestamp) getValue(4);
    }

    /**
     * Setter for <code>reqbaz.components.Project_Id</code>.
     */
    public void setProjectId(java.lang.Integer value) {
        setValue(5, value);
    }

    /**
     * Getter for <code>reqbaz.components.Project_Id</code>.
     */
    public java.lang.Integer getProjectId() {
        return (java.lang.Integer) getValue(5);
    }

    /**
     * Setter for <code>reqbaz.components.Leader_Id</code>.
     */
    public void setLeaderId(java.lang.Integer value) {
        setValue(6, value);
    }

    /**
     * Getter for <code>reqbaz.components.Leader_Id</code>.
     */
    public java.lang.Integer getLeaderId() {
        return (java.lang.Integer) getValue(6);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Record1<java.lang.Integer> key() {
        return (org.jooq.Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Row7<java.lang.Integer, java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> fieldsRow() {
        return (org.jooq.Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Row7<java.lang.Integer, java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, java.lang.Integer, java.lang.Integer> valuesRow() {
        return (org.jooq.Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field1() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.String> field2() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.String> field3() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.sql.Timestamp> field4() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.CREATION_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.sql.Timestamp> field5() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.LASTUPDATED_TIME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field6() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.PROJECT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field7() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS.LEADER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.lang.Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.lang.String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.lang.String value3() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.sql.Timestamp value4() {
        return getCreationTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.sql.Timestamp value5() {
        return getLastupdatedTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.lang.Integer value6() {
        return getProjectId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public java.lang.Integer value7() {
        return getLeaderId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value1(java.lang.Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value2(java.lang.String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value3(java.lang.String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value4(java.sql.Timestamp value) {
        setCreationTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value5(java.sql.Timestamp value) {
        setLastupdatedTime(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value6(java.lang.Integer value) {
        setProjectId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord value7(java.lang.Integer value) {
        setLeaderId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ComponentsRecord values(java.lang.Integer value1, java.lang.String value2, java.lang.String value3, java.sql.Timestamp value4, java.sql.Timestamp value5, java.lang.Integer value6, java.lang.Integer value7) {
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ComponentsRecord
     */
    public ComponentsRecord() {
        super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS);
    }

    /**
     * Create a detached, initialised ComponentsRecord
     */
    public ComponentsRecord(java.lang.Integer id, java.lang.String name, java.lang.String description, java.sql.Timestamp creationTime, java.sql.Timestamp lastupdatedTime, java.lang.Integer projectId, java.lang.Integer leaderId) {
        super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Components.COMPONENTS);

        setValue(0, id);
        setValue(1, name);
        setValue(2, description);
        setValue(3, creationTime);
        setValue(4, lastupdatedTime);
        setValue(5, projectId);
        setValue(6, leaderId);
    }
}
