/**
 * This class is generated by jOOQ
 */
package de.rwth.dbis.acis.bazaar.dal.jooq;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = { "http://www.jooq.org", "3.3.2" },
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Reqbaz extends org.jooq.impl.SchemaImpl {

	private static final long serialVersionUID = -1714113175;

	/**
	 * The singleton instance of <code>reqbaz</code>
	 */
	public static final Reqbaz REQBAZ = new Reqbaz();

	/**
	 * No further instances allowed
	 */
	private Reqbaz() {
		super("reqbaz");
	}

	@Override
	public final java.util.List<org.jooq.Table<?>> getTables() {
		java.util.List result = new java.util.ArrayList();
		result.addAll(getTables0());
		return result;
	}

	private final java.util.List<org.jooq.Table<?>> getTables0() {
		return java.util.Arrays.<org.jooq.Table<?>>asList(
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Attachements.ATTACHEMENTS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Authorizations.AUTHORIZATIONS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Comments.COMMENTS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Components.COMPONENTS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Developers.DEVELOPERS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Followers.FOLLOWERS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Projects.PROJECTS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Requirements.REQUIREMENTS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Tags.TAGS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Users.USERS,
			de.rwth.dbis.acis.bazaar.dal.jooq.tables.Votes.VOTES);
	}
}
