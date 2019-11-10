package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.validation.constraints.Email;
import java.awt.*;
import java.net.URLConnection;
import java.util.*;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${vbr.web.domain}")
    private String webDomain;

    @Value("${vbr.web.color}")
    private String webColor;

    @Value("${vbr.mail.user}")
    private String mailUser;

    @Value("${vbr.mail.password}")
    private String mailPassword;

    @Override
    public void sendUserCreatedNotificationEmail(User user) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(notificationEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element footerHtmlCell = email.getElementById("footer");

        fillLogo(logoHtmlCell);

        String title = "Your Volleyball Referee account was successfully created";
        titleHtmlCell.appendText(title);

        String content = String.format("Dear %s. You may now sign in to the Android app and to %s using your email address and your password.", user.getPseudo(), webDomain);
        contentHtmlCell.appendText(content);

        String footer = "The content of this email is confidential and intended for the recipient specified in message only. Do not reply to this email. Please do not print this email unless it is necessary. Every unprinted email helps the environment.";
        footerHtmlCell.appendText(footer);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, user.getEmail(), imageAttachments);
    }

    @Override
    public void sendPasswordResetEmail(@Email String userEmail, UUID passwordResetId) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element mainHtmlCell = email.getElementById("main");
        org.jsoup.nodes.Element footerHtmlCell = email.getElementById("footer");

        fillLogo(logoHtmlCell);

        String title = "Reset your Volleyball Referee password";
        titleHtmlCell.appendText(title);

        String content = "You requested to reset your password. Please click on the link below to continue.";
        contentHtmlCell.appendText(content);

        String passwordResetUrl = String.format("%s/api/v3.2/public/users/password/follow/%s", webDomain, passwordResetId);
        appendLink(mainHtmlCell, "RESET PASSWORD", passwordResetUrl);

        String footer = "The content of this email is confidential and intended for the recipient specified in message only. Do not reply to this email. Please do not print this email unless it is necessary. Every unprinted email helps the environment.";
        footerHtmlCell.appendText(footer);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, userEmail, imageAttachments);
    }

    @Override
    public void sendPasswordUpdatedNotificationEmail(User user) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(notificationEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element footerHtmlCell = email.getElementById("footer");

        fillLogo(logoHtmlCell);

        String title = "Your Volleyball Referee password was successfully changed";
        titleHtmlCell.appendText(title);

        String content = String.format("If you did not make this request, please reset your password on %s/password-lost.", webDomain);
        contentHtmlCell.appendText(content);

        String footer = "The content of this email is confidential and intended for the recipient specified in message only. Do not reply to this email. Please do not print this email unless it is necessary. Every unprinted email helps the environment.";
        footerHtmlCell.appendText(footer);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, user.getEmail(), imageAttachments);
    }

    private void fillLogo(org.jsoup.nodes.Element parentElement) {
        org.jsoup.nodes.Element htmlImage = new org.jsoup.nodes.Element("img");
        htmlImage.attr("src", "data:image/png;base64,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");
        htmlImage.attr("alt", "image or logo");
        htmlImage.attr("width", "auto");
        htmlImage.attr("height", "60px");
        htmlImage.attr("style", "width: auto; height: 60px;");

        parentElement.appendChild(htmlImage);
    }

    private void appendLink(org.jsoup.nodes.Element parentElement, String text, String href) {
        org.jsoup.nodes.Element anchorElement = new org.jsoup.nodes.Element("a");
        anchorElement.attr("href", href);
        anchorElement.attr("style", String.format(
                "color: %s; background-color: %s; text-decoration: none currentcolor solid; border-radius: 4px; padding: 16px;",
                getTextColor(webColor), webColor
        ));
        anchorElement.appendText(text);

        parentElement.appendChild(anchorElement);
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

    private Map<String, byte[]> extractImages(org.jsoup.nodes.Document document) {
        Map<String, byte[]> imageAttachments = new HashMap<>();

        for (org.jsoup.nodes.Element element : document.getElementsByTag("img")) {
            String src = element.attr("src");

            String imageType = src.substring(src.indexOf("data:image/") + "data:image/".length(), src.indexOf(";base64,"));
            String base64Image = src.substring(src.indexOf(";base64,") + ";base64,".length());
            byte[] image = Base64.getDecoder().decode(base64Image);
            String contentId = String.format("image-cid-%d.%s", imageAttachments.size(), imageType);
            imageAttachments.put(contentId, image);

            element.attr("src", String.format("cid:%s", contentId));
        }

        return imageAttachments;
    }

    private void sendEmail(String emailContent, String emailSubject, @Email String emailTo, Map<String, byte[]> imageAttachments) {
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
            messageBodyPart.setContent(emailContent, "text/html");
            messageBodyPart.setHeader("Content-Type", "text/html;charset=UTF-8");
            multipart.addBodyPart(messageBodyPart);

            for (Map.Entry<String, byte[]> imageAttachment : imageAttachments.entrySet()) {
                String mimeType = URLConnection.guessContentTypeFromName(imageAttachment.getKey());

                MimeBodyPart filePart = new MimeBodyPart();
                filePart.setHeader("Content-ID", String.format("<%s>", imageAttachment.getKey()));
                filePart.setFileName(imageAttachment.getKey());
                filePart.setDataHandler(new DataHandler(new ByteArrayDataSource(imageAttachment.getValue(), mimeType)));
                multipart.addBodyPart(filePart);
            }

            message.setContent(multipart);

            Transport.send(message);

        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String interactiveEmailHtmlSkeleton() {
        return "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "  </head>\n" +
                "  <body style=\"background-color: #e4e4e4; font-family: sans-serif;\">\n" +
                "    <table width=\"600px\" align=\"center\" style=\"width: 600px; padding: 8px; margin-left: auto; margin-right: auto; background-color: #fff;\">\n" +
                "      <tbody>\n" +
                "        <tr>\n" +
                "          <td id=\"logo\" align=\"center\" style=\"text-align: center; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"title\" align=\"center\" style=\"text-align: center; font-size: 1.3em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"content\" align=\"center\" style=\"text-align: center; font-size: 1em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"main\" align=\"center\" style=\"text-align: center; font-size: 1.3em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"footer\" align=\"center\" style=\"text-align: center; font-size: 0.8em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>\n";
    }

    private String notificationEmailHtmlSkeleton() {
        return "<!doctype html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <link rel=\"stylesheet\" type=\"text/css\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">" +
                "  </head>\n" +
                "  <body style=\"background-color: #e4e4e4; font-family: sans-serif;\">\n" +
                "    <table width=\"600px\" align=\"center\" style=\"width: 600px; padding: 8px; margin-left: auto; margin-right: auto; background-color: #fff;\">\n" +
                "      <tbody>\n" +
                "        <tr>\n" +
                "          <td id=\"logo\" align=\"center\" style=\"text-align: center; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"title\" align=\"center\" style=\"text-align: center; font-size: 1.3em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"content\" align=\"center\" style=\"text-align: center; font-size: 1em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"footer\" align=\"center\" style=\"text-align: center; font-size: 0.8em; padding-left: 0px; padding-right: 0px; padding-top: 16px; padding-bottom: 16px\"></td>\n" +
                "        </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>\n";
    }

}
