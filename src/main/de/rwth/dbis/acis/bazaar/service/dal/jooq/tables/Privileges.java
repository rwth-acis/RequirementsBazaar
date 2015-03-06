/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.4.2" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Privileges extends org.jooq.impl.TableImpl<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> {

	private static final long serialVersionUID = 2071824451;

	/**
	 * The singleton instance of <code>reqbaz.privileges</code>
	 */
	public static final de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges PRIVILEGES = new de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges();

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> getRecordType() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord.class;
	}

	/**
	 * The column <code>reqbaz.privileges.Id</code>.
	 */
	public final org.jooq.TableField<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord, java.lang.Integer> ID = createField("Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>reqbaz.privileges.name</code>.
	 */
	public final org.jooq.TableField<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord, java.lang.String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR.length(100).nullable(false), this, "");

	/**
	 * Create a <code>reqbaz.privileges</code> table reference
	 */
	public Privileges() {
		this("privileges", null);
	}

	/**
	 * Create an aliased <code>reqbaz.privileges</code> table reference
	 */
	public Privileges(java.lang.String alias) {
		this(alias, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges.PRIVILEGES);
	}

	private Privileges(java.lang.String alias, org.jooq.Table<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> aliased) {
		this(alias, aliased, null);
	}

	private Privileges(java.lang.String alias, org.jooq.Table<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> aliased, org.jooq.Field<?>[] parameters) {
		super(alias, de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz.REQBAZ, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.Identity<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord, java.lang.Integer> getIdentity() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys.IDENTITY_PRIVILEGES;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public org.jooq.UniqueKey<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord> getPrimaryKey() {
		return de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys.KEY_PRIVILEGES_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public java.util.List<org.jooq.UniqueKey<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PrivilegesRecord>>asList(de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys.KEY_PRIVILEGES_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges as(java.lang.String alias) {
		return new de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges(alias, this);
	}

	/**
	 * Rename this table
	 */
	public de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges rename(java.lang.String name) {
		return new de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Privileges(name, null);
	}
}