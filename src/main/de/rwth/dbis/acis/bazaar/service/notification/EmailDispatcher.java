package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.*;
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

    public void sendEmailNotification(Date creationDate, Activity.ActivityAction activityAction,
                                      int dataId, Activity.DataType dataType, int userId) {
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
            // delete the user who created the activity
            recipients.removeIf(recipient -> recipient.getId() == userId);

            if (!recipients.isEmpty()) {
                // generate mail
                Properties props = System.getProperties();
                Session session = Session.getInstance(props, null);
                Message mailMessage = new MimeMessage(session);
                mailMessage.setFrom(new InternetAddress(emailFromAddress));
                for (User recipient : recipients) {
                    if (recipient.getEMail() != null && !recipient.getEMail().isEmpty())
                        mailMessage.addRecipients(Message.RecipientType.BCC,
                                InternetAddress.parse(recipient.getEMail(), false));
                }
                // use activityAction and dataType to generate email text
                String subject = "";
                String bodyText = "";
                String objectName = "";
                String resourcePath = "";
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
                mailMessage.setSubject(subject);
                String greeting = Localization.getInstance().getResourceBundle().getString("email.bodyText.greeting");
                String footer1 = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer1");
                String footer2 = Localization.getInstance().getResourceBundle().getString("email.bodyText.footer2");
                String text = greeting;
                text = text.concat("\r\n\r\n");
                text = text.concat(bodyText);
                text = text.concat("\r\n" + dataUrl);
                text = text.concat("\r\n\r\n");
                text = text.concat(footer1);
                text = text.concat("\r\n");
                text = text.concat(footer2);
                mailMessage.setText(text);
                mailMessage.setHeader("X-Mailer", "msgsend");
                mailMessage.setSentDate(creationDate);

                // send mail
                Transport.send(mailMessage);
            }
        } catch (Exception e) {
            //TODO log
        }
    }
}
