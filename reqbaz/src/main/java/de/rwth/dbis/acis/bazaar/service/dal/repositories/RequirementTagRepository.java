package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.RequirementTag;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

/**
 * @since 6/22/2014
 */
public interface RequirementTagRepository extends Repository<RequirementTag> {
    CreationStatus addOrUpdate(RequirementTag tag) throws BazaarException;

    void delete(int tagId, int requirementId);
}
