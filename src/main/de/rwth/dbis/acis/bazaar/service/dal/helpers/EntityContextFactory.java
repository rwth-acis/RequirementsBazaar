package de.rwth.dbis.acis.bazaar.service.dal.helpers;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Category;
import de.rwth.dbis.acis.bazaar.service.dal.entities.EntityContext;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CategoryRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementRecord;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.UserRepositoryImpl;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CategoryTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementTransformer;
import org.jooq.Record;
import org.jooq.Require;
import org.jooq.Result;

import java.util.List;

public class EntityContextFactory{
    private static ProjectTransformer projectTransformer;
    private static CategoryTransformer categoryTransformer;
    private static RequirementTransformer requirementTransformer;


    public static EntityContext create(List<String> embed, Record record){
        EntityContext.Builder contextBuilder = EntityContext.getBuilder();
        if(embed != null) {
            for (String entry : embed) {
                if (entry.equalsIgnoreCase("project")) {
                    contextBuilder.project(transformToProject(record));

                } else if (entry.equalsIgnoreCase("category")) {
                    //TODO Need to handle multiple Categories
                    //context.category(transformToCategory(record));

                } else if (entry.equalsIgnoreCase("requirement")) {
                    contextBuilder.requirement(transformToRequirement(record));
                }
            }
        }
        return contextBuilder.build();
    }

    private static Project transformToProject(Record record){
        projectTransformer = (projectTransformer != null) ? projectTransformer : new ProjectTransformer();
        ProjectRecord projectRecord = record.into(ProjectRecord.class);
        return projectTransformer.getEntityFromTableRecord(projectRecord);
    }
    private static Category transformToCategory(Record record){
        categoryTransformer = (categoryTransformer != null) ? categoryTransformer : new CategoryTransformer();
        CategoryRecord categoryRecord = record.into(CategoryRecord.class);
        return categoryTransformer.getEntityFromTableRecord(categoryRecord);
    }
    private static Requirement transformToRequirement(Record record){
        requirementTransformer = (requirementTransformer != null) ? requirementTransformer : new RequirementTransformer();
        RequirementRecord requirementRecord= record.into(RequirementRecord.class);
        return requirementTransformer.getEntityFromTableRecord(requirementRecord);
    }




}
