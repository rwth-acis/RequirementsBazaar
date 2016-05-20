package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ProjectFollowerRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ProjectFollowerTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ProjectFollower.PROJECT_FOLLOWER;

public class ProjectFollowerRepositoryImpl extends RepositoryImpl<ProjectFollower, ProjectFollowerRecord> implements ProjectFollowerRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public ProjectFollowerRepositoryImpl(DSLContext jooq) {
        super(jooq, new ProjectFollowerTransformator());
    }

    @Override
    public void delete(int userId, int projectId) throws BazaarException {
        try {
            jooq.delete(PROJECT_FOLLOWER)
                    .where(PROJECT_FOLLOWER.USER_ID.equal(userId).and(PROJECT_FOLLOWER.PROJECT_ID.equal(projectId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserAlreadyFollows(int userId, int projectId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(PROJECT_FOLLOWER)
                    .where(PROJECT_FOLLOWER.USER_ID.equal(userId).and(PROJECT_FOLLOWER.PROJECT_ID.equal(projectId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public CreationStatus addOrUpdate(ProjectFollower projectFollower) throws BazaarException {
        ProjectFollowerRecord record = jooq.selectFrom(PROJECT_FOLLOWER)
                .where(PROJECT_FOLLOWER.USER_ID.equal(projectFollower.getUserId()).and(PROJECT_FOLLOWER.PROJECT_ID.equal(projectFollower.getProjectId())))
                .fetchOne();

        if (record != null) {
            return CreationStatus.UNCHANGED;
        } else {
            try {
                this.add(projectFollower);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }
}
