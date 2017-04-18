package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.CategoryFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.CategoryFollowerMapRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.CategoryFollowerTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.CATEGORY_FOLLOWER_MAP;

public class CategoryFollowerRepositoryImpl extends RepositoryImpl<CategoryFollower, CategoryFollowerMapRecord> implements CategoryFollowerRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public CategoryFollowerRepositoryImpl(DSLContext jooq) {
        super(jooq, new CategoryFollowerTransformer());
    }

    @Override
    public void delete(int userId, int categoryId) throws BazaarException {
        try {
            jooq.delete(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId).and(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
    }

    @Override
    public boolean hasUserAlreadyFollows(int userId, int categoryId) throws BazaarException {
        int execute = 0;
        try {
            execute = jooq.selectFrom(CATEGORY_FOLLOWER_MAP)
                    .where(CATEGORY_FOLLOWER_MAP.USER_ID.equal(userId).and(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryId)))
                    .execute();
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        }
        return execute > 0;
    }

    @Override
    public CreationStatus addOrUpdate(CategoryFollower categoryFollower) throws BazaarException {
        CategoryFollowerMapRecord record = jooq.selectFrom(CATEGORY_FOLLOWER_MAP)
                .where(CATEGORY_FOLLOWER_MAP.USER_ID.equal(categoryFollower.getUserId()).and(CATEGORY_FOLLOWER_MAP.CATEGORY_ID.equal(categoryFollower.getCategoryId())))
                .fetchOne();

        if (record != null) {
            return CreationStatus.UNCHANGED;
        } else {
            try {
                this.add(categoryFollower);
            } catch (Exception ex) {
                ExceptionHandler.getInstance().convertAndThrowException(ex, ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
            }
            return CreationStatus.CREATED;
        }
    }
}
