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
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Comments;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Votes;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.RequirementFollower.REQUIREMENT_FOLLOWER;
import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements.REQUIREMENTS;

public class RequirementTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.RequirementsRecord> {
    @Override
    public RequirementsRecord createRecord(Requirement entry) {
        entry = this.cleanEntity(entry);

        RequirementsRecord record = new RequirementsRecord();
        record.setDescription(entry.getDescription());
        record.setTitle(entry.getTitle());
        record.setCreationTime(new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        record.setLeadDeveloperId(entry.getLeadDeveloperId());
        record.setCreatorId(entry.getCreatorId());
        record.setProjectId(entry.getProjectId());

        return record;
    }

    public Requirement.Builder mapToEntityBuilder(RequirementsRecord record) {
        return Requirement.getBuilder(record.getTitle())
                .description(record.getDescription())
                .id(record.getId())
                .realized(record.getRealized())
                .creationTime(record.getCreationTime())
                .lastupdatedTime(record.getLastupdatedTime())
                .leadDeveloperId(record.getLeadDeveloperId())
                .projectId(record.getProjectId())
                .creatorId(record.getCreatorId());
    }

    @Override
    public Requirement getEntityFromTableRecord(RequirementsRecord record) {
        return mapToEntityBuilder(record)
                .build();
    }

    @Override
    public Table<RequirementsRecord> getTable() {
        return REQUIREMENTS;
    }

    @Override
    public TableField<RequirementsRecord, Integer> getTableId() {
        return REQUIREMENTS.ID;
    }

    @Override
    public Class<? extends RequirementsRecord> getRecordClass() {
        return RequirementsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Requirement entry) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
            if (entry.getDescription() != null) {
                put(REQUIREMENTS.DESCRIPTION, entry.getDescription());
            }
            if (entry.getTitle() != null) {
                put(REQUIREMENTS.TITLE, entry.getTitle());
            }
            if (entry.getLeadDeveloperId() != 0) {
                put(REQUIREMENTS.LEAD_DEVELOPER_ID, entry.getLeadDeveloperId());
            }
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(REQUIREMENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(REQUIREMENTS.CREATION_TIME.desc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            if (sort.getField().equals("date")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(REQUIREMENTS.CREATION_TIME.asc());
                        break;
                    case DESC:
                        sortFields.add(REQUIREMENTS.CREATION_TIME.desc());
                        break;
                    default:
                        sortFields.add(REQUIREMENTS.CREATION_TIME.desc());
                        break;
                }
            } else if (sort.getField().equals("title")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(REQUIREMENTS.TITLE.asc());
                        break;
                    case DESC:
                        sortFields.add(REQUIREMENTS.TITLE.desc());
                        break;
                    default:
                        sortFields.add(REQUIREMENTS.TITLE.asc());
                        break;
                }
            } else if (sort.getField().equals("vote")) {

                Field<Object> voteCount = DSL.select(DSL.count(DSL.nullif(Votes.VOTES.IS_UPVOTE, 0)))
                        .from(Votes.VOTES)
                        .where(Votes.VOTES.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                        .asField("voteCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(voteCount.asc());
                        break;
                    case DESC:
                        sortFields.add(voteCount.desc());
                        break;
                    default:
                        sortFields.add(voteCount.desc());
                        break;
                }
            } else if (sort.getField().equals("comment")) {

                Field<Object> commentCount = DSL.select(DSL.count())
                        .from(Comments.COMMENTS)
                        .where(Comments.COMMENTS.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
                        .asField("commentCount");

                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(commentCount.asc());
                        break;
                    case DESC:
                        sortFields.add(commentCount.desc());
                        break;
                    default:
                        sortFields.add(commentCount.desc());
                        break;
                }
            } else if (sort.getField().equals("follower")) {

                Field<Object> followerCount = DSL.select(DSL.count())
                        .from(REQUIREMENT_FOLLOWER)
                        .where(REQUIREMENT_FOLLOWER.REQUIREMENT_ID.equal(REQUIREMENTS.ID))
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
            } else if (sort.getField().equals("realized")) {
                switch (sort.getSortDirection()) {
                    case ASC:
                        sortFields.add(REQUIREMENTS.REALIZED.asc());
                        break;
                    case DESC:
                        sortFields.add(REQUIREMENTS.REALIZED.desc());
                        break;
                    default:
                        sortFields.add(REQUIREMENTS.REALIZED.desc());
                        break;
                }
            }
        }
        return sortFields;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                REQUIREMENTS.TITLE.likeIgnoreCase(likeExpression)
                        .or(REQUIREMENTS.DESCRIPTION.likeIgnoreCase(likeExpression))
        );
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
            if (filterEntry.getKey().equals("realized")) {
                if (filterEntry.getValue().equals("realized")) {
                    conditions.add(REQUIREMENTS.REALIZED.isNotNull());
                }
                if (filterEntry.getValue().equals("open")) {
                    conditions.add(REQUIREMENTS.REALIZED.isNull());
                }
            }
        }
        return conditions;
    }

    public Requirement cleanEntity(Requirement requirement) {
        if (requirement.getTitle() != null) {
            requirement.setTitle(EmojiParser.parseToAliases(requirement.getTitle()));
        }
        if (requirement.getDescription() != null) {
            requirement.setDescription(EmojiParser.parseToAliases(requirement.getDescription()));
        }
        return requirement;
    }
}
