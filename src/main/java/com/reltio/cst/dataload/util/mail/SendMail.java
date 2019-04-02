package com.reltio.cst.dataload.util.mail;

import com.reltio.cst.dataload.DataloadConstants;
import com.reltio.cst.dataload.domain.DataloaderInput;
import com.reltio.cst.dataload.impl.LoadJsonToTenant;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * Created by diptam on 8/13/2015.
 */
public class SendMail {

    private static final Logger logger = LogManager.getLogger(SendMail.class.getName());
    private static final String SUBJ_PREFIX = "[Dataload Update] ";
    Session session = null;

    public SendMail() {
        Properties emailProperties = System.getProperties();
        emailProperties.put(DataloadConstants.MAIL_TRANSPORT_PROTOCOL, "smtp");
        emailProperties.put(DataloadConstants.MAIL_SMTP_PORT, 25);
        emailProperties.put(DataloadConstants.MAIL_SMTP_AUTH, "true");
        emailProperties
                .put(DataloadConstants.MAIL_SMTP_STARTTLS_ENABLE, "true");
        emailProperties.put(DataloadConstants.MAIL_SMTP_STARTTLS_REQUIRED,
                "true");
        // Create a Session object to represent a mail session with the
        // specified properties.
        this.session = Session.getDefaultInstance(emailProperties);
    }

    public void send(DataloaderInput dataloaderInput) {

        // Create a message with the specified information.
        MimeMessage msg = new MimeMessage(session);

        try {
            if (dataloaderInput.getEmailsToSendUpdate() != null) {
                msg.setFrom(new InternetAddress(DataloadConstants.FROM_EMAIL,
                        DataloadConstants.FROM_EMAIL_NAME));
                for (String email : dataloaderInput.getEmailsToSendUpdate()) {
                    msg.addRecipients(Message.RecipientType.TO,
                            InternetAddress.parse(email));
                }
                msg.addRecipients(Message.RecipientType.BCC, InternetAddress
                        .parse(DataloadConstants.DEFAULT_BCC_EMAIL));

                String subject = SUBJ_PREFIX + dataloaderInput.getTenantId()
                        + "@" + dataloaderInput.getServerHostName() + "  "
                        + dataloaderInput.getDataType() + " "
                        + dataloaderInput.getStatus();

                msg.setSubject(subject);

                // msg.setContent(formHTML(dataloaderInput),
                // "text/html; charset=utf-8");
                //

                Multipart multipart = new MimeMultipart();
                // creates body part for the message
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(formHTML(dataloaderInput),
                        "text/html; charset=utf-8");
                multipart.addBodyPart(messageBodyPart);
                // creates body part for the attachment
                if (dataloaderInput.getFailedLogFileName() != null
                        && !dataloaderInput.getFailedLogFileName().isEmpty()) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    attachPart.attachFile(dataloaderInput
                            .getFailedLogFileName());

                    multipart.addBodyPart(attachPart);
                }

                msg.setContent(multipart);

                // Create a transport.
                Transport transport = session.getTransport();

                // Send the message.

                // Connect to Amazon SES using the SMTP username and password.
                transport.connect(
                        dataloaderInput.getSmpt_host(),
                        dataloaderInput.getSmtp_username(),
                        dataloaderInput.getSmtp_password());
//				transport.connect(DataloadConstants.MAIL_SMTP_HOST,DataloaderInput.SMTP_USERNAME,DataloadConstants.SMTP_PASSWORD);
                // Send the email.
                transport.sendMessage(msg, msg.getAllRecipients());
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            logger.debug(ex);
        }

    }

    private void formEmailBodyTableValue(StringBuilder sb, String key,
                                         String value) {
        sb.append("<tr><td>").append(key).append("</td><td>").append(value)
                .append("</td></tr>").append(System.lineSeparator());
    }

