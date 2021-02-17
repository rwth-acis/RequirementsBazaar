package de.rwth.dbis.acis.bazaar.service.dal.transform;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.ProjectFollowerMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import java.util.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.PROJECT_FOLLOWER_MAP;


public class ProjectFollowerTransformer implements Transformer<ProjectFollower, ProjectFollowerMapRecord> {
    @Override
    public ProjectFollowerMapRecord createRecord(ProjectFollower entity) {
        ProjectFollowerMapRecord record = new ProjectFollowerMapRecord();
        record.setProjectId(entity.getProjectId());
        record.setUserId(entity.getUserId());
        return record;
    }

    @Override
    public ProjectFollower getEntityFromTableRecord(ProjectFollowerMapRecord record) {
        return ProjectFollower.getBuilder()
                .id(record.getId())
                .userId(record.getUserId())
                .projectId(record.getProjectId())
                .build();
    }

    @Override
    public Table<ProjectFollowerMapRecord> getTable() {
        return PROJECT_FOLLOWER_MAP;
    }

    @Override
    public TableField<ProjectFollowerMapRecord, Integer> getTableId() {
        return PROJECT_FOLLOWER_MAP.ID;
    }

    @Override
    public Class<? extends ProjectFollowerMapRecord> getRecordClass() {
        return ProjectFollowerMapRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(final ProjectFollower entity) {
        return new HashMap<Field, Object>() {{
            put(PROJECT_FOLLOWER_MAP.PROJECT_ID, entity.getProjectId());
            put(PROJECT_FOLLOWER_MAP.USER_ID, entity.getUserId());
        }};
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(PROJECT_FOLLOWER_MAP.ID.asc());
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
