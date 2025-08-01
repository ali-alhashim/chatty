package com.chatty.model;

import com.chatty.Enum.ContactRequestStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "contact_requests")
public class ContactRequest {
    @Id
    private String id;

    private String senderId;     // current user
    private String receiverId;   // user to be added

    private String senderName;
    private String receiverName;

    private String senderAvatar;
    private String receiverAvatar;

    private ContactRequestStatus status;       // PENDING, ACCEPTED, REJECTED

    private LocalDateTime requestedAt;
    private LocalDateTime respondedAt;

    public String getSenderAvatar() {
        return senderAvatar;
    }

    public String getReceiverAvatar() {
        return receiverAvatar;
    }

    public void setSenderAvatar(String senderAvatar) {
        this.senderAvatar = senderAvatar;
    }

    public void setReceiverAvatar(String receiverAvatar) {
        this.receiverAvatar = receiverAvatar;
    }

    public String getId() {
        return id;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }



    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }



    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public ContactRequestStatus getStatus() {
        return status;
    }

    public void setStatus(ContactRequestStatus status) {
        this.status = status;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }


}

