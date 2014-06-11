package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.Project;
import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Projects.PROJECTS;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectsRecord;
import de.rwth.dbis.acis.bazaar.dal.transform.ProjectTransformator;
import de.rwth.dbis.acis.bazaar.dal.transform.Transformator;
import org.jooq.DSLContext;
import org.jooq.Row1;
import org.jooq.Table;
import org.jooq.TableField;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ProjectRepositoryImpl extends RepositoryImpl<Project,ProjectsRecord> implements ProjectRepository {
    /**
     * @param jooq DSLContext object to initialize JOOQ connection. For more see JOOQ documentation.
     */
    public ProjectRepositoryImpl(DSLContext jooq) {
        super(jooq, new ProjectTransformator());
    }
}
