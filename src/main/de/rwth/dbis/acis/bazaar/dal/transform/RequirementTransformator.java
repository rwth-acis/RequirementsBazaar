package de.rwth.dbis.acis.bazaar.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.entities.Requirement;
import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;

import static de.rwth.dbis.acis.bazaar.dal.jooq.tables.Requirements.REQUIREMENTS;


import java.util.HashMap;
import java.util.Map;

/**
 * @author Adam Gavronek <gavronek@dbis.rwth-aachen.de>
 * @since 6/9/2014
 */
public class RequirementTransformator implements Transformator<de.rwth.dbis.acis.bazaar.dal.entities.Requirement,de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.RequirementsRecord> {
    @Override
    public RequirementsRecord createRecord(Requirement entry) {
        RequirementsRecord record = new RequirementsRecord();
        record.setDescription(entry.getDescription());
        record.setTitle(entry.getTitle());

        //TODO connections

        return record;
    }

    @Override
    public Requirement mapToEntity(RequirementsRecord record) {
        return Requirement.getBuilder(record.getTitle())
                .description(record.getDescription())
                .id(record.getId())
                .build();
    }

    @Override
    public Table<RequirementsRecord> getTable() {
        return REQUIREMENTS;
    }

    @Override
    public TableField<RequirementsRecord, Integer> getTableId() {
        return REQUIREMENTS.ID;
    }

    @Override
    public Class<? extends RequirementsRecord> getRecordClass() {
        return RequirementsRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final Requirement entry) {
        return new HashMap<Field, Object>(){{
            put(REQUIREMENTS.DESCRIPTION, entry.getDescription());
            put(REQUIREMENTS.TITLE, entry.getTitle());
        }};
    }
}
