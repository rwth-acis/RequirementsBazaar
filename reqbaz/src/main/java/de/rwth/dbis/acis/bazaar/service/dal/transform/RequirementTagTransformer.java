package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementTagMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementTag;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.REQUIREMENT_TAG_MAP;

public class RequirementTagTransformer implements Transformer<RequirementTag, RequirementTagMapRecord> {
    @Override
    public RequirementTagMapRecord createRecord(RequirementTag entity) {
        RequirementTagMapRecord record = new RequirementTagMapRecord();
        record.setRequirementId(entity.getRequirementId());
        record.setTagId(entity.getTagId());
        return record;
    }

    @Override
    public RequirementTag getEntityFromTableRecord(RequirementTagMapRecord record) {
        return RequirementTag.builder()
                .id(record.getId())
                .tagId(record.getTagId())
                .requirementId(record.getRequirementId())
                .build();
    }

    @Override
    public Table<RequirementTagMapRecord> getTable() {
        return REQUIREMENT_TAG_MAP;
    }

    @Override
    public TableField<RequirementTagMapRecord, Integer> getTableId() {
        return REQUIREMENT_TAG_MAP.ID;
    }

    @Override
    public Class<? extends RequirementTagMapRecord> getRecordClass() {
        return RequirementTagMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(RequirementTag entity) {
        return new HashMap<>() {{
            put(REQUIREMENT_TAG_MAP.REQUIREMENT_ID, entity.getRequirementId());
            put(REQUIREMENT_TAG_MAP.TAG_ID, entity.getTagId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(REQUIREMENT_TAG_MAP.ID.asc());
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
