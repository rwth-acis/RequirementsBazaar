package de.rwth.dbis.acis.bazaar.service.dal.entities;


import java.util.Date;
import java.util.Set;

public class Email extends EntityBase {

    private final transient int id;

    private final Set<User> recipients;
    private final String subject;
    private final String starting;
    private final String message;
    private final String closing;
    private final String footer;
    private final Date creationDate;

    protected Email(Builder builder) {
        id = builder.id;
        recipients = builder.recipients;
        subject = builder.subject;
        starting = builder.starting;
        message = builder.message;
        closing = builder.closing;
        footer = builder.footer;
        creationDate = builder.creationDate;
    }

    public int getId() {
        return id;
    }

    public Set<User> getRecipients() {
        return recipients;
    }

    public String getSubject() {
        return subject;
    }

    public String getStarting() {
        return starting;
    }

    public String getMessage() {
        return message;
    }

    public String getClosing() {
        return closing;
    }

    public String getFooter() {
        return footer;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void removeRecipient(User user) {
        recipients.remove(user);
    }

    public static Activity.Builder getBuilder() {
        return new Activity.Builder();
    }

    public static class Builder {

        private int id;
        private Set<User> recipients;
        private String subject;
        private String starting;
        private String message;
        private String closing;
        private String footer;
        private Date creationDate;

        public Builder() {

        }

        public Builder(Email email) {
            this.recipients = email.recipients;
            this.subject = email.subject;
            this.starting = email.starting;
            this.message = email.message;
            this.closing = email.closing;
            this.footer = email.footer;
            this.creationDate = email.creationDate;
        }

        public Builder recipients(Set<User> recipients) {
            this.recipients = recipients;
            return this;
        }

        public Builder subject(String subject) {
            this.subject = subject;
            return this;
        }

        public Builder starting(String starting) {
            this.starting = starting;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder closing(String closing) {
            this.closing = closing;
            return this;
        }

        public Builder footer(String footer) {
            this.footer = footer;
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
