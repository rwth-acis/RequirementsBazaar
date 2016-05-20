/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.service.dal.jooq.tables;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.Keys;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.Reqbaz;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RoleRoleRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
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
		"jOOQ version:3.7.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RoleRole extends TableImpl<RoleRoleRecord> {

	private static final long serialVersionUID = 883198946;

	/**
	 * The reference instance of <code>reqbaz.role_role</code>
	 */
	public static final RoleRole ROLE_ROLE = new RoleRole();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<RoleRoleRecord> getRecordType() {
		return RoleRoleRecord.class;
	}

	/**
	 * The column <code>reqbaz.role_role.Id</code>.
	 */
	public final TableField<RoleRoleRecord, Integer> ID = createField("Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>reqbaz.role_role.Child_Id</code>.
	 */
	public final TableField<RoleRoleRecord, Integer> CHILD_ID = createField("Child_Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * The column <code>reqbaz.role_role.Parent_Id</code>.
	 */
	public final TableField<RoleRoleRecord, Integer> PARENT_ID = createField("Parent_Id", org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

	/**
	 * Create a <code>reqbaz.role_role</code> table reference
	 */
	public RoleRole() {
		this("role_role", null);
	}

	/**
	 * Create an aliased <code>reqbaz.role_role</code> table reference
	 */
	public RoleRole(String alias) {
		this(alias, ROLE_ROLE);
	}

	private RoleRole(String alias, Table<RoleRoleRecord> aliased) {
		this(alias, aliased, null);
	}

	private RoleRole(String alias, Table<RoleRoleRecord> aliased, Field<?>[] parameters) {
		super(alias, Reqbaz.REQBAZ, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Identity<RoleRoleRecord, Integer> getIdentity() {
		return Keys.IDENTITY_ROLE_ROLE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public UniqueKey<RoleRoleRecord> getPrimaryKey() {
		return Keys.KEY_ROLE_ROLE_PRIMARY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<RoleRoleRecord>> getKeys() {
		return Arrays.<UniqueKey<RoleRoleRecord>>asList(Keys.KEY_ROLE_ROLE_PRIMARY);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ForeignKey<RoleRoleRecord, ?>> getReferences() {
		return Arrays.<ForeignKey<RoleRoleRecord, ?>>asList(Keys.ROLE_CHILD, Keys.ROLE_PARENT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public RoleRole as(String alias) {
		return new RoleRole(alias, this);
	}

	/**
	 * Rename this table
	 */
	public RoleRole rename(String name) {
		return new RoleRole(name, null);
	}
}
