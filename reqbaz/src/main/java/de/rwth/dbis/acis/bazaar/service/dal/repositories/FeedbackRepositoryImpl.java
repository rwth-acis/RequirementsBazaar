package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.FeedbackRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Feedback;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.dal.transform.FeedbackTransformer;
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

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.FEEDBACK;

public class FeedbackRepositoryImpl extends RepositoryImpl<Feedback, FeedbackRecord> implements FeedbackRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public FeedbackRepositoryImpl(DSLContext jooq) {
        super(jooq, new FeedbackTransformer());
    }


    @Override
    public PaginationResult<Feedback> findAllByProject(int projectId, Pageable pageable) throws BazaarException {
        PaginationResult<Feedback> result = null;
        List<Feedback> feedbacks;
        try {
            feedbacks = new ArrayList<>();

            Field<Object> idCount = jooq.selectCount()
                    .from(FEEDBACK)
                    .where(transformer.getFilterConditions(pageable.getFilters()))
                    //.and(transformer.getSearchCondition(pageable.getSearch()))
                    .and(FEEDBACK.PROJECT_ID.eq(projectId))
                    .asField("idCount");

            List<Record> queryResults = jooq.select(FEEDBACK.fields())
                    .select(idCount)
                    .from(FEEDBACK)
                    .where(FEEDBACK.PROJECT_ID.eq(projectId))
                    .limit(pageable.getPageSize())
                    .offset(pageable.getOffset())
                    .fetch();

            for (Record queryResult : queryResults) {
                FeedbackRecord feedbackRecord = queryResult.into(FEEDBACK);
                Feedback feedback = transformer.getEntityFromTableRecord(feedbackRecord);
                feedbacks.add(findById(feedback.getId())); // TODO: Remove the getId call and create the objects themself here
            }
            int total = queryResults.isEmpty() ? 0 : ((Integer) queryResults.get(0).get("idCount"));
            result = new PaginationResult<>(total, pageable, feedbacks);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (Exception e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return result;
    }

    public Feedback findById(int id) throws Exception {
        Feedback returnFeedback = null;
        try {
            FeedbackRecord record = jooq.selectFrom(FEEDBACK)
                    .where(transformer.getTableId().equal(id))
                    .fetchOne();
            returnFeedback = transformer.getEntityFromTableRecord(record);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformer.getRecordClass() + " found with id: " + id),
                    ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
        return returnFeedback;
    }

}
