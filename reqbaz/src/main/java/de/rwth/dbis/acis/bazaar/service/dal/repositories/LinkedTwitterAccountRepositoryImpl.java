package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import java.util.Optional;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.LinkedTwitterAccountRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.LinkedTwitterAccount;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.PageInfo;
import de.rwth.dbis.acis.bazaar.service.dal.transform.LinkedTwitterAccountTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import org.jooq.DSLContext;

public class LinkedTwitterAccountRepositoryImpl extends RepositoryImpl<LinkedTwitterAccount, LinkedTwitterAccountRecord> implements LinkedTwitterAccountRepository {

    /**
     * @param jooq DSLContext for JOOQ connection
     */
    public LinkedTwitterAccountRepositoryImpl(DSLContext jooq) {
        super(jooq, new LinkedTwitterAccountTransformer());
    }

    @Override
    public Optional<LinkedTwitterAccount> findCurrentlyLinked() throws BazaarException {
        return findAll(new PageInfo(0, 1)).stream().findFirst();
    }
}
