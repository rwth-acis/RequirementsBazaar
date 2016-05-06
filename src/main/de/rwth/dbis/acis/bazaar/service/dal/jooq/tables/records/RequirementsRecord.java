/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
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
public class RequirementsRecord extends UpdatableRecordImpl<RequirementsRecord> implements Record9<Integer, String, String, Timestamp, Timestamp, Timestamp, Integer, Integer, Integer> {

	private static final long serialVersionUID = -305233084;

	/**
	 * Setter for <code>reqbaz.requirements.Id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.Id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>reqbaz.requirements.title</code>.
	 */
	public void setTitle(String value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.title</code>.
	 */
	public String getTitle() {
		return (String) getValue(1);
	}

	/**
	 * Setter for <code>reqbaz.requirements.description</code>.
	 */
	public void setDescription(String value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.description</code>.
	 */
	public String getDescription() {
		return (String) getValue(2);
	}

	/**
	 * Setter for <code>reqbaz.requirements.realized</code>.
	 */
	public void setRealized(Timestamp value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.realized</code>.
	 */
	public Timestamp getRealized() {
		return (Timestamp) getValue(3);
	}

	/**
	 * Setter for <code>reqbaz.requirements.creation_time</code>.
	 */
	public void setCreationTime(Timestamp value) {
		setValue(4, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.creation_time</code>.
	 */
	public Timestamp getCreationTime() {
		return (Timestamp) getValue(4);
	}

	/**
	 * Setter for <code>reqbaz.requirements.lastupdated_time</code>.
	 */
	public void setLastupdatedTime(Timestamp value) {
		setValue(5, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.lastupdated_time</code>.
	 */
	public Timestamp getLastupdatedTime() {
		return (Timestamp) getValue(5);
	}

	/**
	 * Setter for <code>reqbaz.requirements.Lead_developer_Id</code>.
	 */
	public void setLeadDeveloperId(Integer value) {
		setValue(6, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.Lead_developer_Id</code>.
	 */
	public Integer getLeadDeveloperId() {
		return (Integer) getValue(6);
	}

	/**
	 * Setter for <code>reqbaz.requirements.Creator_Id</code>.
	 */
	public void setCreatorId(Integer value) {
		setValue(7, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.Creator_Id</code>.
	 */
	public Integer getCreatorId() {
		return (Integer) getValue(7);
	}

	/**
	 * Setter for <code>reqbaz.requirements.Project_Id</code>.
	 */
	public void setProjectId(Integer value) {
		setValue(8, value);
	}

	/**
	 * Getter for <code>reqbaz.requirements.Project_Id</code>.
	 */
	public Integer getProjectId() {
		return (Integer) getValue(8);
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
	// Record9 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row9<Integer, String, String, Timestamp, Timestamp, Timestamp, Integer, Integer, Integer> fieldsRow() {
		return (Row9) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row9<Integer, String, String, Timestamp, Timestamp, Timestamp, Integer, Integer, Integer> valuesRow() {
		return (Row9) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return Requirements.REQUIREMENTS.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field2() {
		return Requirements.REQUIREMENTS.TITLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<String> field3() {
		return Requirements.REQUIREMENTS.DESCRIPTION;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field4() {
		return Requirements.REQUIREMENTS.REALIZED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field5() {
		return Requirements.REQUIREMENTS.CREATION_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field6() {
		return Requirements.REQUIREMENTS.LASTUPDATED_TIME;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field7() {
		return Requirements.REQUIREMENTS.LEAD_DEVELOPER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field8() {
		return Requirements.REQUIREMENTS.CREATOR_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field9() {
		return Requirements.REQUIREMENTS.PROJECT_ID;
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
	public String value2() {
		return getTitle();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String value3() {
		return getDescription();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value4() {
		return getRealized();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value5() {
		return getCreationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value6() {
		return getLastupdatedTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value7() {
		return getLeadDeveloperId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value8() {
		return getCreatorId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value9() {
		return getProjectId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value2(String value) {
		setTitle(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value3(String value) {
		setDescription(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value4(Timestamp value) {
		setRealized(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value5(Timestamp value) {
		setCreationTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value6(Timestamp value) {
		setLastupdatedTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value7(Integer value) {
		setLeadDeveloperId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value8(Integer value) {
		setCreatorId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord value9(Integer value) {
		setProjectId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RequirementsRecord values(Integer value1, String value2, String value3, Timestamp value4, Timestamp value5, Timestamp value6, Integer value7, Integer value8, Integer value9) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		value5(value5);
		value6(value6);
		value7(value7);
		value8(value8);
		value9(value9);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached RequirementsRecord
	 */
	public RequirementsRecord() {
		super(Requirements.REQUIREMENTS);
	}

	/**
	 * Create a detached, initialised RequirementsRecord
	 */
	public RequirementsRecord(Integer id, String title, String description, Timestamp realized, Timestamp creationTime, Timestamp lastupdatedTime, Integer leadDeveloperId, Integer creatorId, Integer projectId) {
		super(Requirements.REQUIREMENTS);

		setValue(0, id);
		setValue(1, title);
		setValue(2, description);
		setValue(3, realized);
		setValue(4, creationTime);
		setValue(5, lastupdatedTime);
		setValue(6, leadDeveloperId);
		setValue(7, creatorId);
		setValue(8, projectId);
	}
}
