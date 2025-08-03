package com.chatty.repository;

import com.chatty.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    //(one-direction only)
    List<ChatMessage> findBySenderIdAndReceiverId(String senderId, String receiverId);


    // Bidirectional (A → B or B → A)
    @Query("{ '$or': [ " +
            "{ 'senderId': ?0, 'receiverId': ?1 }, " +
            "{ 'senderId': ?1, 'receiverId': ?0 } " +
            "] }")
    List<ChatMessage> findConversation(String userId1, String userId2);

}
