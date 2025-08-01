package com.chatty.repository;

import com.chatty.Enum.ContactRequestStatus;
import com.chatty.model.ContactRequest;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRequestRepository extends MongoRepository<ContactRequest, String> {

    Optional<ContactRequest> findBySenderIdAndReceiverIdAndStatus(String senderId, String receiverId, ContactRequestStatus status);

    // Find all pending requests received by a user
    List<ContactRequest> findByReceiverIdAndStatus(String receiverId, ContactRequestStatus status);

    // Find all requests sent by a user
    List<ContactRequest> findBySenderId(String senderId);

    List<ContactRequest> findByReceiverId(String receiverId);



    // Optional: Find all pending requests sent by a user
    List<ContactRequest> findBySenderIdAndStatus(String senderId,  ContactRequestStatus status);
}
