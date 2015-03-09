/*
 *
 *  Copyright (c) 2014, RWTH Aachen University.
 *  For a list of contributors see the AUTHORS file at the top-level directory
 *  of this distribution.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */

package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects.PROJECTS;

import org.jooq.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ProjectTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Project, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord> {
    @Override
    public ProjectsRecord createRecord(Project entry) {
        ProjectsRecord record = new ProjectsRecord();
//        record.setId(entry.getId());
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setLeaderId(entry.getLeaderId());
        record.setVisibility(entry.getVisibility().asChar());
        record.setDefaultComponentsId(entry.getDefaultComponentId());
        return record;
    }

    @Override
    public Project mapToEntity(ProjectsRecord record) {
        return Project.getBuilder(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .leaderId(record.getLeaderId())
                .defaultComponentId(record.getDefaultComponentsId())
                .visibility(Project.ProjectVisibility.getVisibility(record.getVisibility()))
                .build();
    }

    @Override
    public Table<ProjectsRecord> getTable() {
        return PROJECTS;
    }

    @Override
    public TableField<ProjectsRecord, Integer> getTableId() {
        return PROJECTS.ID;
    }

    @Override
    public Class<? extends ProjectsRecord> getRecordClass() {
        return ProjectsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Project entry) {
        return new HashMap<Field, Object>() {{
            put(PROJECTS.DESCRIPTION, entry.getDescription());
            put(PROJECTS.NAME, entry.getName());
            put(PROJECTS.LEADER_ID, entry.getLeaderId());
            put(PROJECTS.DEFAULT_COMPONENTS_ID, entry.getDefaultComponentId());
            put(PROJECTS.VISIBILITY,entry.getVisibility().asChar());
        }};

    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(PROJECTS.NAME.asc());
            case ASC:
                return Arrays.asList(PROJECTS.NAME.asc());
            case DESC:
                return Arrays.asList(PROJECTS.NAME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                PROJECTS.NAME.likeIgnoreCase(likeExpression)
                        .or(PROJECTS.DESCRIPTION.likeIgnoreCase(likeExpression))
        );
    }
}
