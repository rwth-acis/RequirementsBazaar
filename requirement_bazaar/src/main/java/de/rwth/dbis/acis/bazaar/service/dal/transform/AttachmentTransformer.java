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
import de.rwth.dbis.acis.bazaar.service.dal.entities.Attachment;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.AttachmentRecord;
import org.jooq.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.ATTACHMENT;

public class AttachmentTransformer implements Transformer<Attachment, AttachmentRecord> {

    @Override
    public AttachmentRecord createRecord(Attachment entity) {
        entity = this.cleanEntry(entity);

        AttachmentRecord record = new AttachmentRecord();
        record.setLastUpdatedDate(record.getCreationDate());
        record.setRequirementId(entity.getRequirementId());
        record.setUserId(entity.getCreator().getId());
        record.setName(entity.getName());
        record.setDescription(entity.getDescription());
        record.setMimeType(entity.getMimeType());
        record.setIdentifier(entity.getIdentifier());
        record.setFileUrl(entity.getFileUrl());
        record.setCreationDate(LocalDateTime.now());
        return record;
    }

    @Override
    public Attachment getEntityFromTableRecord(AttachmentRecord record) {
        return Attachment.getBuilder()
                .id(record.getId())
                .requirementId(record.getRequirementId())
                .name(record.getName())
                .description(record.getDescription())
                .mimeType(record.getMimeType())
                .identifier(record.getIdentifier())
                .fileUrl(record.getFileUrl())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .build();
    }

    @Override
    public Table<AttachmentRecord> getTable() {
        return ATTACHMENT;
    }

    @Override
    public TableField<AttachmentRecord, Integer> getTableId() {
        return ATTACHMENT.ID;
    }

    @Override
    public Class<? extends AttachmentRecord> getRecordClass() {
        return AttachmentRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Attachment entity) {
        HashMap<Field, Object> updateMap = new HashMap<Field, Object>() {{
        }};
        if (!updateMap.isEmpty()) {
            updateMap.put(ATTACHMENT.LAST_UPDATED_DATE, LocalDateTime.now());
        }
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(ATTACHMENT.CREATION_DATE.asc());
        }
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        throw new Exception("Search is not supported!");
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }

    private Attachment cleanEntry(Attachment attachment) {
        if (attachment.getName() != null) {
            attachment.setName(EmojiParser.parseToAliases(attachment.getName()));
        }
        if (attachment.getDescription() != null) {
            attachment.setDescription(EmojiParser.parseToAliases(attachment.getDescription()));
        }
        return attachment;
    }
}


