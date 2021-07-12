package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementTagMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementTag;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.transform.RequirementTagTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_TAG_MAP;

public class RequirementTagRepositoryImpl extends RepositoryImpl<RequirementTag, RequirementTagMapRecord> implements RequirementTagRepository {
    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public RequirementTagRepositoryImpl(DSLContext jooq) {
        super(jooq, new RequirementTagTransformer());
    }

    @Override
    public CreationStatus addOrUpdate(RequirementTag requirementTag) throws BazaarException {
        RequirementTagMapRecord record = jooq.selectFrom(REQUIREMENT_TAG_MAP)
                .where(REQUIREMENT_TAG_MAP.REQUIREMENT_ID.eq(requirementTag.getRequirementId()))
                .and(REQUIREMENT_TAG_MAP.TAG_ID.eq(requirementTag.getTagId()))
                .fetchOne();

        if (record != null) {
            return CreationStatus.UNCHANGED;
        } else {
            try {
                add(requirementTag);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }

    @Override
    public void delete(int tagId, int requirementId) {
        jooq.deleteFrom(REQUIREMENT_TAG_MAP).where(REQUIREMENT_TAG_MAP.TAG_ID.eq(tagId)).and(REQUIREMENT_TAG_MAP.REQUIREMENT_ID.eq(requirementId)).execute();
    }
}
