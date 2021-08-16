package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.FeedbackRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Feedback;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.time.OffsetDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.FEEDBACK;

public class FeedbackTransformer implements Transformer<Feedback, FeedbackRecord> {
    @Override
    public FeedbackRecord createRecord(Feedback entity) {
        FeedbackRecord record = new FeedbackRecord();
        record.setProjectId(entity.getProjectId());
        record.setFeedback(entity.getFeedback());
        record.setEmail(entity.getEMail());
        record.setRequirementId(entity.getRequirementId());
        record.setCreationDate(OffsetDateTime.now());
        return record;
    }

    @Override
    public Feedback getEntityFromTableRecord(FeedbackRecord record) {
        return Feedback.builder()
                .id(record.getId())
                .creationDate(record.getCreationDate())
                .projectId(record.getProjectId())
                .eMail(record.getEmail())
                .feedback(record.getFeedback())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<FeedbackRecord> getTable() {
        return FEEDBACK;
    }

    @Override
    public TableField<FeedbackRecord, Integer> getTableId() {
        return FEEDBACK.ID;
    }

    @Override
    public Class<? extends FeedbackRecord> getRecordClass() {
        return FeedbackRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(Feedback entity) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{

            if (entity.getRequirementId() != null) {
                put(FEEDBACK.REQUIREMENT_ID, entity.getRequirementId());
            }
        }};
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(FEEDBACK.CREATION_DATE.desc());
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
}
