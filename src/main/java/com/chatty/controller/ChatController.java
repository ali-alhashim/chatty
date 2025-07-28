package com.chatty.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @GetMapping({"/", "dashboard"})
    public String chatDashboard()
    {
        return "chat-dashboard";
    }
}
