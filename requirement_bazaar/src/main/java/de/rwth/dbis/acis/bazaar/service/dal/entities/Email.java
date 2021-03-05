package de.rwth.dbis.acis.bazaar.service.dal.entities;


import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Jacksonized
@Builder(builderClassName = "Builder", toBuilder = true)
public class Email extends EntityBase {

    private final transient int id;

    private final Set<User> recipients;
    private final String subject;
    private final String starting;
    private final String message;
    private final String closing;
    private final String footer;
    private final LocalDateTime creationDate;

    public void removeRecipient(User user) {
        recipients.remove(user);
    }

    public static Activity.Builder getBuilder() {
        return new Activity.Builder();
    }
}
