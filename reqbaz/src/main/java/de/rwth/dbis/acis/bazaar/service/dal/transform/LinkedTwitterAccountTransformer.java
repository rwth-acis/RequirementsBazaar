package de.rwth.dbis.acis.bazaar.service.dal.transform;

import java.util.*;

import de.rwth.dbis.acis.bazaar.dal.jooq.tables.records.LinkedTwitterAccountRecord;
import de.rwth.dbis.acis.bazaar.service.dal.entities.LinkedTwitterAccount;
import de.rwth.dbis.acis.bazaar.service.dal.helpers.Pageable;
import org.jooq.*;

import static de.rwth.dbis.acis.bazaar.dal.jooq.Tables.LINKED_TWITTER_ACCOUNT;

public class LinkedTwitterAccountTransformer implements Transformer<LinkedTwitterAccount, LinkedTwitterAccountRecord> {

    @Override
    public LinkedTwitterAccountRecord createRecord(LinkedTwitterAccount entity) {
        LinkedTwitterAccountRecord record = new LinkedTwitterAccountRecord();
        record.setId(entity.getId());
        record.setLinkedByUserId(entity.getLinkedByUserId());
        record.setCreationDate(entity.getCreationDate());
        record.setLastUpdatedDate(entity.getLastUpdatedDate());
        record.setTwitterUsername(entity.getTwitterUsername());
        record.setAccessToken(entity.getAccessToken());
        record.setRefreshToken(entity.getRefreshToken());
        record.setExpirationDate(entity.getExpirationDate());
        return record;
    }

    @Override
    public LinkedTwitterAccount getEntityFromTableRecord(LinkedTwitterAccountRecord record) {
        return LinkedTwitterAccount.builder()
                .id(record.getId())
                .linkedByUserId(record.getLinkedByUserId())
                .creationDate(record.getCreationDate())
                .lastUpdatedDate(record.getLastUpdatedDate())
                .twitterUsername(record.getTwitterUsername())
                .accessToken(record.getAccessToken())
                .refreshToken(record.getRefreshToken())
                .expirationDate(record.getExpirationDate())
                .build();
    }

    @Override
    public Table<LinkedTwitterAccountRecord> getTable() {
        return LINKED_TWITTER_ACCOUNT;
    }

    @Override
    public TableField<LinkedTwitterAccountRecord, Integer> getTableId() {
        return LINKED_TWITTER_ACCOUNT.ID;
    }

    @Override
    public Class<? extends LinkedTwitterAccountRecord> getRecordClass() {
        return LinkedTwitterAccountRecord.class;
    }

    @Override
    public Map<Field, Object> getUpdateMap(LinkedTwitterAccount entity) {
        HashMap<Field, Object> updateMap = new HashMap<>() {{

            if (entity.getTwitterUsername() != null) {
                put(LINKED_TWITTER_ACCOUNT.TWITTER_USERNAME, entity.getTwitterUsername());
            }
            if (entity.getAccessToken() != null) {
                put(LINKED_TWITTER_ACCOUNT.ACCESS_TOKEN, entity.getAccessToken());
            }
            if (entity.getRefreshToken() != null) {
                put(LINKED_TWITTER_ACCOUNT.REFRESH_TOKEN, entity.getRefreshToken());
            }
            if (entity.getExpirationDate() != null) {
                put(LINKED_TWITTER_ACCOUNT.EXPIRATION_DATE, entity.getExpirationDate());
            }
            if (entity.getLastUpdatedDate() != null) {
                put(LINKED_TWITTER_ACCOUNT.LAST_UPDATED_DATE, entity.getLastUpdatedDate());
            }
        }};
        return updateMap;
    }

    @Override
    public Collection<? extends SortField<?>> getSortFields(List<Pageable.SortField> sorts) {
        if (sorts.isEmpty()) {
            return Collections.singletonList(LINKED_TWITTER_ACCOUNT.ID.asc());
        }
        return null;
    }

    @Override
    public Condition getSearchCondition(String search) throws Exception {
        throw new Exception("Search is not supported!");
    }

    @Override
    public Collection<? extends Condition> getFilterConditions(Map<String, String> filters) throws Exception {
        return new ArrayList<>();
    }
}
