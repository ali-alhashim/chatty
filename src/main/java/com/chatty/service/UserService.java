package com.chatty.service;


import com.chatty.model.User;
import com.chatty.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean isContact(String requesterId, String userId)
    {
        return userRepository.findById(requesterId)
                .map(requester -> requester.getContactIds().contains(userId))
                .orElse(false);
    }

     public void addContact(String currentUserId, String addUserId)
    {
        // we send Add request to addUserId if accepted both will be contact
        // request show as realtime notification or in add requests
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
