/*
 * This file is generated by jOOQ.
*/
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User;

import java.sql.Timestamp;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record14;
import org.jooq.Row14;
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
public class UserRecord extends UpdatableRecordImpl<UserRecord> implements Record14<Integer, String, String, String, Boolean, String, String, String, Byte, Byte, Timestamp, Timestamp, Timestamp, Byte> {

    private static final long serialVersionUID = 1368312286;

    /**
     * Setter for <code>reqbaz.user.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>reqbaz.user.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>reqbaz.user.first_name</code>.
     */
    public void setFirstName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>reqbaz.user.first_name</code>.
     */
    public String getFirstName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>reqbaz.user.last_name</code>.
     */
    public void setLastName(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>reqbaz.user.last_name</code>.
     */
    public String getLastName() {
        return (String) get(2);
    }

    /**
     * Setter for <code>reqbaz.user.email</code>.
     */
    public void setEmail(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>reqbaz.user.email</code>.
     */
    public String getEmail() {
        return (String) get(3);
    }

    /**
     * Setter for <code>reqbaz.user.admin</code>.
     */
    public void setAdmin(Boolean value) {
        set(4, value);
    }

    /**
     * Getter for <code>reqbaz.user.admin</code>.
     */
    public Boolean getAdmin() {
        return (Boolean) get(4);
    }

    /**
     * Setter for <code>reqbaz.user.las2peer_id</code>.
     */
    public void setLas2peerId(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>reqbaz.user.las2peer_id</code>.
     */
    public String getLas2peerId() {
        return (String) get(5);
    }

    /**
     * Setter for <code>reqbaz.user.user_name</code>.
     */
    public void setUserName(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>reqbaz.user.user_name</code>.
     */
    public String getUserName() {
        return (String) get(6);
    }

    /**
     * Setter for <code>reqbaz.user.profile_image</code>.
     */
    public void setProfileImage(String value) {
        set(7, value);
    }

    /**
     * Getter for <code>reqbaz.user.profile_image</code>.
     */
    public String getProfileImage() {
        return (String) get(7);
    }

    /**
     * Setter for <code>reqbaz.user.email_lead_subscription</code>.
     */
    public void setEmailLeadSubscription(Byte value) {
        set(8, value);
    }

    /**
     * Getter for <code>reqbaz.user.email_lead_subscription</code>.
     */
    public Byte getEmailLeadSubscription() {
        return (Byte) get(8);
    }

    /**
     * Setter for <code>reqbaz.user.email_follow_subscription</code>.
     */
    public void setEmailFollowSubscription(Byte value) {
        set(9, value);
    }

    /**
     * Getter for <code>reqbaz.user.email_follow_subscription</code>.
     */
    public Byte getEmailFollowSubscription() {
        return (Byte) get(9);
    }

    /**
     * Setter for <code>reqbaz.user.creation_date</code>.
     */
    public void setCreationDate(Timestamp value) {
        set(10, value);
    }

    /**
     * Getter for <code>reqbaz.user.creation_date</code>.
     */
    public Timestamp getCreationDate() {
        return (Timestamp) get(10);
    }

    /**
     * Setter for <code>reqbaz.user.last_updated_date</code>.
     */
    public void setLastUpdatedDate(Timestamp value) {
        set(11, value);
    }

    /**
     * Getter for <code>reqbaz.user.last_updated_date</code>.
     */
    public Timestamp getLastUpdatedDate() {
        return (Timestamp) get(11);
    }

    /**
     * Setter for <code>reqbaz.user.last_login_date</code>.
     */
    public void setLastLoginDate(Timestamp value) {
        set(12, value);
    }

    /**
     * Getter for <code>reqbaz.user.last_login_date</code>.
     */
    public Timestamp getLastLoginDate() {
        return (Timestamp) get(12);
    }

    /**
     * Setter for <code>reqbaz.user.personalization_enabled</code>.
     */
    public void setPersonalizationEnabled(Byte value) {
        set(13, value);
    }

    /**
     * Getter for <code>reqbaz.user.personalization_enabled</code>.
     */
    public Byte getPersonalizationEnabled() {
        return (Byte) get(13);
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
    // Record14 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row14<Integer, String, String, String, Boolean, String, String, String, Byte, Byte, Timestamp, Timestamp, Timestamp, Byte> fieldsRow() {
        return (Row14) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row14<Integer, String, String, String, Boolean, String, String, String, Byte, Byte, Timestamp, Timestamp, Timestamp, Byte> valuesRow() {
        return (Row14) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return User.USER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return User.USER.FIRST_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return User.USER.LAST_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return User.USER.EMAIL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field5() {
        return User.USER.ADMIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return User.USER.LAS2PEER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return User.USER.USER_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field8() {
        return User.USER.PROFILE_IMAGE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field9() {
        return User.USER.EMAIL_LEAD_SUBSCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field10() {
        return User.USER.EMAIL_FOLLOW_SUBSCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field11() {
        return User.USER.CREATION_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field12() {
        return User.USER.LAST_UPDATED_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field13() {
        return User.USER.LAST_LOGIN_DATE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Byte> field14() {
        return User.USER.PERSONALIZATION_ENABLED;
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
        return getFirstName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getLastName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getEmail();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value5() {
        return getAdmin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getLas2peerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getUserName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value8() {
        return getProfileImage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value9() {
        return getEmailLeadSubscription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value10() {
        return getEmailFollowSubscription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value11() {
        return getCreationDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value12() {
        return getLastUpdatedDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value13() {
        return getLastLoginDate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Byte value14() {
        return getPersonalizationEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value2(String value) {
        setFirstName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value3(String value) {
        setLastName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value4(String value) {
        setEmail(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value5(Boolean value) {
        setAdmin(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value6(String value) {
        setLas2peerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value7(String value) {
        setUserName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value8(String value) {
        setProfileImage(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value9(Byte value) {
        setEmailLeadSubscription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value10(Byte value) {
        setEmailFollowSubscription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value11(Timestamp value) {
        setCreationDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value12(Timestamp value) {
        setLastUpdatedDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value13(Timestamp value) {
        setLastLoginDate(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord value14(Byte value) {
        setPersonalizationEnabled(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UserRecord values(Integer value1, String value2, String value3, String value4, Boolean value5, String value6, String value7, String value8, Byte value9, Byte value10, Timestamp value11, Timestamp value12, Timestamp value13, Byte value14) {
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
        value14(value14);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached UserRecord
     */
    public UserRecord() {
        super(User.USER);
    }

    /**
     * Create a detached, initialised UserRecord
     */
    public UserRecord(Integer id, String firstName, String lastName, String email, Boolean admin, String las2peerId, String userName, String profileImage, Byte emailLeadSubscription, Byte emailFollowSubscription, Timestamp creationDate, Timestamp lastUpdatedDate, Timestamp lastLoginDate, Byte personalizationEnabled) {
        super(User.USER);

        set(0, id);
        set(1, firstName);
        set(2, lastName);
        set(3, email);
        set(4, admin);
        set(5, las2peerId);
        set(6, userName);
        set(7, profileImage);
        set(8, emailLeadSubscription);
        set(9, emailFollowSubscription);
        set(10, creationDate);
        set(11, lastUpdatedDate);
        set(12, lastLoginDate);
        set(13, personalizationEnabled);
    }
}
