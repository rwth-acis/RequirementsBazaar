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

package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Attachment;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Project;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Attachments;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Projects;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Requirements;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.Users;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentsRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UsersRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.AttachmentTransformator;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/22/2014
 */
public class AttachmentRepositoryImpl extends RepositoryImpl<Attachment, AttachmentsRecord> implements AttachmentRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public AttachmentRepositoryImpl(DSLContext jooq) {
        super(jooq, new AttachmentTransformator());
    }

    @Override
    public Attachment findById(int id) throws Exception {
        Attachment attachment = null;
        try {
            Users creatorUser = Users.USERS.as("creatorUser");
            Record record = jooq.selectFrom(Attachments.ATTACHMENTS
                    .join(creatorUser).on(creatorUser.ID.equal(Attachments.ATTACHMENTS.USER_ID)))
                    .where(transformator.getTableId().equal(id))
                    .fetchOne();
            AttachmentsRecord attachmentsRecord = record.into(AttachmentsRecord.class);
            attachment = transformator.getEntityFromTableRecord(attachmentsRecord);
            UserTransformator userTransformator = new UserTransformator();
            UsersRecord usersRecord = record.into(creatorUser);
            attachment.setCreator(userTransformator.getEntityFromTableRecord(usersRecord));
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformator.getRecordClass() + " found with id: " + id),
                    ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
        return attachment;
    }

    @Override
    public PaginationResult<Attachment> findAllByRequirementId(int requirementId, Pageable pageable) throws BazaarException {
        PaginationResult<Attachment> result = null;
        List<Attachment> attachments;
        try {
            attachments = new ArrayList<>();
            Users creatorUser = Users.USERS.as("creatorUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(Attachments.ATTACHMENTS)
                    .where(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(Attachments.ATTACHMENTS.fields()).select(creatorUser.fields()).select(idCount)
                    .from(Attachments.ATTACHMENTS)
                    .join(creatorUser).on(creatorUser.ID.equal(Attachments.ATTACHMENTS.USER_ID))
                    .where(Attachments.ATTACHMENTS.REQUIREMENT_ID.equal(requirementId))
                    .orderBy(transformator.getSortFields(pageable.getSortDirection()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record record : queryResults) {
                AttachmentsRecord attachmentsRecord = record.into(AttachmentsRecord.class);
                Attachment entry = transformator.getEntityFromTableRecord(attachmentsRecord);
                UserTransformator userTransformator = new UserTransformator();
                UsersRecord usersRecord = record.into(creatorUser);
                entry.setCreator(userTransformator.getEntityFromTableRecord(usersRecord));
                attachments.add(entry);
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, attachments);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    @Override
    public boolean belongsToPublicProject(int id) throws BazaarException {
        try {
            Integer countOfPublicProjects = jooq.selectCount()
                    .from(transformator.getTable())
                    .join(Requirements.REQUIREMENTS).on(Requirements.REQUIREMENTS.ID.eq(Attachments.ATTACHMENTS.REQUIREMENT_ID))
                    .join(Projects.PROJECTS).on(Projects.PROJECTS.ID.eq(Requirements.REQUIREMENTS.PROJECT_ID))
                    .where(transformator.getTableId().eq(id).and(Projects.PROJECTS.VISIBILITY.eq(Project.ProjectVisibility.PUBLIC.asChar())))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }
}
