package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.CategoryFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CategoryFollowerMapRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.CATEGORY_FOLLOWER_MAP;

public class CategoryFollowerTransformer implements Transformer<CategoryFollower, CategoryFollowerMapRecord> {
    @Override
    public CategoryFollowerMapRecord createRecord(CategoryFollower entity) {
        CategoryFollowerMapRecord record = new CategoryFollowerMapRecord();
        record.setCategoryId(entity.getCategoryId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public CategoryFollower getEntityFromTableRecord(CategoryFollowerMapRecord record) {
        return CategoryFollower.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .categoryId(record.getCategoryId())
                .build();
    }

    @Override
    public Table<CategoryFollowerMapRecord> getTable() {
        return CATEGORY_FOLLOWER_MAP;
    }

    @Override
    public TableField<CategoryFollowerMapRecord, Integer> getTableId() {
        return CATEGORY_FOLLOWER_MAP.ID;
    }

    @Override
    public Class<? extends CategoryFollowerMapRecord> getRecordClass() {
        return CategoryFollowerMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final CategoryFollower entity) {
        return new HashMap<Field, Object>() {{
            put(CATEGORY_FOLLOWER_MAP.CATEGORY_ID, entity.getCategoryId());
            put(CATEGORY_FOLLOWER_MAP.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(CATEGORY_FOLLOWER_MAP.ID.asc());
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
