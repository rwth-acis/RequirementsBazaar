package de.rwth.dbis.acis.bazaar.service.dal.repositories;
import de.rwth.dbis.acis.bazaar.service.dal.entities.PersonalisationData;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface PersonalisationDataRepository extends Repository<PersonalisationData> {

    PersonalisationData findByKey(int userId, int version, String key) throws BazaarException;
    void insertOrUpdate(PersonalisationData data) throws BazaarException;


}