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
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentsRecord;
import org.jooq.*;

import java.sql.Timestamp;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachments.ATTACHMENTS;

public class AttachmentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Attachment, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentsRecord> {

    @Override
    public AttachmentsRecord createRecord(Attachment entity) {
        entity = this.cleanEntry(entity);

        AttachmentsRecord record = new AttachmentsRecord();
        record.setLastupdatedTime(record.getCreationTime());
        record.setRequirementId(entity.getRequirementId());
        record.setUserId(entity.getCreatorId());
        record.setTitle(entity.getTitle());
        record.setDescription(entity.getDescription());
        record.setMimeType(entity.getMimeType());
        record.setIdentifier(entity.getIdentifier());
        record.setFileurl(entity.getFileUrl());
        record.setCreationTime(new Timestamp(Calendar.getInstance().getTime().getTime()));
        record.setLastupdatedTime(record.getCreationTime());
        return record;
    }

    @Override
    public Attachment getEntityFromTableRecord(AttachmentsRecord record) {
        Attachment entity = Attachment.getBuilder()
                .id(record.getId())
                .creator(record.getUserId())
                .requirementId(record.getRequirementId())
                .title(record.getTitle())
                .description(record.getDescription())
                .mimeType(record.getMimeType())
                .identifier(record.getIdentifier())
                .fileUrl(record.getFileurl())
                .creationTime(record.getCreationTime())
                .lastupdatedTime(record.getLastupdatedTime())
                .build();
        return entity;
    }

    @Override
    public Table<AttachmentsRecord> getTable() {
        return ATTACHMENTS;
    }

    @Override
    public TableField<AttachmentsRecord, Integer> getTableId() {
        return ATTACHMENTS.ID;
    }

    @Override
    public Class<? extends AttachmentsRecord> getRecordClass() {
        return AttachmentsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Attachment entity) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(ATTACHMENTS.LASTUPDATED_TIME, new java.sql.Timestamp(Calendar.getInstance().getTime().getTime()));
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(ATTACHMENTS.CREATION_TIME.desc());
            case ASC:
                return Arrays.asList(ATTACHMENTS.CREATION_TIME.asc());
            case DESC:
                return Arrays.asList(ATTACHMENTS.CREATION_TIME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                ATTACHMENTS.TITLE.likeIgnoreCase(likeExpression)
                        .or(ATTACHMENTS.DESCRIPTION.likeIgnoreCase(likeExpression))
        );
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    public Attachment cleanEntry(Attachment attachment) {
        attachment.setTitle(EmojiParser.parseToAliases(attachment.getTitle()));
        attachment.setDescription(EmojiParser.parseToAliases(attachment.getDescription()));
        return attachment;
    }
}


