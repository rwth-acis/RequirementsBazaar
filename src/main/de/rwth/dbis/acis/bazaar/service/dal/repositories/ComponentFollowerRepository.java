package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ComponentFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface ComponentFollowerRepository extends Repository<ComponentFollower>{
    void delete(int userId, int componentId) throws BazaarException;

    boolean hasUserAlreadyFollows(int userId, int componentId) throws BazaarException;

    CreationStatus addOrUpdate(ComponentFollower componentFollower) throws BazaarException;
}
