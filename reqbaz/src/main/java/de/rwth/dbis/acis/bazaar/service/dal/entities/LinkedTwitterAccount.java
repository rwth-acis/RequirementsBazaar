package de.rwth.dbis.acis.bazaar.service.dal.entities;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public class LinkedTwitterAccount extends EntityBase {

    private int id;

    private int linkedByUserId;

    private OffsetDateTime creationDate;

    private OffsetDateTime lastUpdatedDate;

    private String twitterUsername;

    private String accessToken;

    private String refreshToken;

    private OffsetDateTime expirationDate;

    public void updateToken(String accessToken, String refreshToken, OffsetDateTime expirationDate) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expirationDate = expirationDate;
        this.lastUpdatedDate = OffsetDateTime.now();
    }

    public boolean isTokenExpired() {
        return OffsetDateTime.now().isAfter(expirationDate);
    }
}
