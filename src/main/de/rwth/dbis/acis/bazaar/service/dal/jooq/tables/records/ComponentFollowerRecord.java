/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollower;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record4;
import org.jooq.Row4;
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
public class ComponentFollowerRecord extends UpdatableRecordImpl<ComponentFollowerRecord> implements Record4<Integer, Integer, Integer, Timestamp> {

	private static final long serialVersionUID = -2003043908;

	/**
	 * Setter for <code>reqbaz.component_follower.Id</code>.
	 */
	public void setId(Integer value) {
		setValue(0, value);
	}

	/**
	 * Getter for <code>reqbaz.component_follower.Id</code>.
	 */
	public Integer getId() {
		return (Integer) getValue(0);
	}

	/**
	 * Setter for <code>reqbaz.component_follower.Component_Id</code>.
	 */
	public void setComponentId(Integer value) {
		setValue(1, value);
	}

	/**
	 * Getter for <code>reqbaz.component_follower.Component_Id</code>.
	 */
	public Integer getComponentId() {
		return (Integer) getValue(1);
	}

	/**
	 * Setter for <code>reqbaz.component_follower.User_Id</code>.
	 */
	public void setUserId(Integer value) {
		setValue(2, value);
	}

	/**
	 * Getter for <code>reqbaz.component_follower.User_Id</code>.
	 */
	public Integer getUserId() {
		return (Integer) getValue(2);
	}

	/**
	 * Setter for <code>reqbaz.component_follower.creation_time</code>.
	 */
	public void setCreationTime(Timestamp value) {
		setValue(3, value);
	}

	/**
	 * Getter for <code>reqbaz.component_follower.creation_time</code>.
	 */
	public Timestamp getCreationTime() {
		return (Timestamp) getValue(3);
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
	// Record4 type implementation
	// -------------------------------------------------------------------------

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Integer, Integer, Integer, Timestamp> fieldsRow() {
		return (Row4) super.fieldsRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Row4<Integer, Integer, Integer, Timestamp> valuesRow() {
		return (Row4) super.valuesRow();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field1() {
		return ComponentFollower.COMPONENT_FOLLOWER.ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field2() {
		return ComponentFollower.COMPONENT_FOLLOWER.COMPONENT_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Integer> field3() {
		return ComponentFollower.COMPONENT_FOLLOWER.USER_ID;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Field<Timestamp> field4() {
		return ComponentFollower.COMPONENT_FOLLOWER.CREATION_TIME;
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
	public Integer value2() {
		return getComponentId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Integer value3() {
		return getUserId();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Timestamp value4() {
		return getCreationTime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentFollowerRecord value1(Integer value) {
		setId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentFollowerRecord value2(Integer value) {
		setComponentId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentFollowerRecord value3(Integer value) {
		setUserId(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentFollowerRecord value4(Timestamp value) {
		setCreationTime(value);
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ComponentFollowerRecord values(Integer value1, Integer value2, Integer value3, Timestamp value4) {
		value1(value1);
		value2(value2);
		value3(value3);
		value4(value4);
		return this;
	}

	// -------------------------------------------------------------------------
	// Constructors
	// -------------------------------------------------------------------------

	/**
	 * Create a detached ComponentFollowerRecord
	 */
	public ComponentFollowerRecord() {
		super(ComponentFollower.COMPONENT_FOLLOWER);
	}

	/**
	 * Create a detached, initialised ComponentFollowerRecord
	 */
	public ComponentFollowerRecord(Integer id, Integer componentId, Integer userId, Timestamp creationTime) {
		super(ComponentFollower.COMPONENT_FOLLOWER);

		setValue(0, id);
		setValue(1, componentId);
		setValue(2, userId);
		setValue(3, creationTime);
	}
}
