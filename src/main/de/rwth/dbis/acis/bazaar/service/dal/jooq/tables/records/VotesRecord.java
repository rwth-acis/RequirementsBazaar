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
public class VotesRecord extends org.jooq.impl.UpdatableRecordImpl<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.VotesRecord> implements org.jooq.Record4<java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Integer> {

    private static final long serialVersionUID = -206434781;

    /**
     * Setter for <code>reqbaz.votes.Id</code>.
     */
    public void setId(java.lang.Integer value) {
        setValue(0, value);
    }

    /**
     * Getter for <code>reqbaz.votes.Id</code>.
     */
    public java.lang.Integer getId() {
        return (java.lang.Integer) getValue(0);
    }

    /**
     * Setter for <code>reqbaz.votes.is_upvote</code>.
     */
    public void setIsUpvote(java.lang.Byte value) {
        setValue(1, value);
    }

    /**
     * Getter for <code>reqbaz.votes.is_upvote</code>.
     */
    public java.lang.Byte getIsUpvote() {
        return (java.lang.Byte) getValue(1);
    }

    /**
     * Setter for <code>reqbaz.votes.Requirement_Id</code>.
     */
    public void setRequirementId(java.lang.Integer value) {
        setValue(2, value);
    }

    /**
     * Getter for <code>reqbaz.votes.Requirement_Id</code>.
     */
    public java.lang.Integer getRequirementId() {
        return (java.lang.Integer) getValue(2);
    }

    /**
     * Setter for <code>reqbaz.votes.User_Id</code>.
     */
    public void setUserId(java.lang.Integer value) {
        setValue(3, value);
    }

    /**
     * Getter for <code>reqbaz.votes.User_Id</code>.
     */
    public java.lang.Integer getUserId() {
        return (java.lang.Integer) getValue(3);
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
    // Record4 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Row4<java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Integer> fieldsRow() {
        return (org.jooq.Row4) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Row4<java.lang.Integer, java.lang.Byte, java.lang.Integer, java.lang.Integer> valuesRow() {
        return (org.jooq.Row4) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field1() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Byte> field2() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES.IS_UPVOTE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field3() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES.REQUIREMENT_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public org.jooq.Field<java.lang.Integer> field4() {
        return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES.USER_ID;
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
    public java.lang.Byte value2() {
        return getIsUpvote();
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
    public VotesRecord value1(java.lang.Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VotesRecord value2(java.lang.Byte value) {
        setIsUpvote(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VotesRecord value3(java.lang.Integer value) {
        setRequirementId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VotesRecord value4(java.lang.Integer value) {
        setUserId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VotesRecord values(java.lang.Integer value1, java.lang.Byte value2, java.lang.Integer value3, java.lang.Integer value4) {
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached VotesRecord
     */
    public VotesRecord() {
        super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES);
    }

    /**
     * Create a detached, initialised VotesRecord
     */
    public VotesRecord(java.lang.Integer id, java.lang.Byte isUpvote, java.lang.Integer requirementId, java.lang.Integer userId) {
        super(de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes.VOTES);

        setValue(0, id);
        setValue(1, isUpvote);
        setValue(2, requirementId);
        setValue(3, userId);
    }
}
