package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import i5.las2peer.logging.L2pLogger;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;

/**
 * Created by martin on 15.02.2016.
 */
public class EmailDispatcher {

    private String smtpServer;
    private String emailFromAddress;
    private BazaarService bazaarService;
    private String frontendBaseURL;

    private final L2pLogger logger = L2pLogger.getInstance(EmailDispatcher.class.getName());

    public EmailDispatcher(BazaarService bazaarService, String smtpServer, String emailFromAddress, String frontendBaseURL) throws Exception {
        this.smtpServer = smtpServer;
        this.emailFromAddress = emailFromAddress;
        this.bazaarService = bazaarService;
        this.frontendBaseURL = frontendBaseURL;
    }

    public void addEmailNotification(Date creationDate, Activity.ActivityAction activityAction,
                                     int dataId, Activity.DataType dataType, int userId, Activity.AdditionalObject additionalObject) {
        DALFacade dalFacade;
        try {
            Email.Builder emailBuilder = new Email.Builder();
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
            // delete the user who created the activity
            Iterator<User> recipientsIterator = recipients.iterator();
            while (recipientsIterator.hasNext()) {
                User recipient = recipientsIterator.next();
                if (recipient.getId() == userId) {
                    recipientsIterator.remove();
                }
            }

            if (!recipients.isEmpty()) {
                // generate mail

                // use activityAction and dataType to generate email text
                String subject = new String();
                String bodyText = new String();
                String objectName;
                String resourcePath = new String();
                String activity = new String();
                if (activityAction == Activity.ActivityAction.CREATE) {
                    activity = Localization.getInstance().getResourceBundle().getString("email.bodyText.created");
                    subject = Localization.getInstance().getResourceBundle().getString("email.subject.new");
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
                    resourcePath = "projects" + "/" + String.valueOf(dataId);
                    subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
                    bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
                    bodyText =  bodyText.concat(" " + activity + " " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + " \"" + objectName + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + project.getDescription() + "\".");
                } else if (dataType == Activity.DataType.CATEGORY) {
                    Category category = additionalObject.getCategory();
                    objectName = category.getName();
                    resourcePath = "projects" + "/" + category.getProjectId() + "/" + "categories" + "/" + String.valueOf(dataId);
                    subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
                    bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
                    bodyText =  bodyText.concat(" " + activity + " " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + " \"" + objectName + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.in"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.project") + " \"" + additionalObject.getProject().getName() + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + category.getDescription() + "\".");
                } else if (dataType == Activity.DataType.REQUIREMENT) {
                    Requirement requirement = additionalObject.getRequirement();
                    objectName = requirement.getName();
                    resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                            requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(dataId);
                    subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
                    bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
                    bodyText =  bodyText.concat(" " + activity + " " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.in"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.category") + " \"" + additionalObject.getCategory().getName() + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.with"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.description") + " \"" + requirement.getDescription() + "\".");
                } else if (dataType == Activity.DataType.COMMENT) {
                    Comment comment = dalFacade.getCommentById(dataId);
                    Requirement requirement = additionalObject.getRequirement();
                    objectName = requirement.getName();
                    resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                            requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
                    subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.comment") + " "
                            + Localization.getInstance().getResourceBundle().getString("email.bodyText.for") + " "
                            + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
                    bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
                    bodyText =  bodyText.concat(" " + activity + " " + Localization.getInstance().getResourceBundle().getString("email.bodyText.comment") + " \"" + comment.getMessage() + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.for"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\".");
                } else if (dataType == Activity.DataType.ATTACHMENT) {
                    Attachment attachment = dalFacade.getAttachmentById(dataId);
                    Requirement requirement = additionalObject.getRequirement();
                    objectName = requirement.getName();
                    resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                            requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
                    subject = subject.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.attachment") + " "
                            + Localization.getInstance().getResourceBundle().getString("email.bodyText.for") + " "
                            + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + ": " + (objectName.length() > 40 ? objectName.substring(0, 39) : objectName));
                    bodyText = bodyText.concat(Localization.getInstance().getResourceBundle().getString("email.bodyText.user") + " " + additionalObject.getUser().getUserName());
                    bodyText =  bodyText.concat(" " + activity + " " + Localization.getInstance().getResourceBundle().getString("email.bodyText.attachment") + " \"" + attachment.getName() + "\"");
                    bodyText =  bodyText.concat("\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.for"));
                    bodyText =  bodyText.concat(" " + Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement") + " \"" + objectName + "\".");
                }
                String dataUrl = frontendBaseURL.concat(resourcePath);

                emailBuilder.receivers(new HashSet<>(recipients));
                emailBuilder.subject(subject);
                emailBuilder.message(bodyText + "\r\n" + Localization.getInstance().getResourceBundle().getString("email.bodyText.see")  + ": " + dataUrl);
                emailBuilder.creationDate(creationDate);
                Email email = emailBuilder.build();

                sendEmailNotification(email);
            }
        } catch (Exception ex) {
            logger.warning(ex.getMessage());
        }
    }

    public void sendEmailNotification(Email mail) {
        try {
            Properties props = System.getProperties();
            Session session = Session.getInstance(props, null);
            Message mailMessage = new MimeMessage(session);
            mailMessage.setFrom(new InternetAddress(emailFromAddress));
            for (User receiver : mail.getReceivers()) {
                if (receiver.getEMail() != null) {
                    mailMessage.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(receiver.getEMail(), false));
                }
            }
            mailMessage.setSubject(mail.getSubject());
            String greeting = Localization.getInstance().getResourceBundle().getString("email.bodyText.greeting");
            String footer1 = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer1");
            String footer2 = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer2");
            String text = greeting;
            text = text.concat("\r\n\r\n");
            text = text.concat(mail.getMessage());
            text = text.concat("\r\n\r\n");
            text = text.concat(footer1);
            text = text.concat("\r\n");
            text = text.concat(footer2);
            mailMessage.setText(text);
            mailMessage.setHeader("X-Mailer", "msgsend");
            mailMessage.setSentDate(mail.getCreationDate());

            Transport.send(mailMessage);
        } catch (MessagingException ex) {
            logger.warning(ex.getMessage());
        }

    }
}
