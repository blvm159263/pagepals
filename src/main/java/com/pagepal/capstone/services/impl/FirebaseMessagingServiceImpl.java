package com.pagepal.capstone.services.impl;

import com.google.firebase.messaging.*;
import com.pagepal.capstone.dtos.firebase.FirebaseMessageData;
import com.pagepal.capstone.services.FirebaseMessagingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FirebaseMessagingServiceImpl implements FirebaseMessagingService {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public String sendNotification(FirebaseMessageData data, String token) throws FirebaseMessagingException {
        Notification notification = Notification
                .builder()
                .setImage(data.getImage())
                .setTitle(data.getSubject())
                .setBody(data.getContent())
                .build();

        Message message = Message
                .builder()
                .setToken(token)
                .setNotification(notification)
                .putAllData(data.getData())
                .build();

        return firebaseMessaging.send(message);
    }

    @Override
    public String sendMultipleNotifications(FirebaseMessageData data, List<String> tokens) throws FirebaseMessagingException {
        Notification notification = Notification
                .builder()
                .setImage(data.getImage())
                .setTitle(data.getSubject())
                .setBody(data.getContent())
                .build();

        MulticastMessage message = MulticastMessage
                .builder()
                .setNotification(notification)
                .putAllData(data.getData())
                .addAllTokens(tokens)
                .build();

        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        return response.getSuccessCount() + " messages were sent successfully";
    }

    @Override
    public String sendNotificationToDevice(String image, String title, String body, Map<String, String> data, String token) {
        try {
            FirebaseMessageData firebaseMessageData = new FirebaseMessageData();
            firebaseMessageData.setImage(image);
            firebaseMessageData.setSubject(title);
            firebaseMessageData.setContent(body);
            firebaseMessageData.setData(data);
            return sendNotification(firebaseMessageData, token);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
