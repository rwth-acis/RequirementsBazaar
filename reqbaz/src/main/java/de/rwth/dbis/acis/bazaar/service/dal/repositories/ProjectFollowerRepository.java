package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.ProjectFollower;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.CreationStatus;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface ProjectFollowerRepository extends Repository<ProjectFollower> {
    void delete(int userId, int projectId) throws BazaarException;

    boolean hasUserAlreadyFollows(int userId, int projectId) throws BazaarException;

    CreationStatus addOrUpdate(ProjectFollower projectFollower) throws BazaarException;
}
