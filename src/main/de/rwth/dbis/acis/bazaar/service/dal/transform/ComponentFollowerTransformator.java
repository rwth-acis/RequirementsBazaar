package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ComponentFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentFollowerRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollower.COMPONENT_FOLLOWER;

public class ComponentFollowerTransformator implements Transformator<ComponentFollower, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentFollowerRecord> {
    @Override
    public ComponentFollowerRecord createRecord(ComponentFollower entity) {
        ComponentFollowerRecord record = new ComponentFollowerRecord();
        record.setComponentId(entity.getComponentId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public ComponentFollower getEntityFromTableRecord(ComponentFollowerRecord record) {
        return ComponentFollower.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .componentId(record.getComponentId())
                .build();
    }

    @Override
    public Table<ComponentFollowerRecord> getTable() {
        return COMPONENT_FOLLOWER;
    }

    @Override
    public TableField<ComponentFollowerRecord, Integer> getTableId() {
        return COMPONENT_FOLLOWER.ID;
    }

    @Override
    public Class<? extends ComponentFollowerRecord> getRecordClass() {
        return ComponentFollowerRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final ComponentFollower entity) {
        return new HashMap<Field, Object>() {{
            put(COMPONENT_FOLLOWER.COMPONENT_ID, entity.getComponentId());
            put(COMPONENT_FOLLOWER.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(COMPONENT_FOLLOWER.ID.asc());
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
