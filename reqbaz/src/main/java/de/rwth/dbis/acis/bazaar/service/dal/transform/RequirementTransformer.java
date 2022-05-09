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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementRecord;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.repositories.RequirementRepositoryImpl;
import i5.las2peer.api.Context;
import i5.las2peer.api.security.Agent;
import i5.las2peer.api.security.AnonymousAgent;
import lombok.RequiredArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;

import java.time.OffsetDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.*;

@RequiredArgsConstructor
public class RequirementTransformer implements Transformer<Requirement, RequirementRecord> {

    private final DALFacade dalFacade;

    @Override
    public RequirementRecord createRecord(Requirement entry) {
        RequirementRecord record = new RequirementRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());
        record.setCreationDate(OffsetDateTime.now());
        record.setCreatorId(entry.getCreator().getId());
        record.setLastUpdatedDate(entry.getLastUpdatedDate());
        record.setLastUpdatingUserId(Optional.ofNullable(entry.getLastUpdatingUser().getId())
                .orElse(RequirementRepositoryImpl.LAST_UPDATING_USER_UNKNOWN));
        record.setProjectId(entry.getProjectId());
        if (entry.getAdditionalProperties() != null) {
            record.setAdditionalProperties(JSONB.jsonb(entry.getAdditionalProperties().toString()));
        }
        return record;
    }

    public Requirement.Builder mapToEntityBuilder(RequirementRecord record) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Requirement.Builder requirementBuilder = Requirement.builder()
                .name(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .realized(record.getRealized())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .projectId(record.getProjectId());

        try {
            return requirementBuilder
                    .additionalProperties(
                            mapper.readTree(record.getAdditionalProperties().data())
                    );
        } catch (Exception e) {
            return requirementBuilder;
        }
    }

    @Override
    public Requirement getEntityFromTableRecord(RequirementRecord record) {
        return mapToEntityBuilder(record)
                .build();
    }

    @Override
    public Table<RequirementRecord> getTable() {
        return REQUIREMENT;
    }

    @Override
    public TableField<RequirementRecord, Integer> getTableId() {
        return REQUIREMENT.ID;
    }

    @Override
    public Class<? extends RequirementRecord> getRecordClass() {
        return RequirementRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(Requirement entry) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{
            if (entry.getDescription() != null) {
                put(REQUIREMENT.DESCRIPTION, entry.getDescription());
            }
            if (entry.getName() != null) {
                put(REQUIREMENT.NAME, entry.getName());
            }
            if (entry.getAdditionalProperties() != null) {
                put(REQUIREMENT.ADDITIONAL_PROPERTIES, entry.getAdditionalProperties());
            }
        }};

        Agent agent = Context.getCurrent().getMainAgent();
        // only set the last_updated_date if it was updated by a user (not, for example, by a webhook)
        if (!updateMap.isEmpty() && !(agent instanceof AnonymousAgent)) {
            String userId = agent.getIdentifier();
            Integer internalUserId;
            try {
                internalUserId = dalFacade.getUserIdByLAS2PeerId(userId);
            } catch (Exception e) {
                throw new RuntimeException("Cannot resolve ID of authenticated user", e);
            }

            updateMap.put(REQUIREMENT.LAST_UPDATED_DATE, OffsetDateTime.now());
            updateMap.put(REQUIREMENT.LAST_UPDATING_USER_ID, internalUserId);
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(RequirementRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
        }
        List<SortField<?>> sortFields = new ArrayList<>();
        for (Pageable.SortField sort : sorts) {
            switch (sort.getField()) {
                case "last_activity":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(RequirementRepositoryImpl.LAST_ACTIVITY.field("last_activity").asc());
                    } else {
                        sortFields.add(RequirementRepositoryImpl.LAST_ACTIVITY.field("last_activity").desc());
                    }
                    break;
                case "date":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(REQUIREMENT.CREATION_DATE.asc());
                    } else {
                        sortFields.add(REQUIREMENT.CREATION_DATE.desc());
                    }
                    break;
                case "name":
                    if (sort.getSortDirection() == Pageable.SortDirection.DESC) {
                        sortFields.add(REQUIREMENT.NAME.desc());
                    } else {
                        sortFields.add(REQUIREMENT.NAME.asc());
                    }
                    break;
                case "vote":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(RequirementRepositoryImpl.VOTE_COUNT.asc());
                    } else {
                        sortFields.add(RequirementRepositoryImpl.VOTE_COUNT.desc());
                    }
                    break;
                case "comment":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(RequirementRepositoryImpl.COMMENT_COUNT.asc());
                    } else {
                        sortFields.add(RequirementRepositoryImpl.COMMENT_COUNT.desc());
                    }
                    break;
                case "follower":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(RequirementRepositoryImpl.FOLLOWER_COUNT.asc());
                    } else {
                        sortFields.add(RequirementRepositoryImpl.FOLLOWER_COUNT.desc());
                    }
                    break;
                case "realized":
                    if (sort.getSortDirection() == Pageable.SortDirection.ASC) {
                        sortFields.add(REQUIREMENT.REALIZED.asc());
                    } else {
                        sortFields.add(REQUIREMENT.REALIZED.desc());
                    }
                    break;
            }
        }
        return sortFields;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        // Catch issues with empty tsvector matching causing nothing to be found
        if (search.equals("")) {
            return REQUIREMENT.NAME.likeIgnoreCase("%");
        }
        return DSL.condition("to_tsvector({0} || {1}) @@ websearch_to_tsquery({2})",
                REQUIREMENT.NAME, REQUIREMENT.DESCRIPTION, search);
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        List<Condition> conditions = new ArrayList<>();
        for (Map.Entry<String, String> filterEntry : filters.entrySet()) {
            if (filterEntry.getKey().equals("realized")) {
                if (filterEntry.getValue().equals("realized")) {
                    conditions.add(REQUIREMENT.REALIZED.isNotNull());
                }
                if (filterEntry.getValue().equals("open")) {
                    conditions.add(REQUIREMENT.REALIZED.isNull());
                }
            } else if (filterEntry.getKey().equals("created")) {
                conditions.add(
                        REQUIREMENT.CREATOR_ID.eq(Integer.parseInt(filterEntry.getValue()))
                );
            } else if (filterEntry.getKey().equals("following")) {
                conditions.add(
                        REQUIREMENT.ID.in(
                                DSL.<Integer>select(REQUIREMENT_FOLLOWER_MAP.REQUIREMENT_ID)
                                        .from(REQUIREMENT_FOLLOWER_MAP)
                                        .where(REQUIREMENT_FOLLOWER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                        )
                );
            } else if (filterEntry.getKey().equals("developing")) {
                conditions.add(
                        REQUIREMENT.ID.in(
                                DSL.<Integer>select(REQUIREMENT_DEVELOPER_MAP.REQUIREMENT_ID)
                                        .from(REQUIREMENT_DEVELOPER_MAP)
                                        .where(REQUIREMENT_DEVELOPER_MAP.USER_ID.eq(Integer.parseInt(filterEntry.getValue())))
                        ).or(
                                REQUIREMENT.LEAD_DEVELOPER_ID.eq(Integer.parseInt(filterEntry.getValue()))
                        )
                );
            } else {
                conditions.add(
                        DSL.falseCondition()
                );
            }
        }
        return conditions;
    }
}
