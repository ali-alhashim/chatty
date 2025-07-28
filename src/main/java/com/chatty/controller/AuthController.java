package com.chatty.controller;

import com.chatty.model.User;
import com.chatty.repository.UserRepository;
import com.chatty.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class AuthController {

     @Autowired
     UserRepository userRepository;

     @Autowired
     OtpService otpService;

    @GetMapping("/login")
    public String loginPage()
    {
        return "login";
    }

    @PostMapping("/send-otp")
    public String loginSubmit(@RequestParam String email) {
        // Create user if not exists (with email only)
        System.out.println("user with email ["+email+"] try to login");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // Generate and send OTP via email (async ideally)
        otpService.generateAndSendOtp(email);


        return "redirect:/otp?email="+email;
    }

    @GetMapping("/otp")
    public String otpPage(@RequestParam String email, Model model)
    {
        model.addAttribute("email", email);
        return "otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otpCode, Model model)
    {

        boolean verified = otpService.verifyOtp(email, otpCode);
        if (!verified) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid OTP code. Please try again.");
            return "otp";
        }

        // OTP is valid - Authenticate user (manually set Authentication)
        User user = userRepository.findByEmail(email).orElseThrow();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return "redirect:/dashboard";
    }


}
