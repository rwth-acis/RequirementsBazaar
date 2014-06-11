package de.rwth.dbis.acis.bazaar.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.entities.Component;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ComponentsRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Components.COMPONENTS;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class ComponentTransformator implements Transformator<de.rwth.dbis.acis.bazaar.dal.entities.Component,de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ComponentsRecord> {
    @Override
    public ComponentsRecord createRecord(Component entry) {
        ComponentsRecord record = new ComponentsRecord();
        record.setDescription(entry.getDescription());
        record.setName(entry.getName());

        return record;
    }

    @Override
    public Component mapToEntity(ComponentsRecord record) {
        return Component.getBuilder(record.getName())
                .description(record.getDescription())
                .id(record.getId())
                .build();
    }

    @Override
    public Table<ComponentsRecord> getTable() {
        return COMPONENTS;
    }

    @Override
    public TableField<ComponentsRecord, Integer> getTableId() {
        return COMPONENTS.ID;
    }

    @Override
    public Class<? extends ComponentsRecord> getRecordClass() {
        return ComponentsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Component entry) {
        return new HashMap<Field, Object>(){{
            put(COMPONENTS.DESCRIPTION, entry.getDescription());
            put(COMPONENTS.NAME, entry.getName());
        }};
    }
}
