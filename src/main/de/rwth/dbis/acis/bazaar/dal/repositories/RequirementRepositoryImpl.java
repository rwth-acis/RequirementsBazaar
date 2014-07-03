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

package de.rwth.dbis.acis.bazaar.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.entities.Follower;
import de.rwth.dbis.acis.bazaar.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.dal.entities.RequirementEx;
import de.rwth.dbis.acis.bazaar.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.*;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord;
import de.rwth.dbis.acis.bazaar.dal.transform.RequirementTransformator;
import de.rwth.dbis.acis.bazaar.dal.transform.Transformator;
import org.jooq.DSLContext;
import org.jooq.Record;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Requirements.REQUIREMENTS;

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

    @Override
    public List<Requirement> findAllByProject(int projectId, Pageable pageable) {
        List<Requirement> entries = new ArrayList<Requirement>();

        List<RequirementsRecord> queryResults;
        queryResults = jooq.selectFrom(REQUIREMENTS)
                .where(REQUIREMENTS.PROJECT_ID.equal(projectId))
                .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(transformator.getRecordClass());

        for (RequirementsRecord queryResult: queryResults) {
            Requirement entry = transformator.mapToEntity(queryResult);
            entries.add(entry);
        }

        return entries;
    }

    @Override
    public List<Requirement> findAllByComponent(int componentId, Pageable pageable) {
        List<Requirement> entries = new ArrayList<Requirement>();

        List<RequirementsRecord> queryResults;
        queryResults = jooq.selectFrom(REQUIREMENTS.join(Tags.TAGS).on(Tags.TAGS.REQUIREMENTS_ID.equal(REQUIREMENTS.ID)))
                .where(Tags.TAGS.COMPONENTS_ID.equal(componentId))
                .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetchInto(transformator.getRecordClass());

        for (RequirementsRecord queryResult: queryResults) {
            Requirement entry = transformator.mapToEntity(queryResult);
            entries.add(entry);
        }

        return entries;
    }

    @Override
    public RequirementEx findById(int id) throws Exception {
        Record queryResult =
                jooq.selectFrom(
                        REQUIREMENTS
                                .join(Comments.COMMENTS).on(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                                .join(Attachements.ATTACHEMENTS).on(Attachements.ATTACHEMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))

                                .join(Followers.FOLLOWERS).on(Followers.FOLLOWERS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                                .join(Users.USERS).on(Followers.FOLLOWERS.USER_ID.equal(Users.USERS.ID))

                                .join(Developers.DEVELOPERS).on(Developers.DEVELOPERS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                                .join(Users.USERS).on(Developers.DEVELOPERS.USER_ID.equal(Users.USERS.ID))

                                .join(Users.USERS).on(Users.USERS.ID.equal(REQUIREMENTS.CREATOR_ID))
                )
                .where(transformator.getTableId().equal(id))
                .fetchOne();

        if (queryResult == null) {
            throw new Exception("No "+ transformator.getRecordClass() +" found with id: " + id);
        }

        //TODO
        return null;
    }
}