    protected String constructEmailBody(DataloaderInput dataloaderInput) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table style=\"width:100%;;border:2px solid #F4F4F4;border-collapse:collapse;\" border=\"1\"> <col width=\"60%\"> <col width=\"40%\">");

        formEmailBodyTableValue(sb, "Tenant ID", dataloaderInput.getTenantId());
        formEmailBodyTableValue(sb, "API Server Used",
                dataloaderInput.getServerHostName());
        formEmailBodyTableValue(sb, "Dataload Type",
                dataloaderInput.getDataloadType());
        formEmailBodyTableValue(sb, "Type of Data",
                dataloaderInput.getDataType());
        formEmailBodyTableValue(sb, "Initaited By",
                dataloaderInput.getUsername());
        formEmailBodyTableValue(sb, "Status", dataloaderInput.getStatus());
        formEmailBodyTableValue(sb, "Start Time",
                LoadJsonToTenant.sdf.format(dataloaderInput
                        .getProgramStartTime()));
        formEmailBodyTableValue(sb, "End Time",
                dataloaderInput.getProgramEndTime());
        formEmailBodyTableValue(sb, "", "");

        formEmailBodyTableValue(sb, "Total Records Count",
                dataloaderInput.getTotalRecordsCount() + "");
        formEmailBodyTableValue(sb, "Success Records Count",
                dataloaderInput.getSuccessRecordsCount() + "");
        formEmailBodyTableValue(sb, "Failed Records Count",
                dataloaderInput.getFailedRecordsCount() + "");
        formEmailBodyTableValue(sb, "Total Time Taken (sec)",
                (dataloaderInput.getTotalTimeTaken() / 1000) + "");

        if (dataloaderInput.getFileName() != null) {
            String[] fileName = dataloaderInput.getFileName().split("/", -1);
            formEmailBodyTableValue(sb, "JSON File Name",
                    fileName[fileName.length - 1]);
        }

        formEmailBodyTableValue(sb, "", "");
        if (dataloaderInput.getUserComments() != null
                && !dataloaderInput.getUserComments().isEmpty()) {
            formEmailBodyTableValue(sb, "User Comments",
                    dataloaderInput.getUserComments());
        }
        sb.append("</table><br><br>");
        // System.out.println(sb.toString());
        return sb.toString();
    }

    protected String formHTML(DataloaderInput dataloaderInput) {

        String CONTENT = constructEmailBody(dataloaderInput);
        String BODY = "<html> <body> <div style=\"width: 710px; height:450px; background: #F4F4F4\">     <div style=\"margin-left: 50px; width: 600px;\">         <img src=\"http://s3.amazonaws.com/reltio.ui.static/reltio.png\" style=\"width: 130px; padding-top: 10px;margin-bottom:20px;padding-left: 10px; height: 40px; \"              alt=\"Reltio\"/>         <div style=\"border: 1px solid #9ECAE3; margin-bottom:20px\">     <div style=\"width: 599px; height: 50px; background: #9ECAE3;vertical-align: middle;\">         <div style=\"padding-top: 12px; padding-left: 15px; width:350px; font-family:Arial, sans-serif; font-size:20px;color: #333333; \">            <!-- theme-->    Dataloading Status Update           <!--theme-->         </div>     </div>     <div>         <div style=\"min-height: 75px; width: 598px; background: #FFFFFF; border-bottom:solid #E5ECEF; padding-top: 0px\">             <!--subrecord-->              "
                + CONTENT
                + "             <!-- subrecord-->             </div>      </div> </div>     </div>       <div id=\"footer\" style=\"margin-left: 64px; width: 600px; height: 40px; background: #F4F4F4\">         <div style=\"font-family:Arial, sans-serif; color: #999999; font-size: 12px\">      </div>         <div style=\"font-family:Arial, sans-serif;color: #999999; font-size: 12px\"></div>     </div>  </div> </body> </html> ";
        return BODY;
    }

    protected String decipherText(String text) {
        return reverseString(text.substring(0, text.length() - 35)
                .substring(35));
    }

    protected String reverseString(String args) {
        String original, reverse = "";
        original = args;
        int length = original.length();
        for (int i = length - 1; i >= 0; i--)
            reverse = reverse + original.charAt(i);
        return reverse;
    }

}
