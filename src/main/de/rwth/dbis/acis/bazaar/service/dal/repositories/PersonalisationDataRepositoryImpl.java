package de.rwth.dbis.acis.bazaar.service.dal.repositories;

import de.rwth.dbis.acis.bazaar.service.dal.entities.PersonalisationData;


import de.rwth.dbis.acis.bazaar.service.dal.jooq.tables.records.PersonalisationDataRecord;
import de.rwth.dbis.acis.bazaar.service.dal.transform.PersonalisationDataTransformer;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.exception.ErrorCode;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionHandler;
import de.rwth.dbis.acis.bazaar.service.exception.ExceptionLocation;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.exception.DataAccessException;


import static de.rwth.dbis.acis.bazaar.service.dal.jooq.Tables.*;

public class PersonalisationDataRepositoryImpl  extends RepositoryImpl<PersonalisationData, PersonalisationDataRecord> implements PersonalisationDataRepository {


    public PersonalisationDataRepositoryImpl(DSLContext jooq) {
        super(jooq, new PersonalisationDataTransformer());
    }


    @Override
    public PersonalisationData findByKey(int userId, int version, String key) throws BazaarException{
        PersonalisationData data = null;
        try {
            Record record = jooq.select().from(PERSONALISATION_DATA)
                    .where(PERSONALISATION_DATA.USER_ID.eq(userId))
                    .and(PERSONALISATION_DATA.VERSION.eq(version))
                    .and(PERSONALISATION_DATA.IDENTIFIER.eq(key))
                    .fetchOne();
            PersonalisationDataRecord personalisationDataRecord = record.into(PersonalisationDataRecord.class);
            data = transformer.getEntityFromTableRecord(personalisationDataRecord);
        } catch (DataAccessException e) {
            ExceptionHandler.getInstance().convertAndThrowException(e, ExceptionLocation.REPOSITORY, ErrorCode.UNKNOWN);
        } catch (NullPointerException e) {
            ExceptionHandler.getInstance().convertAndThrowException(
                    new Exception("No " + transformer.getRecordClass() + " found with userId: " + userId + " version: "+version + " key: "+key),
                    ExceptionLocation.REPOSITORY, ErrorCode.NOT_FOUND);
        }
        return data;
    }
    @Override
    public void insertOrUpdate(PersonalisationData data) throws BazaarException{
         jooq.insertInto(PERSONALISATION_DATA, PERSONALISATION_DATA.IDENTIFIER, PERSONALISATION_DATA.USER_ID, PERSONALISATION_DATA.VERSION, PERSONALISATION_DATA.SETTING)
                .values(data.getKey(), data.getUserId(), data.getVersion(), data.getValue())
                .onDuplicateKeyUpdate()
                .set(PERSONALISATION_DATA.SETTING, data.getValue())
                .execute();
    }

}
