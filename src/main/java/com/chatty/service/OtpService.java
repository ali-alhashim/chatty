package com.chatty.service;



import org.springframework.stereotype.Service;


import java.time.LocalDateTime;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private final Map<String, OtpEntry> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();


    // Inner class to hold OTP code + expiry time
    private static class OtpEntry {
        String code;
        LocalDateTime expiresAt;

        OtpEntry(String code, LocalDateTime expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }


    public void generateAndSendOtp(String email) {
        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        otpStorage.put(email, new OtpEntry(code, expiry));
        // TODO: send email (for now just print to console)
        System.out.println("OTP for " + email + ": " + code + " (expires at " + expiry + ")");

    }

    public boolean verifyOtp(String email, String inputCode) {
        OtpEntry entry = otpStorage.get(email);

        if (entry == null) return false;

        // Check code match and expiration
        if (!entry.code.equals(inputCode)) return false;
        if (entry.expiresAt.isBefore(LocalDateTime.now())) {
            otpStorage.remove(email); // Clean up expired entry
            return false;
        }

        // OTP valid, remove after use
        otpStorage.remove(email);
        return true;
    }



}
