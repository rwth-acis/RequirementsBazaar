/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementCategoryMap;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


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
public class RequirementCategoryMapRecord extends UpdatableRecordImpl<RequirementCategoryMapRecord> implements Record3<Integer, Integer, Integer> {

    private static final long serialVersionUID = -1847480760;

    /**
     * Setter for <code>reqbaz.requirement_category_map.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>reqbaz.requirement_category_map.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>reqbaz.requirement_category_map.category_id</code>.
     */
    public void setCategoryId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>reqbaz.requirement_category_map.category_id</code>.
     */
    public Integer getCategoryId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>reqbaz.requirement_category_map.requirement_id</code>.
     */
    public void setRequirementId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>reqbaz.requirement_category_map.requirement_id</code>.
     */
    public Integer getRequirementId() {
        return (Integer) get(2);
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
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP.CATEGORY_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP.REQUIREMENT_ID;
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
        return getCategoryId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getRequirementId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequirementCategoryMapRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequirementCategoryMapRecord value2(Integer value) {
        setCategoryId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequirementCategoryMapRecord value3(Integer value) {
        setRequirementId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequirementCategoryMapRecord values(Integer value1, Integer value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached RequirementCategoryMapRecord
     */
    public RequirementCategoryMapRecord() {
        super(RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP);
    }

    /**
     * Create a detached, initialised RequirementCategoryMapRecord
     */
    public RequirementCategoryMapRecord(Integer id, Integer categoryId, Integer requirementId) {
        super(RequirementCategoryMap.REQUIREMENT_CATEGORY_MAP);

        set(0, id);
        set(1, categoryId);
        set(2, requirementId);
    }
}