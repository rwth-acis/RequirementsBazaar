package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;

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
                String objectName = new String();
                String resourcePath = new String();

                if (dataType == Activity.DataType.PROJECT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.project.created");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.project.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.project.updated");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.project.updated");
                    }
                    Project project = dalFacade.getProjectById(dataId, 0);
                    objectName = project.getName();
                    resourcePath = "projects" + "/" + String.valueOf(dataId);
                } else if (dataType == Activity.DataType.CATEGORY) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.category.created");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.category.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.category.updated");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.category.updated");
                    }
                    Category category = dalFacade.getCategoryById(dataId, userId);
                    objectName = category.getName();
                    resourcePath = "projects" + "/" + category.getProjectId() + "/" + "categories" + "/" + String.valueOf(dataId);
                } else if (dataType == Activity.DataType.REQUIREMENT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.requirement.created");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.requirement.updated");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement.updated");
                    } else if (activityAction == Activity.ActivityAction.REALIZE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.requirement.realized");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.requirement.realized");
                    }
                    Requirement requirement = dalFacade.getRequirementById(dataId, userId);
                    objectName = requirement.getName();
                    resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                            requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(dataId);
                } else if (dataType == Activity.DataType.COMMENT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.comment.created");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.comment.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.comment.updated");
                        bodyText = Localization.getInstance().getResourceBundle().getString("email.bodyText.comment.updated");
                    }
                    Comment comment = dalFacade.getCommentById(dataId);
                    Requirement requirement = dalFacade.getRequirementById(comment.getRequirementId(), userId);
                    objectName = requirement.getName();
                    resourcePath = "projects" + "/" + requirement.getProjectId() + "/" + "categories" + "/" +
                            requirement.getCategories().get(0).getId() + "/" + "requirements" + "/" + String.valueOf(requirement.getId());
                }
                String dataUrl = frontendBaseURL.concat(resourcePath);
                objectName = objectName.length() > 40 ? objectName.substring(0, 39) : objectName;
                subject = subject.concat(" " + objectName);

                emailBuilder.receivers(new HashSet<>(recipients));
                emailBuilder.subject(subject);
                emailBuilder.message(bodyText + "\r\n" + dataUrl);
                emailBuilder.creationDate(creationDate);
                Email email = emailBuilder.build();

                sendEmailNotification(email);
            }
        } catch (Exception ex) {
            //TODO Log
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
            //TODO Log
        }

    }
}
