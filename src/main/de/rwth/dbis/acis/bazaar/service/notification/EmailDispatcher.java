package de.rwth.dbis.acis.bazaar.service.notification;

import de.rwth.dbis.acis.bazaar.service.BazaarService;
import de.rwth.dbis.acis.bazaar.service.dal.DALFacade;
import de.rwth.dbis.acis.bazaar.service.dal.entities.Activity;
import de.rwth.dbis.acis.bazaar.service.dal.entities.User;
import de.rwth.dbis.acis.bazaar.service.exception.BazaarException;
import de.rwth.dbis.acis.bazaar.service.internalization.Localization;
import i5.las2peer.security.Context;
import org.jooq.util.derby.sys.Sys;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by martin on 15.02.2016.
 */
public class EmailDispatcher {

    private String smtpServer;
    private String emailFromAddress;
    private BazaarService bazaarService;
    private String baseURL;

    public EmailDispatcher(BazaarService bazaarService, String smtpServer, String emailFromAddress, String baseURL) throws Exception {
        this.smtpServer = smtpServer;
        this.emailFromAddress = emailFromAddress;
        this.bazaarService = bazaarService;
        this.baseURL = baseURL;
    }

    public void sendEmailNotification(Date creationTime, Activity.ActivityAction activityAction,
                                      int dataId, Activity.DataType dataType, String resourcePath, int userId) {
        DALFacade dalFacade = null;
        try {
            dalFacade = bazaarService.createConnection();

            List<User> recipients = new ArrayList<>();
            if (dataType.equals(Activity.DataType.REQUIREMENT)) {
                recipients = dalFacade.getRecipientListForRequirement(dataId);
            } else if (dataType.equals(Activity.DataType.COMMENT)) {
                int requirementId = dalFacade.getCommentById(dataId).getRequirementId();
                recipients = dalFacade.getRecipientListForRequirement(requirementId);
            } else if (dataType.equals(Activity.DataType.COMPONENT)) {
                recipients = dalFacade.getRecipientListForComponent(dataId);
            } else if (dataType.equals(Activity.DataType.PROJECT)) {
                recipients = dalFacade.getRecipientListForProject(dataId);
            }

            if (!recipients.isEmpty()) {
                // generate mail
                Properties props = System.getProperties();
                Session session = Session.getInstance(props, null);
                Message mailMessage = new MimeMessage(session);
                mailMessage.setFrom(new InternetAddress(emailFromAddress));
                for (int i = 0; i < recipients.size(); i++) {
                    if (recipients.get(i).geteMail() != null && !recipients.get(i).geteMail().isEmpty())
                        mailMessage.addRecipients(Message.RecipientType.BCC,
                                InternetAddress.parse(recipients.get(i).geteMail(), false));
                }
                // use activityAction and dataType to generate email text
                String subject = new String();
                String bodytext = new String();
                if (dataType == Activity.DataType.PROJECT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.project.created");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.project.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.project.updated");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.project.updated");
                    }
                } else if (dataType == Activity.DataType.COMPONENT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.component.created");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.component.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.component.updated");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.component.updated");
                    }
                } else if (dataType == Activity.DataType.REQUIREMENT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.requirement.created");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.requirement.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.requirement.updated");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.requirement.updated");
                    }
                } else if (dataType == Activity.DataType.COMMENT) {
                    if (activityAction == Activity.ActivityAction.CREATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.comment.created");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.comment.created");
                    } else if (activityAction == Activity.ActivityAction.UPDATE) {
                        subject = Localization.getInstance().getResourceBundle().getString("email.subject.comment.updated");
                        bodytext = Localization.getInstance().getResourceBundle().getString("email.bodytext.comment.updated");
                    }
                }
                String dataUrl = baseURL + resourcePath + "/" + String.valueOf(dataId);
                subject = subject.concat(" " + dataUrl);
                mailMessage.setSubject(subject);
                String greeting = Localization.getInstance().getResourceBundle().getString("email.bodytext.greeting");
                String footer1 = Localization.getInstance().getResourceBundle().getString("email.bodytext.footer1");
                String footer2 = Localization.getInstance().getResourceBundle().getString("email.bodytext.footer2");
                String text = greeting;
                text = text.concat("\r\n\r\n");
                text = text.concat(bodytext);
                text = text.concat(" " + dataUrl);
                text = text.concat("\r\n\r\n");
                text = text.concat(footer1);
                text = text.concat("\r\n");
                text = text.concat(footer2);
                mailMessage.setText(text);
                mailMessage.setHeader("X-Mailer", "msgsend");
                mailMessage.setSentDate(creationTime);

                // send mail
                Transport.send(mailMessage);
            }
        } catch (Exception e) {
            Context.logError(this, e.getMessage());
        }
    }
}
