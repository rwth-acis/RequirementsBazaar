package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.CategoryFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface CategoryFollowerRepository extends Repository<CategoryFollower>{
    void delete(int userId, int categoryId) throws BazaarException;

    boolean hasUserAlreadyFollows(int userId, int categoryId) throws BazaarException;

    CreationStatus addOrUpdate(CategoryFollower categoryFollower) throws BazaarException;
}
