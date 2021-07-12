package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.TagRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;
import de.rwth.dbis.acis.bazaar.service.dal.transform.TagTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import java.util.ArrayList;
import java.util.List;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.TAG;

public class TagRepositoryImpl extends RepositoryImpl<Tag, TagRecord> implements TagRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public TagRepositoryImpl(DSLContext jooq) {
        super(jooq, new TagTransformer());
    }

    @Override
    public List<Tag> findByProjectId(int projectId) throws BazaarException {
        List<Tag> tags = new ArrayList<>();
        try {

            List<TagRecord> queryResults = jooq.selectFrom(TAG)
                    .where(TAG.PROJECT_ID.eq(projectId))
                    .orderBy(TAG.NAME)
                    .fetchInto(transformer.getRecordClass());

            for (TagRecord queryResult : queryResults) {
                Tag entry = transformer.getEntityFromTableRecord(queryResult);
                tags.add(entry);
            }
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }

        return tags;
    }
}
