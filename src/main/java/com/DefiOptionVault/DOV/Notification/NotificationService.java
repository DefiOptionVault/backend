package com.DefiOptionVault.DOV.Notification;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

public class NotificationService {
    public void sendNotification(String token, String title, String body) throws Exception {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .setToken(token)
                .build();

        FirebaseMessaging.getInstance().send(message);
    }
}