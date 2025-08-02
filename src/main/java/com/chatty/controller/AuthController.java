package com.chatty.controller;

import com.chatty.model.User;
import com.chatty.repository.UserRepository;
import com.chatty.service.OtpService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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

        System.out.println("user with email ["+email+"] try to login");
        //check if user use username then get his email
        String theEmail = email.toLowerCase();
        if(email.contains("@"))
        {

            // Create user if not exists (with email only)
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email.toLowerCase());
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser);
                });

        // Generate and send OTP via email (async ideally)


            otpService.generateAndSendOtp(email.toLowerCase());
        }
        else
        {
            // user use
            User user =userRepository.findByName(email).orElse(null);
            if(user ==null)
            {
                return "redirect:/login?&error=NicknameNotExist!";
            }

            otpService.generateAndSendOtp(user.getEmail());

            theEmail = user.getEmail();
        }



        return "redirect:/otp?email="+theEmail;
    }

    @GetMapping("/otp")
    public String otpPage(@RequestParam String email, Model model)
    {
        model.addAttribute("email", email.toLowerCase());
        return "otp";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email, @RequestParam String otpCode, HttpSession session)
    {
        System.out.println("user verify-otp email:"+email +" code:"+otpCode);

        boolean verified = otpService.verifyOtp(email.toLowerCase(), otpCode);
        if (!verified) {

            return "redirect:/otp?email=" + email + "&error=Invalid+OTP+code";
        }

        // OTP is valid - Authenticate user (manually set Authentication)
        System.out.println("OTP is valid - Authenticate user");
        User user = userRepository.findByEmail(email.toLowerCase()).orElseThrow();
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
        session.setAttribute("user", user);

        if(user.getName() ==null || Objects.equals(user.getName(), ""))
        {
            return "redirect:/profile-setup";
        }
        return "redirect:/dashboard";
    }


    @GetMapping("/logout")
    public String logout(HttpSession session)
    {
        session.invalidate();
        return "redirect:/login";
    }


}
