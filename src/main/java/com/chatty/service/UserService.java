package com.chatty.service;


import com.chatty.Enum.ContactRequestStatus;
import com.chatty.component.ContactWebSocketHandler;
import com.chatty.model.ContactRequest;
import com.chatty.model.User;
import com.chatty.repository.ContactRequestRepository;
import com.chatty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    ContactRequestRepository contactRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactWebSocketHandler webSocketHandler;

    public boolean isContact(String requesterId, String userId)
    {
        return userRepository.findById(requesterId)
                .map(requester -> requester.getContactIds().contains(userId))
                .orElse(false);
    }

    //Add Contact Request
     public void addContact(String senderId, String receiverId)
    {
        // Check if request already exists
        Optional<ContactRequest> existing = contactRequestRepository.findBySenderIdAndReceiverIdAndStatus(senderId, receiverId, ContactRequestStatus.PENDING);
        if (existing.isPresent()) {
            throw new RuntimeException("Contact request already sent.");
        }

        User senderUser = userRepository.findById(senderId).orElse(null);
        User receiverUser = userRepository.findById(receiverId).orElse(null);
        if(senderUser ==null)
        {
            throw new RuntimeException("sender user not exist");
        }
        if(receiverUser ==null)
        {
            throw new RuntimeException("receiver user not exist");
        }

        ContactRequest request = new ContactRequest();
        request.setSenderId(senderId);
        request.setSenderAvatar(senderUser.getAvatar());
        request.setReceiverAvatar(receiverUser.getAvatar());
        request.setSenderName(senderUser.getName());
        request.setReceiverId(receiverId);
        request.setReceiverName(receiverUser.getName());
        request.setStatus(ContactRequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());
        contactRequestRepository.save(request);

        // 🔥 Broadcast to clients
        webSocketHandler.broadcast(
                Map.of(
                        "id", request.getId(),
                        "senderName", request.getSenderName(),
                        "senderAvatar", request.getSenderAvatar(),
                        "receiverName", request.getReceiverName(),
                        "receiverAvatar", request.getReceiverAvatar()
                ),
                "NEW_REQUEST"
        );

    }

    public boolean isAlreadyContact(String userId, String targetUserId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return false;

        return user.getContactIds().contains(targetUserId);
    }

    public List<User> searchUsers(String keyword, String  currentUserId)
    {
        List<User> allMatching = userRepository.findByKeyword(keyword);

        return allMatching.stream()
                .filter(user -> !user.getId().equals(currentUserId))
                .filter(user -> !isAlreadyContact(currentUserId, user.getId())) // implement this
                .collect(Collectors.toList());
    }
}
