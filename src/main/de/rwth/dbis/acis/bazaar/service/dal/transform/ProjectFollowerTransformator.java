package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectFollowerRecord;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ProjectFollower.PROJECT_FOLLOWER;


public class ProjectFollowerTransformator implements Transformator<ProjectFollower, de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectFollowerRecord> {
    @Override
    public ProjectFollowerRecord createRecord(ProjectFollower entity) {
        ProjectFollowerRecord record = new ProjectFollowerRecord();
        record.setProjectId(entity.getProjectId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public ProjectFollower getEntityFromTableRecord(ProjectFollowerRecord record) {
        return ProjectFollower.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .projectId(record.getProjectId())
                .build();
    }

    @Override
    public Table<ProjectFollowerRecord> getTable() {
        return PROJECT_FOLLOWER;
    }

    @Override
    public TableField<ProjectFollowerRecord, Integer> getTableId() {
        return PROJECT_FOLLOWER.ID;
    }

    @Override
    public Class<? extends ProjectFollowerRecord> getRecordClass() {
        return ProjectFollowerRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final ProjectFollower entity) {
        return new HashMap<Field, Object>() {{
            put(PROJECT_FOLLOWER.PROJECT_ID, entity.getProjectId());
            put(PROJECT_FOLLOWER.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Arrays.asList(PROJECT_FOLLOWER.ID.asc());
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
