/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.2" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AttachementsRecord extends org.jooq.impl.UpdatableRecordImpl<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachementsRecord> implements org.jooq.Record12<java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String> {

	private static final long serialVersionUID = 1323537098;

	/**
	 * Setter for <code>reqbaz.attachements.Id</code>.
	 */
	public void setId(java.lang.Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.Id</code>.
	 */
	public java.lang.Integer getId() {
		return (java.lang.Integer) getValue(0);
	}

	/**
	 * Setter for <code>reqbaz.attachements.creation_time</code>.
	 */
	public void setCreationTime(java.sql.Timestamp value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.creation_time</code>.
	 */
	public java.sql.Timestamp getCreationTime() {
		return (java.sql.Timestamp) getValue(1);
	}

	/**
	 * Setter for <code>reqbaz.attachements.Requirement_Id</code>.
	 */
	public void setRequirementId(java.lang.Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.Requirement_Id</code>.
	 */
	public java.lang.Integer getRequirementId() {
		return (java.lang.Integer) getValue(2);
	}

	/**
	 * Setter for <code>reqbaz.attachements.User_Id</code>.
	 */
	public void setUserId(java.lang.Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.User_Id</code>.
	 */
	public java.lang.Integer getUserId() {
		return (java.lang.Integer) getValue(3);
	}

	/**
	 * Setter for <code>reqbaz.attachements.title</code>.
	 */
	public void setTitle(java.lang.String value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.title</code>.
	 */
	public java.lang.String getTitle() {
		return (java.lang.String) getValue(4);
	}

	/**
	 * Setter for <code>reqbaz.attachements.discriminator</code>.
	 */
	public void setDiscriminator(java.lang.String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.discriminator</code>.
	 */
	public java.lang.String getDiscriminator() {
		return (java.lang.String) getValue(5);
	}

	/**
	 * Setter for <code>reqbaz.attachements.file_path</code>.
	 */
	public void setFilePath(java.lang.String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.file_path</code>.
	 */
	public java.lang.String getFilePath() {
		return (java.lang.String) getValue(6);
	}

	/**
	 * Setter for <code>reqbaz.attachements.description</code>.
	 */
	public void setDescription(java.lang.String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.description</code>.
	 */
	public java.lang.String getDescription() {
		return (java.lang.String) getValue(7);
	}

	/**
	 * Setter for <code>reqbaz.attachements.story</code>.
	 */
	public void setStory(java.lang.String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.story</code>.
	 */
	public java.lang.String getStory() {
		return (java.lang.String) getValue(8);
	}

	/**
	 * Setter for <code>reqbaz.attachements.subject</code>.
	 */
	public void setSubject(java.lang.String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.subject</code>.
	 */
	public java.lang.String getSubject() {
		return (java.lang.String) getValue(9);
	}

	/**
	 * Setter for <code>reqbaz.attachements.object</code>.
	 */
	public void setObject(java.lang.String value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.object</code>.
	 */
	public java.lang.String getObject() {
		return (java.lang.String) getValue(10);
	}

	/**
	 * Setter for <code>reqbaz.attachements.object_desc</code>.
	 */
	public void setObjectDesc(java.lang.String value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>reqbaz.attachements.object_desc</code>.
	 */
	public java.lang.String getObjectDesc() {
		return (java.lang.String) getValue(11);
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
	// Record12 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row12<java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String> fieldsRow() {
		return (org.jooq.Row12) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Row12<java.lang.Integer, java.sql.Timestamp, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String> valuesRow() {
		return (org.jooq.Row12) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field1() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.sql.Timestamp> field2() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.CREATION_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field3() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.REQUIREMENT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.Integer> field4() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field5() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field6() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.DISCRIMINATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field7() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.FILE_PATH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field8() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field9() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.STORY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field10() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.SUBJECT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field11() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.OBJECT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Field<java.lang.String> field12() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS.OBJECT_DESC;
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
	public java.sql.Timestamp value2() {
		return getCreationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value3() {
		return getRequirementId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.Integer value4() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value5() {
		return getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value6() {
		return getDiscriminator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value7() {
		return getFilePath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value8() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value9() {
		return getStory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value10() {
		return getSubject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value11() {
		return getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.lang.String value12() {
		return getObjectDesc();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value1(java.lang.Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value2(java.sql.Timestamp value) {
		setCreationTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value3(java.lang.Integer value) {
		setRequirementId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value4(java.lang.Integer value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value5(java.lang.String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value6(java.lang.String value) {
		setDiscriminator(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value7(java.lang.String value) {
		setFilePath(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value8(java.lang.String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value9(java.lang.String value) {
		setStory(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value10(java.lang.String value) {
		setSubject(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value11(java.lang.String value) {
		setObject(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord value12(java.lang.String value) {
		setObjectDesc(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachementsRecord values(java.lang.Integer value1, java.sql.Timestamp value2, java.lang.Integer value3, java.lang.Integer value4, java.lang.String value5, java.lang.String value6, java.lang.String value7, java.lang.String value8, java.lang.String value9, java.lang.String value10, java.lang.String value11, java.lang.String value12) {
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AttachementsRecord
	 */
	public AttachementsRecord() {
		super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS);
	}

	/**
	 * Create a detached, initialised AttachementsRecord
	 */
	public AttachementsRecord(java.lang.Integer id, java.sql.Timestamp creationTime, java.lang.Integer requirementId, java.lang.Integer userId, java.lang.String title, java.lang.String discriminator, java.lang.String filePath, java.lang.String description, java.lang.String story, java.lang.String subject, java.lang.String object, java.lang.String objectDesc) {
		super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS);

		setValue(0, id);
		setValue(1, creationTime);
		setValue(2, requirementId);
		setValue(3, userId);
		setValue(4, title);
		setValue(5, discriminator);
		setValue(6, filePath);
		setValue(7, description);
		setValue(8, story);
		setValue(9, subject);
		setValue(10, object);
		setValue(11, objectDesc);
	}
}