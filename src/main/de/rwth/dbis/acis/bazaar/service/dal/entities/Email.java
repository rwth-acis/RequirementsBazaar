package de.rwth.dbis.acis.bazaar.service.dal.entities;


import java.util.Date;
import java.util.Set;

public class Email extends EntityBase {

    private final transient int id;

    private final Set<User> receivers;
    private final String subject;
    private final String message;
    private final Date creationDate;

    protected Email(Builder builder) {
        id = builder.id;
        receivers = builder.receivers;
        subject = builder.subject;
        message = builder.message;
        creationDate = builder.creationDate;
    }

    public int getId() {
        return id;
    }

    public Set<User> getReceivers() {
        return receivers;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public static Activity.Builder getBuilder() {
        return new Activity.Builder();
    }

    public static class Builder {

        private int id;
        private Set<User> receivers;
        private String subject;
        private String message;
        private Date creationDate;

        public Builder receivers(Set<User> receivers) {
            this.receivers = receivers;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder creationDate(Date creationDate) {
            this.creationDate = creationDate;
            return this;
        }

        public Email build() {
            Email created = new Email(this);
            return created;
        }
    }
}
