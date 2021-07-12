package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Feedback;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PaginationResult;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface FeedbackRepository  extends Repository<Feedback> {

    PaginationResult<Feedback> findAllByProject(int projectId, Pageable pageable) throws BazaarException;

    Feedback findById(int id) throws Exception;
}
