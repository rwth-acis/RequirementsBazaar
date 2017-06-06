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
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.AttachmentRecord;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.UserRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.AttachmentTransformer;
import de.rwth.dbis.acis.bazaar.service.dal.transform.UserTransformer;
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

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.*;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/22/2014
 */
public class AttachmentRepositoryImpl extends RepositoryImpl<Attachment, AttachmentRecord> implements AttachmentRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public AttachmentRepositoryImpl(DSLContext jooq) {
        super(jooq, new AttachmentTransformer());
    }

    @Override
    public Attachment findById(int id) throws Exception {
        Attachment attachment = null;
        try {
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User creatorUser = USER.as("creatorUser");
            Record record = jooq.selectFrom(ATTACHMENT
                    .join(creatorUser).on(creatorUser.ID.equal(ATTACHMENT.USER_ID)))
                    .where(transformer.getTableId().equal(id))
                    .fetchOne();
            AttachmentRecord attachmentRecord = record.into(AttachmentRecord.class);
            attachment = transformer.getEntityFromTableRecord(attachmentRecord);
            UserTransformer userTransformer = new UserTransformer();
            UserRecord userRecord = record.into(creatorUser);
            attachment.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
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
            de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.User creatorUser = USER.as("creatorUser");

            Field<Object> idCount = jooq.selectCount()
                    .from(ATTACHMENT)
                    .where(ATTACHMENT.REQUIREMENT_ID.equal(requirementId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(ATTACHMENT.fields()).select(creatorUser.fields()).select(idCount)
                    .from(ATTACHMENT)
                    .join(creatorUser).on(creatorUser.ID.equal(ATTACHMENT.USER_ID))
                    .where(ATTACHMENT.REQUIREMENT_ID.equal(requirementId))
                    .orderBy(transformer.getSortFields(pageable.getSorts()))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record record : queryResults) {
                AttachmentRecord attachmentRecord = record.into(AttachmentRecord.class);
                Attachment entry = transformer.getEntityFromTableRecord(attachmentRecord);
                UserTransformer userTransformer = new UserTransformer();
                UserRecord userRecord = record.into(creatorUser);
                entry.setCreator(userTransformer.getEntityFromTableRecord(userRecord));
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
                    .from(transformer.getTable())
                    .join(REQUIREMENT).on(REQUIREMENT.ID.eq(ATTACHMENT.REQUIREMENT_ID))
                    .join(PROJECT).on(PROJECT.ID.eq(REQUIREMENT.PROJECT_ID))
                    .where(transformer.getTableId().eq(id).and(PROJECT.VISIBILITY.isTrue()))
                    .fetchOne(0, int.class);

            return (countOfPublicProjects == 1);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return false;
    }
}
