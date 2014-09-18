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

import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachementsRecord;
import org.jooq.*;

import java.sql.Timestamp;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachements.ATTACHEMENTS;
/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/22/2014
 */
public class AttachmentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.service.dal.entities.Attachment, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachementsRecord> {

    @Override
    public AttachementsRecord createRecord(Attachment entity) {
        AttachementsRecord attachementsRecord = new AttachementsRecord();

        attachementsRecord.setId(entity.getId());
        attachementsRecord.setCreationTime(new Timestamp(entity.getCreation_time().getTime()));
        attachementsRecord.setRequirementId(entity.getRequirementId());
        attachementsRecord.setUserId(entity.getCreatorId());
        attachementsRecord.setTitle(entity.getTitle());

        if(entity instanceof File)
            fillFile(attachementsRecord, (File) entity);
        else if(entity instanceof FreeStory)
            fillFreeStory(attachementsRecord, (FreeStory) entity);
        else if(entity instanceof Image)
            fillImage(attachementsRecord, (Image) entity);
        else if(entity instanceof Log)
            fillLog(attachementsRecord, (Log) entity);
        else if(entity instanceof UserStory)
            fillUserStory(attachementsRecord, (UserStory) entity);

        return attachementsRecord;
    }

    private void fillUserStory(AttachementsRecord record, UserStory entity) {
        record.setObject(entity.getObject());
        record.setObjectDesc(entity.getObjectDescription());
        record.setSubject(entity.getSubject());

        record.setDiscriminator(AttachmentType.UserStory.toString());
    }

    private void fillLog(AttachementsRecord record, Log entity) {
        record.setDescription(entity.getDescription());
        record.setFilePath(entity.getFilePath());

        record.setDiscriminator(AttachmentType.Log.toString());
    }

    private void fillImage(AttachementsRecord record, Image entity) {
        record.setDescription(entity.getDescription());
        record.setFilePath(entity.getFilePath());

        record.setDiscriminator(AttachmentType.Image.toString());
    }

    private void fillFreeStory(AttachementsRecord record, FreeStory entity) {
        record.setStory(entity.getStory());

        record.setDiscriminator(AttachmentType.FreeStory.toString());
    }

    private void fillFile(AttachementsRecord record, File entity) {
        record.setDescription(entity.getDescription());
        record.setFilePath(entity.getFilePath());

        record.setDiscriminator(AttachmentType.File.toString());
    }

    @Override
    public Attachment mapToEntity(AttachementsRecord record) {
        Attachment entity = null;
        AttachmentType type = AttachmentType.getEnum(record.getDiscriminator());

        switch (type){
            case UserStory:
                entity = UserStory.getBuilder()
                        .object(record.getObject())
                        .objectDescription(record.getObjectDesc())
                        .subject(record.getSubject())
                        .id(record.getId())
                        .creator(record.getUserId())
                        .requirementId(record.getRequirementId())
                        .title(record.getTitle())
                        .creationTime(new Date(record.getCreationTime().getTime()))
                        .build();
                break;
            case Log:
                entity = Log.getBuilder()
                        .description(record.getDescription())
                        .filePath(record.getFilePath())
                        .id(record.getId())
                        .creator(record.getUserId())
                        .requirementId(record.getRequirementId())
                        .title(record.getTitle())
                        .creationTime(new Date(record.getCreationTime().getTime()))
                        .build();
                break;
            case Image:
                entity = Image.getBuilder()
                        .description(record.getDescription())
                        .filePath(record.getFilePath())
                        .id(record.getId())
                        .creator(record.getUserId())
                        .requirementId(record.getRequirementId())
                        .title(record.getTitle())
                        .creationTime(new Date(record.getCreationTime().getTime()))
                        .build();
                break;
            case FreeStory:
                entity = FreeStory.getBuilder()
                        .story(record.getStory())
                        .id(record.getId())
                        .creator(record.getUserId())
                        .requirementId(record.getRequirementId())
                        .title(record.getTitle())
                        .creationTime(new Date(record.getCreationTime().getTime()))
                        .build();
                break;
            case File:
                entity = File.getBuilder()
                        .description(record.getDescription())
                        .filePath(record.getFilePath())
                        .id(record.getId())
                        .creator(record.getUserId())
                        .requirementId(record.getRequirementId())
                        .title(record.getTitle())
                        .creationTime(new Date(record.getCreationTime().getTime()))
                        .build();
                break;
        }

        return entity;
    }

    @Override
    public Table<AttachementsRecord> getTable() {
        return ATTACHEMENTS;
    }

    @Override
    public TableField<AttachementsRecord, Integer> getTableId() {
        return ATTACHEMENTS.ID;
    }

    @Override
    public Class<? extends AttachementsRecord> getRecordClass() {
        return AttachementsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Attachment entity) {
        return new HashMap<Field, Object>(){{
            put(ATTACHEMENTS.USER_ID, entity.getCreatorId());
            put(ATTACHEMENTS.TITLE, entity.getTitle());
            put(ATTACHEMENTS.REQUIREMENT_ID, entity.getRequirementId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(Pageable.SortDirection sortDirection) {
        switch (sortDirection) {
            case DEFAULT:
                return Arrays.asList(ATTACHEMENTS.CREATION_TIME.desc());
            case ASC:
                return Arrays.asList(ATTACHEMENTS.CREATION_TIME.asc());
            case DESC:
                return Arrays.asList(ATTACHEMENTS.CREATION_TIME.desc());
        }
        return null;
    }

    @Override
    public Collection<? extends Condition> getSearchFields(String likeExpression) throws Exception {
        return Arrays.asList(
                    ATTACHEMENTS.TITLE.likeIgnoreCase(likeExpression)
                .or(ATTACHEMENTS.DESCRIPTION.likeIgnoreCase(likeExpression))
                .or(ATTACHEMENTS.OBJECT_DESC.likeIgnoreCase(likeExpression))
                .or(ATTACHEMENTS.STORY.likeIgnoreCase(likeExpression))
                .or(ATTACHEMENTS.SUBJECT.likeIgnoreCase(likeExpression))
        );
    }
}

enum AttachmentType
{
    UserStory("U"), //UserStory
    Log("L"), //Log
    Image("I"), //Image
    FreeStory("S"), //FreeStory
    File("F");  //File

    private String value;

    AttachmentType(String u) {
        this.value = u;
    }
    public String getValue() {
        return value;
    }
    @Override
    public String toString() {
        return this.getValue();
    }

    public static AttachmentType getEnum(String value) {
        for(AttachmentType v : values())
            if(v.getValue().equalsIgnoreCase(value)) return v;
        throw new IllegalArgumentException();
    }
}


