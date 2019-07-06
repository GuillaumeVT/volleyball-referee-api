package com.tonkar.volleyballreferee.service;

import com.tonkar.volleyballreferee.entity.User;

import javax.validation.constraints.Email;
import java.util.UUID;

public interface EmailService {

    void sendUserCreatedNotificationEmail(User user);

    void sendPasswordResetEmail(@Email String userEmail, UUID passwordResetId);

    void sendPasswordUpdatedNotificationEmail(User user);

}
