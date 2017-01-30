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

import com.vdurmont.emoji.EmojiParser;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ProjectFollower;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects.PROJECTS;

public class ProjectTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Project, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectsRecord> {

    @Override
    public ProjectsRecord createRecord(Project entry) {
        entry = this.cleanEntry(entry);

        ProjectsRecord record = new ProjectsRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setLeaderId(entry.getLeaderId());
        record.setVisibility(entry.getVisibility().asChar());
        record.setDefaultComponentsId(entry.getDefaultComponentId());
        record.setCreationTime(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        return record;
    }

    @Override
    public Project getEntityFromTableRecord(ProjectsRecord record) {
        return Project.getBuilder(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .leaderId(record.getLeaderId())
                .defaultComponentId(record.getDefaultComponentsId())
                .visibility(Project.ProjectVisibility.getVisibility(record.getVisibility()))
                .creationTime(record.getCreationTime())
                .lastupdatedTime(record.getLastupdatedTime())
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
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entry.getDescription() != null) {
                put(PROJECTS.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(PROJECTS.NAME, entry.getName());
            }
            if (entry.getLeaderId() != 0) {
                put(PROJECTS.LEADER_ID, entry.getLeaderId());
            }
            if (entry.getDefaultComponentId() != null) {
                put(PROJECTS.DEFAULT_COMPONENTS_ID, entry.getDefaultComponentId());
            }
            if (entry.getVisibility() != null) {
                put(PROJECTS.VISIBILITY, entry.getVisibility().asChar());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(PROJECTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(PROJECTS.NAME.asc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            if (sort.getField().equals("name")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(PROJECTS.NAME.asc());
                        break;
                    case DESC:
                        sortFields.add(PROJECTS.NAME.desc());
                        break;
                    default:
                        sortFields.add(PROJECTS.NAME.asc());
                        break;
                }
            } else if (sort.getField().equals("date")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(PROJECTS.CREATION_TIME.asc());
                        break;
                    case DESC:
                        sortFields.add(PROJECTS.CREATION_TIME.desc());
                        break;
                    default:
                        sortFields.add(PROJECTS.CREATION_TIME.desc());
                        break;
                }
            } else if (sort.getField().equals("requirement")) {

                Field<Object> requirementCount = DSL.select(DSL.count())
                        .from(Requirements.REQUIREMENTS)
                        .where(Requirements.REQUIREMENTS.PROJECT_ID.equal(PROJECTS.ID))
                        .asField("requirementCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(requirementCount.asc());
                        break;
                    case DESC:
                        sortFields.add(requirementCount.desc());
                        break;
                    default:
                        sortFields.add(requirementCount.desc());
                        break;
                }
            } else if (sort.getField().equals("follower")) {

                Field<Object> followerCount = DSL.select(DSL.count())
                        .from(ProjectFollower.PROJECT_FOLLOWER)
                        .where(ProjectFollower.PROJECT_FOLLOWER.PROJECT_ID.equal(PROJECTS.ID))
                        .asField("followerCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(followerCount.asc());
                        break;
                    case DESC:
                        sortFields.add(followerCount.desc());
                        break;
                    default:
                        sortFields.add(followerCount.desc());
                        break;
                }
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        return PROJECTS.NAME.likeIgnoreCase("%" + search + "%")
                .or(PROJECTS.DESCRIPTION.likeIgnoreCase("%" + search + "%"));
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    public Project cleanEntry(Project project) {
        if (project.getName() != null) {
            project.setName(EmojiParser.parseToAliases(project.getName()));
        }
        if (project.getDescription() != null) {
            project.setDescription(EmojiParser.parseToAliases(project.getDescription()));
        }
        return project;
    }
}
