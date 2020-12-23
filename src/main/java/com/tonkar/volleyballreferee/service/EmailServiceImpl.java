package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.validation.constraints.Email;
import java.awt.*;
import java.util.Date;
import java.util.Properties;
import java.util.UUID;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${vbr.web.domain}")
    private String webDomain;

    @Value("${vbr.web.color}")
    private String webColor;

    @Value("${vbr.email.user}")
    private String mailUser;

    @Value("${vbr.email.password}")
    private String mailPassword;

    @Override
    public void sendUserCreatedNotificationEmail(User user) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element titleHtmlDiv = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlDiv = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlDiv = email.getElementById("link");

        String title = "Your Volleyball Referee account was successfully created";
        titleHtmlDiv.appendText(title);

        String content = String.format("Dear %s. You may now sign in with the Android app and the website using your email address and your password.", user.getPseudo());
        contentHtmlDiv.appendText(content);

        appendLink(linkHtmlDiv, "VISIT WEBSITE", webDomain);

        sendEmail(email, title, user.getEmail());
    }

    @Override
    public void sendPasswordResetEmail(@Email String userEmail, UUID passwordResetId) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element titleHtmlDiv = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlDiv = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlDiv = email.getElementById("link");

        String title = "Reset your Volleyball Referee password";
        titleHtmlDiv.appendText(title);

        String content = "You requested to reset your password. Please follow this link to continue:";
        contentHtmlDiv.appendText(content);

        String passwordResetUrl = String.format("%s/api/v3.2/public/users/password/follow/%s", webDomain, passwordResetId);
        appendLink(linkHtmlDiv, "RESET PASSWORD", passwordResetUrl);

        sendEmail(email, title, userEmail);
    }

    @Override
    public void sendPasswordUpdatedNotificationEmail(User user) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element titleHtmlDiv = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlDiv = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlDiv = email.getElementById("link");

        String title = "Your Volleyball Referee password was successfully changed";
        titleHtmlDiv.appendText(title);

        String content = "If you did not make this request, please follow this link to reset your password:";
        contentHtmlDiv.appendText(content);

        String passwordResetUrl = String.format("%s/password-lost", webDomain);
        appendLink(linkHtmlDiv, "RESET PASSWORD", passwordResetUrl);

        sendEmail(email, title, user.getEmail());
    }

    @Override
    public void sendFriendRequestEmail(User senderUser, User receiverUser) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element titleHtmlDiv = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlDiv = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlDiv = email.getElementById("link");

        String title = String.format("The user %s wishes to be your colleague", senderUser.getPseudo());
        titleHtmlDiv.appendText(title);

        String content = String.format("Dear %s. Colleagues are able to select each other to referee games. You may accept or reject this request from the Android app or from the website.", receiverUser.getPseudo());
        contentHtmlDiv.appendText(content);

        appendLink(linkHtmlDiv, "VISIT WEBSITE", webDomain);

        sendEmail(email, title, receiverUser.getEmail());
    }

    @Override
    public void sendAcceptFriendRequestEmail(User acceptingUser, User senderUser) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element titleHtmlDiv = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlDiv = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlDiv = email.getElementById("link");

        String title = String.format("The user %s is now your colleague", acceptingUser.getPseudo());
        titleHtmlDiv.appendText(title);

        String content = String.format("Dear %s. You are now able to select %s to referee your games.", senderUser.getPseudo(), acceptingUser.getPseudo());
        contentHtmlDiv.appendText(content);

        appendLink(linkHtmlDiv, "VISIT WEBSITE", webDomain);

        sendEmail(email, title, senderUser.getEmail());
    }

    private void appendLink(org.jsoup.nodes.Element parentElement, String text, String href) {
        org.jsoup.nodes.Element linkAnchor = new org.jsoup.nodes.Element("a");
        linkAnchor.attr("href", href);
        linkAnchor.attr("style", String.format(
                "display: inline-block; color: %s; background-color: %s; font-size: 1em; font-weight: 700; text-decoration: none !important; border-radius: 4px; padding: 20px;",
                getTextColor(webColor), webColor));
        linkAnchor.appendText(text);

        parentElement.appendChild(linkAnchor);
    }

    private String getTextColor(String backgroundColor) {
        Color color = Color.decode(backgroundColor);
        String textColor;

        double a = 1 - ( 0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue()) / 255;

        if (a < 0.5) {
            textColor = "#000";
        } else {
            textColor = "#fff";
        }

        return textColor;
    }

    private void sendEmail(org.jsoup.nodes.Document email, String emailSubject, @Email String emailTo) {
        try {
            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "465");
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.socketFactory.port", "465");
            prop.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

            Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(mailUser, mailPassword);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUser));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(emailTo));
            message.setSubject(emailSubject);
            message.setSentDate(new Date());

            Multipart multipart = new MimeMultipart();

            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(email.toString(), "text/html");
            messageBodyPart.setHeader("Content-Type", "text/html;charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            log.error(e.getMessage());
        }
    }

    private String interactiveEmailHtmlSkeleton() {
        return """
                <!doctype html>
                <html>
                  <head>
                    <meta charset="utf-8">
                    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
                    <link rel="stylesheet" type="text/css">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                  </head>
                  <body style="font-family: sans-serif;">
                    <div style="width: 600px; margin-left: auto; margin-right: auto;">
                      <div style="text-align: center; margin-left: 30px; margin-right: 30px; padding: 20px 30px 20px 30px;">
                        <img src="https://www.volleyball-referee.com/favicon.ico" alt="logo" style="width: auto; height: 80px">
                      </div>
                      <div style="background-color: #e4e4e4; margin-left: 30px; margin-right: 30px;">
                        <div id="title" style="text-align: center; font-size: 1.6em; font-weight: 700; padding: 20px 30px 20px 30px;"></div>
                        <div id="content" style="text-align: center; font-size: 1em; padding: 20px 30px 20px 30px;"></div>
                        <div id="link" style="text-align: center; font-size: 1em; padding: 20px 30px 20px 30px;"></div>
                      </div>
                    </div>
                  </body>
                </html>
                """;
    }
}
