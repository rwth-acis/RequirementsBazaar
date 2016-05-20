/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachments;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record13;
import org.jooq.Row13;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class AttachmentsRecord extends UpdatableRecordImpl<AttachmentsRecord> implements Record13<Integer, Timestamp, Timestamp, Integer, Integer, String, String, String, String, String, String, String, String> {

	private static final long serialVersionUID = 551992590;

	/**
	 * Setter for <code>reqbaz.attachments.Id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.Id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>reqbaz.attachments.creation_time</code>.
	 */
	public void setCreationTime(Timestamp value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.creation_time</code>.
	 */
	public Timestamp getCreationTime() {
		return (Timestamp) getValue(1);
	}

	/**
	 * Setter for <code>reqbaz.attachments.lastupdated_time</code>.
	 */
	public void setLastupdatedTime(Timestamp value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.lastupdated_time</code>.
	 */
	public Timestamp getLastupdatedTime() {
		return (Timestamp) getValue(2);
	}

	/**
	 * Setter for <code>reqbaz.attachments.Requirement_Id</code>.
	 */
	public void setRequirementId(Integer value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.Requirement_Id</code>.
	 */
	public Integer getRequirementId() {
		return (Integer) getValue(3);
	}

	/**
	 * Setter for <code>reqbaz.attachments.User_Id</code>.
	 */
	public void setUserId(Integer value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.User_Id</code>.
	 */
	public Integer getUserId() {
		return (Integer) getValue(4);
	}

	/**
	 * Setter for <code>reqbaz.attachments.title</code>.
	 */
	public void setTitle(String value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.title</code>.
	 */
	public String getTitle() {
		return (String) getValue(5);
	}

	/**
	 * Setter for <code>reqbaz.attachments.discriminator</code>.
	 */
	public void setDiscriminator(String value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.discriminator</code>.
	 */
	public String getDiscriminator() {
		return (String) getValue(6);
	}

	/**
	 * Setter for <code>reqbaz.attachments.file_path</code>.
	 */
	public void setFilePath(String value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.file_path</code>.
	 */
	public String getFilePath() {
		return (String) getValue(7);
	}

	/**
	 * Setter for <code>reqbaz.attachments.description</code>.
	 */
	public void setDescription(String value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(8);
	}

	/**
	 * Setter for <code>reqbaz.attachments.story</code>.
	 */
	public void setStory(String value) {
		setValue(9, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.story</code>.
	 */
	public String getStory() {
		return (String) getValue(9);
	}

	/**
	 * Setter for <code>reqbaz.attachments.subject</code>.
	 */
	public void setSubject(String value) {
		setValue(10, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.subject</code>.
	 */
	public String getSubject() {
		return (String) getValue(10);
	}

	/**
	 * Setter for <code>reqbaz.attachments.object</code>.
	 */
	public void setObject(String value) {
		setValue(11, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.object</code>.
	 */
	public String getObject() {
		return (String) getValue(11);
	}

	/**
	 * Setter for <code>reqbaz.attachments.object_desc</code>.
	 */
	public void setObjectDesc(String value) {
		setValue(12, value);
	}

	/**
	 * Getter for <code>reqbaz.attachments.object_desc</code>.
	 */
	public String getObjectDesc() {
		return (String) getValue(12);
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
	// Record13 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row13<Integer, Timestamp, Timestamp, Integer, Integer, String, String, String, String, String, String, String, String> fieldsRow() {
		return (Row13) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row13<Integer, Timestamp, Timestamp, Integer, Integer, String, String, String, String, String, String, String, String> valuesRow() {
		return (Row13) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return Attachments.ATTACHMENTS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field2() {
		return Attachments.ATTACHMENTS.CREATION_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field3() {
		return Attachments.ATTACHMENTS.LASTUPDATED_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field4() {
		return Attachments.ATTACHMENTS.REQUIREMENT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field5() {
		return Attachments.ATTACHMENTS.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field6() {
		return Attachments.ATTACHMENTS.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field7() {
		return Attachments.ATTACHMENTS.DISCRIMINATOR;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field8() {
		return Attachments.ATTACHMENTS.FILE_PATH;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field9() {
		return Attachments.ATTACHMENTS.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field10() {
		return Attachments.ATTACHMENTS.STORY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field11() {
		return Attachments.ATTACHMENTS.SUBJECT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field12() {
		return Attachments.ATTACHMENTS.OBJECT;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field13() {
		return Attachments.ATTACHMENTS.OBJECT_DESC;
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
	public Timestamp value2() {
		return getCreationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value3() {
		return getLastupdatedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value4() {
		return getRequirementId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value5() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value6() {
		return getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value7() {
		return getDiscriminator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value8() {
		return getFilePath();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value9() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value10() {
		return getStory();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value11() {
		return getSubject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value12() {
		return getObject();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value13() {
		return getObjectDesc();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value2(Timestamp value) {
		setCreationTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value3(Timestamp value) {
		setLastupdatedTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value4(Integer value) {
		setRequirementId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value5(Integer value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value6(String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value7(String value) {
		setDiscriminator(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value8(String value) {
		setFilePath(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value9(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value10(String value) {
		setStory(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value11(String value) {
		setSubject(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value12(String value) {
		setObject(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord value13(String value) {
		setObjectDesc(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AttachmentsRecord values(Integer value1, Timestamp value2, Timestamp value3, Integer value4, Integer value5, String value6, String value7, String value8, String value9, String value10, String value11, String value12, String value13) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		value9(value9);
		value10(value10);
		value11(value11);
		value12(value12);
		value13(value13);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached AttachmentsRecord
	 */
	public AttachmentsRecord() {
		super(Attachments.ATTACHMENTS);
	}

	/**
	 * Create a detached, initialised AttachmentsRecord
	 */
	public AttachmentsRecord(Integer id, Timestamp creationTime, Timestamp lastupdatedTime, Integer requirementId, Integer userId, String title, String discriminator, String filePath, String description, String story, String subject, String object, String objectDesc) {
		super(Attachments.ATTACHMENTS);

		setValue(0, id);
		setValue(1, creationTime);
		setValue(2, lastupdatedTime);
		setValue(3, requirementId);
		setValue(4, userId);
		setValue(5, title);
		setValue(6, discriminator);
		setValue(7, filePath);
		setValue(8, description);
		setValue(9, story);
		setValue(10, subject);
		setValue(11, object);
		setValue(12, objectDesc);
	}
}
