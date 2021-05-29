package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.Tag;

import java.util.List;

public interface TagRepository extends Repository<Tag> {

    @Override
    Tag findById(int id) throws Exception;

    List<Tag> findByProjectId(int projectId) throws Exception;
}
