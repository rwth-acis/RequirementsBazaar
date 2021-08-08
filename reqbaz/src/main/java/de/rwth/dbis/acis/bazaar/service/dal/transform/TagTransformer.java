package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.TagRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.time.OffsetDateTime;
import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.TAG;

public class TagTransformer implements Transformer<Tag, TagRecord> {
    @Override
    public TagRecord createRecord(Tag entity) {
        TagRecord record = new TagRecord();
        record.setProjectId(entity.getProjectId());
        record.setName(entity.getName());
        record.setColour(entity.getColour());
        record.setCreationDate(OffsetDateTime.now());
        return record;
    }

    @Override
    public Tag getEntityFromTableRecord(TagRecord record) {
        return Tag.builder()
                .id(record.getId())
                .projectId(record.getProjectId())
                .colour(record.getColour())
                .name(record.getName())
                .build();
    }

    @Override
    public Table<TagRecord> getTable() {
        return TAG;
    }

    @Override
    public TableField<TagRecord, Integer> getTableId() {
        return TAG.ID;
    }

    @Override
    public Class<? extends TagRecord> getRecordClass() {
        return TagRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(Tag entity) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{

            if (entity.getName() != null) {
                put(TAG.NAME, entity.getName());
            }
            if (entity.getColour() != null) {
                put(TAG.COLOUR, entity.getColour());
            }
        }};
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(TAG.NAME.asc());
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
