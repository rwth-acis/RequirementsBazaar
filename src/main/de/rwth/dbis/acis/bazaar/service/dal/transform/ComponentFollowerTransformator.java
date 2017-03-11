package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ComponentFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentFollowerMapRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.COMPONENT_FOLLOWER_MAP;

public class ComponentFollowerTransformator implements Transformator<ComponentFollower, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentFollowerMapRecord> {
    @Override
    public ComponentFollowerMapRecord createRecord(ComponentFollower entity) {
        ComponentFollowerMapRecord record = new ComponentFollowerMapRecord();
        record.setComponentId(entity.getComponentId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public ComponentFollower getEntityFromTableRecord(ComponentFollowerMapRecord record) {
        return ComponentFollower.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .componentId(record.getComponentId())
                .build();
    }

    @Override
    public Table<ComponentFollowerMapRecord> getTable() {
        return COMPONENT_FOLLOWER_MAP;
    }

    @Override
    public TableField<ComponentFollowerMapRecord, Integer> getTableId() {
        return COMPONENT_FOLLOWER_MAP.ID;
    }

    @Override
    public Class<? extends ComponentFollowerMapRecord> getRecordClass() {
        return ComponentFollowerMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final ComponentFollower entity) {
        return new HashMap<Field, Object>() {{
            put(COMPONENT_FOLLOWER_MAP.COMPONENT_ID, entity.getComponentId());
            put(COMPONENT_FOLLOWER_MAP.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(COMPONENT_FOLLOWER_MAP.ID.asc());
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
