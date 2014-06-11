package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ComponentsRecord;
import de.rwth.dbis.acis.bazaar.dal.transform.ComponentTransformator;
import org.jooq.DSLContext;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ComponentRepositoryImpl extends RepositoryImpl<Component,ComponentsRecord> implements ComponentRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ComponentRepositoryImpl(DSLContext jooq) {
        super(jooq, new ComponentTransformator());
    }
}
