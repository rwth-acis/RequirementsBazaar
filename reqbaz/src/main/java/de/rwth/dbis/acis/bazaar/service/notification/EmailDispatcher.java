package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import i5.las2peer.logging.L2pLogger;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by martin on 15.02.2016.
 */
public class EmailDispatcher {

    private final L2pLogger logger = L2pLogger.getInstance(EmailDispatcher.class.getName());
    private final String smtpServer;
    private final String emailFromAddress;
    private final BazaarService bazaarService;
    private final String frontendBaseURL;
    private final String emailSummaryTimePeriodInMinutes;
    private final Map<Integer, List<Email>> notificationSummery;

    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public EmailDispatcher(BazaarService bazaarService, String smtpServer, String emailFromAddress, String frontendBaseURL, String emailSummaryTimePeriodInMinutes) throws Exception {
        this.smtpServer = smtpServer;
        this.emailFromAddress = emailFromAddress;
        this.bazaarService = bazaarService;
        this.frontendBaseURL = frontendBaseURL;
        notificationSummery = new LinkedHashMap<>();
        this.emailSummaryTimePeriodInMinutes = emailSummaryTimePeriodInMinutes;
    }

    public void addEmailNotification(OffsetDateTime creationDate, Activity.ActivityAction activityAction,
                                     int dataId, Activity.DataType dataType, int userId, Activity.AdditionalObject additionalObject) {
        DALFacade dalFacade;
        try {
            dalFacade = bazaarService.getDBConnection();

            List<User> recipients = new ArrayList<>();
            if (dataType.equals(Activity.DataType.PROJECT)) {
                recipients = dalFacade.getRecipientListForProject(dataId);
            } else if (dataType.equals(Activity.DataType.CATEGORY)) {
                recipients = dalFacade.getRecipientListForCategory(dataId);
            } else if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                recipients = dalFacade.getRecipientListForRequirement(dataId);
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                int requirementId = dalFacade.getCommentById(dataId).getRequirementId();
                recipients = dalFacade.getRecipientListForRequirement(requirementId);
            } else if (dataType.equals(Activity.DataType.ATTACHMENT)) {
                int requirementId = dalFacade.getAttachmentById(dataId).getRequirementId();
                recipients = dalFacade.getRecipientListForRequirement(requirementId);
            }

            Email email = generateEmail(recipients, creationDate, activityAction, dataId, dataType, additionalObject);

            Iterator<User> recipientsIterator = email.getRecipients().iterator();
            while (recipientsIterator.hasNext()) {
                User recipient = recipientsIterator.next();
                if (recipient.getId() == userId) {
                    // delete the user who created the activity
                    recipientsIterator.remove();
                    email.removeRecipient(recipient);
                } else if (!notificationSummery.containsKey(recipient.getId()) && !emailSummaryTimePeriodInMinutes.isEmpty()) {
                    // if user has no notificationsummery: create one
                    notificationSummery.put(recipient.getId(), new ArrayList<>());
                } else if (!emailSummaryTimePeriodInMinutes.isEmpty()) {
                    //if user has notificationsummery, add this email to it and remove from recipient
                    notificationSummery.get(recipient.getId()).add(email.toBuilder().recipients(new HashSet<>(Arrays.asList(recipient))).build());
                    recipientsIterator.remove();
                    email.removeRecipient(recipient);
                }
            }

            if (!email.getRecipients().isEmpty()) {
                sendEmail(email);
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    private Email generateEmail(List<User> recipients, OffsetDateTime creationDate, Activity.ActivityAction activityAction,
                                int dataId, Activity.DataType dataType, Activity.AdditionalObject additionalObject) throws Exception {
        DALFacade dalFacade = bazaarService.getDBConnection();
        String subject = "";
        String bodyText = "";
        String objectName;
        String resourcePath = "";
        String activity = "";
        if (activityAction == Activity.ActivityAction.CREATE) {
            activity = Localization.getInstance().getResourceBundle().getString("email.bodyText.created");
            subject = Localization.getInstance().getResourceBundle().getString("email.subject.New");
        } else if (activityAction == Activity.ActivityAction.UPDATE) {
            activity = Localization.getInstance().getResourceBundle().getString("email.bodyText.updated");
            subject = Localization.getInstance().getResourceBundle().getString("email.subject.updated");
        } else if (activityAction == Activity.ActivityAction.REALIZE) {
            activity = Localization.getInstance().getResourceBundle().getString("email.bodyText.realized");
            subject = Localization.getInstance().getResourceBundle().getString("email.subject.realized");
        }

        if (dataType == Activity.DataType.PROJECT) {
            Project project = additionalObject.getProject();
            objectName = project.getName();
            resourcePath = "projects" + "/" + dataId;
            subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
            bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
            bodyText = bodyText.concat(" " + activity);
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + " \"" + objectName + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + project.getDescription() + "\".");
        } else if (dataType == Activity.DataType.CATEGORY) {
            Category category = additionalObject.getCategory();
            objectName = category.getName();
            resourcePath = "projects" + "/" + category.getProjectId() + "/" + "categories" + "/" + dataId;
            subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
            bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
            bodyText = bodyText.concat(" " + activity);
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + " \"" + objectName + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.in"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + " \"" + additionalObject.getProject().getName() + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + category.getDescription() + "\".");
        } else if (dataType == Activity.DataType.REQUIREMENT) {
            Requirement requirement = additionalObject.getRequirement();
            objectName = requirement.getName();
            resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                    requirement.getCategories().get(0) + "/" + "requirements" + "/" + dataId;
            subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
            bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
            bodyText = bodyText.concat(" " + activity);
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.in"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + " \"" + additionalObject.getCategory().getName() + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + requirement.getDescription() + "\".");
        } else if (dataType == Activity.DataType.COMMENT) {
            Comment comment = dalFacade.getCommentById(dataId);
            Requirement requirement = additionalObject.getRequirement();
            objectName = requirement.getName();
            resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                    requirement.getCategories().get(0) + "/" + "requirements" + "/" + requirement.getId();
            subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.comment") + " "
                    + Localization.getInstance().getResourceBundle().getString("email.bodyText.for") + " "
                    + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
            bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
            bodyText = bodyText.concat(" " + activity);
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.comment") + " \"" + comment.getMessage() + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.for"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\".");
        } else if (dataType == Activity.DataType.ATTACHMENT) {
            Attachment attachment = dalFacade.getAttachmentById(dataId);
            Requirement requirement = additionalObject.getRequirement();
            objectName = requirement.getName();
            resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                    requirement.getCategories().get(0) + "/" + "requirements" + "/" + requirement.getId();
            subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.attachment") + " "
                    + Localization.getInstance().getResourceBundle().getString("email.bodyText.for") + " "
                    + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
            bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
            bodyText = bodyText.concat(" " + activity);
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.attachment") + " \"" + attachment.getName() + "\"");
            bodyText = bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.for"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.the"));
            bodyText = bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\".");
        }
        String dataUrl = frontendBaseURL.concat(resourcePath);

        String greeting = Localization.getInstance().getResourceBundle().getString("email.bodyText.greeting");
        String news = Localization.getInstance().getResourceBundle().getString("email.bodyText.news");
        String closing = String.format(Localization.getInstance().getResourceBundle().getString("email.bodyText.nextSummary"), emailSummaryTimePeriodInMinutes) + "\r\n\r\n" +
                Localization.getInstance().getResourceBundle().getString("email.bodyText.bestWishes");
        String footer = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer");

        Email.Builder emailBuilder = Email.builder();
        emailBuilder.recipients(new HashSet<>(recipients));
        emailBuilder.subject(subject);
        emailBuilder.starting(greeting + "\r\n\r\n" + news);
        emailBuilder.message(bodyText + "\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.forDetails") + " " + dataUrl);
        emailBuilder.closing(closing);
        emailBuilder.footer(footer);
        emailBuilder.creationDate(creationDate);
        Email email = emailBuilder.build();

        return email;
    }

    public void emptyNotificationSummery() {
        DALFacade dalFacade;
        try {
            dalFacade = bazaarService.getDBConnection();
            Iterator notificationSummeryIterator = notificationSummery.entrySet().iterator();
            while (notificationSummeryIterator.hasNext()) {
                Map.Entry pair = (Map.Entry) notificationSummeryIterator.next();

                User user = dalFacade.getUserById((Integer) pair.getKey());
                List<Email> notifications = (List<Email>) pair.getValue();
                if (notifications.size() > 0) {

                    String greeting = Localization.getInstance().getResourceBundle().getString("email.bodyText.greeting");
                    String news = Localization.getInstance().getResourceBundle().getString("email.bodyText.news");
                    String closing = String.format(Localization.getInstance().getResourceBundle().getString("email.bodyText.nextSummary"), emailSummaryTimePeriodInMinutes) + "\r\n\r\n" +
                            Localization.getInstance().getResourceBundle().getString("email.bodyText.bestWishes");
                    String footer = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer");
                    String subject = Integer.toString(notifications.size());
                    if (notifications.size() == 1) {
                        subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.subject.singleSummary"));
                    } else {
                        subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.subject.multipleSummary"));
                    }

                    String message = news;
                    Iterator notificationIterator = notifications.iterator();
                    while (notificationIterator.hasNext()) {
                        Email notification = (Email) notificationIterator.next();
                        message = message.concat("\r\n\r\n" + notification.getMessage());
                        notificationIterator.remove();
                    }

                    Email.Builder emailBuilder = Email.builder();
                    emailBuilder.recipients(new HashSet<>(Arrays.asList(user)));
                    emailBuilder.subject(subject);
                    emailBuilder.starting(greeting);
                    emailBuilder.message(message);
                    emailBuilder.closing(closing);
                    emailBuilder.footer(footer);
                    emailBuilder.creationDate(OffsetDateTime.now());
                    Email summary = emailBuilder.build();
                    sendEmail(summary);

                } else {
                    notificationSummeryIterator.remove();
                }
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    private void sendEmail(Email mail) {
        executorService.execute(() -> {
            try {
                Properties props = System.getProperties();
                Session session = Session.getInstance(props, null);
                Message mailMessage = new MimeMessage(session);
                mailMessage.setFrom(new InternetAddress(emailFromAddress));
                for (User receiver : mail.getRecipients()) {
                    if (receiver.getEMail() != null) {
                        mailMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(receiver.getEMail(), false));
                    }
                }
                mailMessage.setSubject(mail.getSubject());
                String text = mail.getStarting();
                text = text.concat("\r\n\r\n");
                text = text.concat(mail.getMessage());
                text = text.concat("\r\n\r\n");
                text = text.concat(mail.getClosing());
                text = text.concat("\r\n\r\n");
                text = text.concat(mail.getFooter());
                mailMessage.setText(text);
                mailMessage.setHeader("X-Mailer", "msgsend");
                mailMessage.setSentDate(java.sql.Timestamp.valueOf(mail.getCreationDate().toLocalDateTime()));

                Transport.send(mailMessage);

            } catch (Exception ex) {
                logger.warning(ex.getMessage());
            }
        });
    }
}
