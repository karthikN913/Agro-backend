package com.agrosystem.controller;

import com.agrosystem.model.Message;
import com.agrosystem.repository.MessageRepository;
import com.agrosystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class ChatController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public") // Broadcasts to anyone subscribed. For private chat, we'd use SimpMessagingTemplate.
    public Message sendMessage(@Payload Message chatMessage) {
        if (chatMessage.getSender() != null && chatMessage.getSender().getId() != null &&
            chatMessage.getReceiver() != null && chatMessage.getReceiver().getId() != null) {
               
            userRepository.findById(chatMessage.getSender().getId()).ifPresent(chatMessage::setSender);
            userRepository.findById(chatMessage.getReceiver().getId()).ifPresent(chatMessage::setReceiver);
            
            messageRepository.save(chatMessage);
        }
        return chatMessage;
    }

    @GetMapping("/api/messages/{userId1}/{userId2}")
    @ResponseBody
    public List<Message> getChatHistory(@PathVariable Long userId1, @PathVariable Long userId2) {
        return messageRepository.findChatHistory(userId1, userId2);
    }
}
