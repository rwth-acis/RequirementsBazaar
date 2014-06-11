package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.dal.transform.RequirementTransformator;
import de.rwth.dbis.acis.bazaar.dal.transform.Transformator;
import org.jooq.DSLContext;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class RequirementRepositoryImpl extends RepositoryImpl<Requirement,RequirementsRecord> implements RequirementRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public RequirementRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementTransformator());
    }
}
