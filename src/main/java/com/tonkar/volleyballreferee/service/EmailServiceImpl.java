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
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlCell = email.getElementById("link");

        fillLogo(logoHtmlCell);

        String title = "Your Volleyball Referee account was successfully created";
        titleHtmlCell.appendText(title);

        String content = String.format("Dear %s. You may now sign in with the Android app and the website using your email address and your password.", user.getPseudo());
        contentHtmlCell.appendText(content);

        appendLink(linkHtmlCell, "VISIT WEBSITE", webDomain);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, user.getEmail(), imageAttachments);
    }

    @Override
    public void sendPasswordResetEmail(@Email String userEmail, UUID passwordResetId) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlCell = email.getElementById("link");

        fillLogo(logoHtmlCell);

        String title = "Reset your Volleyball Referee password";
        titleHtmlCell.appendText(title);

        String content = "You requested to reset your password. Please follow this link to continue:";
        contentHtmlCell.appendText(content);

        String passwordResetUrl = String.format("%s/api/v3.2/public/users/password/follow/%s", webDomain, passwordResetId);
        appendLink(linkHtmlCell, "RESET PASSWORD", passwordResetUrl);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, userEmail, imageAttachments);
    }

    @Override
    public void sendPasswordUpdatedNotificationEmail(User user) {
        org.jsoup.nodes.Document email = org.jsoup.Jsoup.parse(interactiveEmailHtmlSkeleton());

        org.jsoup.nodes.Element logoHtmlCell = email.getElementById("logo");
        org.jsoup.nodes.Element titleHtmlCell = email.getElementById("title");
        org.jsoup.nodes.Element contentHtmlCell = email.getElementById("content");
        org.jsoup.nodes.Element linkHtmlCell = email.getElementById("link");

        fillLogo(logoHtmlCell);

        String title = "Your Volleyball Referee password was successfully changed";
        titleHtmlCell.appendText(title);

        String content = "If you did not make this request, please follow this link to reset your password:";
        contentHtmlCell.appendText(content);

        String passwordResetUrl = String.format("%s/password-lost", webDomain);
        appendLink(linkHtmlCell, "RESET PASSWORD", passwordResetUrl);

        Map<String, byte[]> imageAttachments = extractImages(email);

        sendEmail(email.toString(), title, user.getEmail(), imageAttachments);
    }

    private void fillLogo(org.jsoup.nodes.Element parentElement) {
        org.jsoup.nodes.Element htmlImage = new org.jsoup.nodes.Element("img");
        htmlImage.attr("src", "data:image/png;base64,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");
        htmlImage.attr("alt", "image or logo");
        htmlImage.attr("width", "auto");
        htmlImage.attr("height", "80px");
        htmlImage.attr("style", "width: auto; height: 80px;");

        parentElement.appendChild(htmlImage);
    }

    private void appendLink(org.jsoup.nodes.Element parentElement, String text, String href) {
        org.jsoup.nodes.Element anchorElement = new org.jsoup.nodes.Element("a");
        anchorElement.attr("href", href);
        anchorElement.attr("style", String.format(
                "color: %s; background-color: %s; text-decoration: none !important; border-radius: 4px; padding: 16px;",
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
                "          <td id=\"logo\" align=\"center\" style=\"text-align: center; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"title\" align=\"center\" style=\"text-align: center; font-size: 1.7em; font-weight: 500; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"content\" align=\"center\" style=\"text-align: center; font-size: 1em; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"link\" align=\"center\" style=\"text-align: center; font-size: 1.3em; padding: 24px 0px;\"></td>\n" +
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
                "          <td id=\"logo\" align=\"center\" style=\"text-align: center; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"title\" align=\"center\" style=\"text-align: center; font-size: 1.7em; font-weight: 500; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "        <tr>\n" +
                "          <td id=\"content\" align=\"center\" style=\"text-align: center; font-size: 1em; padding: 24px 0px;\"></td>\n" +
                "        </tr>\n" +
                "      </tbody>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>\n";
    }

}
