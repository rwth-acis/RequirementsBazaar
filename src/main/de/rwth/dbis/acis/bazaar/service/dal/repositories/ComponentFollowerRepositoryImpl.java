package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ComponentFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.ComponentFollowerRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.ComponentFollowerTransformator;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.ComponentFollower.COMPONENT_FOLLOWER;

public class ComponentFollowerRepositoryImpl extends RepositoryImpl<ComponentFollower, ComponentFollowerRecord> implements ComponentFollowerRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public ComponentFollowerRepositoryImpl(DSLContext jooq) {
        super(jooq, new ComponentFollowerTransformator());
    }

    @Override
    public void delete(int userId, int componentId) throws BazaarException {
        try {
            jooq.delete(COMPONENT_FOLLOWER)
                    .where(COMPONENT_FOLLOWER.USER_ID.equal(userId).and(COMPONENT_FOLLOWER.COMPONENT_ID.equal(componentId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserAlreadyFollows(int userId, int componentId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(COMPONENT_FOLLOWER)
                    .where(COMPONENT_FOLLOWER.USER_ID.equal(userId).and(COMPONENT_FOLLOWER.COMPONENT_ID.equal(componentId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public CreationStatus addOrUpdate(ComponentFollower componentFollower) throws BazaarException {
        ComponentFollowerRecord record = jooq.selectFrom(COMPONENT_FOLLOWER)
                .where(COMPONENT_FOLLOWER.USER_ID.equal(componentFollower.getUserId()).and(COMPONENT_FOLLOWER.COMPONENT_ID.equal(componentFollower.getComponentId())))
                .fetchOne();

        if (record != null) {
            return CreationStatus.UNCHANGED;
        } else {
            try {
                this.add(componentFollower);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }
}
