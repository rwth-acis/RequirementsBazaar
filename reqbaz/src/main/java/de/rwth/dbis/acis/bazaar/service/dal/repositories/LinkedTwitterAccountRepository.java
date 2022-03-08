package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import java.util.Optional;

import de.rwth.dbis.acis.bazaar.service.dal.entities.LinkedTwitterAccount;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;

public interface LinkedTwitterAccountRepository extends Repository<LinkedTwitterAccount> {

    /**
     * Returns the Twitter account that is currently linked to the ReqBaz instance. May be an empty optional
     * if no account is linked.
     *
     * @return
     */
    Optional<LinkedTwitterAccount> findCurrentlyLinked() throws BazaarException;
}
