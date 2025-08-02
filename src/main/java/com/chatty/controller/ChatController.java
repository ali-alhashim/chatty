package com.chatty.controller;

import com.chatty.Enum.ContactRequestStatus;
import com.chatty.component.ContactWebSocketHandler;
import com.chatty.config.AppConfig;
import com.chatty.config.FileStorageException;
import com.chatty.model.ContactRequest;
import com.chatty.model.User;
import com.chatty.repository.ContactRequestRepository;
import com.chatty.repository.UserRepository;
import com.chatty.service.FileStorageService;
import com.chatty.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    FileStorageService fileStorageService;


    @Autowired
    UserService userService;

    @Autowired
    ContactRequestRepository contactRequestRepository;

    @Autowired
    AppConfig appConfig;

    @Autowired
    private ContactWebSocketHandler webSocketHandler;


    @Value("${app.uploads-base-dir}")
    private String appUploadsBaseDir;

    @GetMapping({"/", "/dashboard"})
    public String chatDashboard(Model model, Principal principal, @RequestParam(required = false) String contactId, RedirectAttributes redirectAttributes)
    {
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        List<User> contacts = userRepository.findAllById(currentUser.getContactIds());

        List<ContactRequest> pendingRequests = contactRequestRepository.findByReceiverIdAndStatus(currentUser.getId(), ContactRequestStatus.PENDING);
        List<ContactRequest> pendingResponses= contactRequestRepository.findBySenderIdAndStatus(currentUser.getId(), ContactRequestStatus.PENDING);

        if(contactId !=null)
        {
            model.addAttribute("contactId", contactId);
            //So user select a contact to chat with contactId load chat history
            //and send contact avatar and name
            User selectedContact = userRepository.findById(contactId).orElse(null);
            if(selectedContact ==null)
            {
                redirectAttributes.addFlashAttribute("error", "you select contact not exist");
                return "redirect:/dashboard";
            }

            model.addAttribute("selectedContact", selectedContact);
        }

        model.addAttribute("baseUrlWS", appConfig.getBaseUrl_ws());
        model.addAttribute("pendingResponses", pendingResponses);
        model.addAttribute("pendingRequests",pendingRequests);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("contacts", contacts);
        return "chat-dashboard";
    }

    @GetMapping("/profile-setup")
    public String showProfileSetup(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile-setup";
    }




    @PostMapping("/profile-setup")
    public String handleProfileSetup(@RequestParam String name,
                                     @RequestParam("avatar") MultipartFile avatarFile,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (userRepository.existsByName(name.toLowerCase())) {
            redirectAttributes.addFlashAttribute("error", "The name already exists, please select another one.");
            return "redirect:/profile-setup";
        }


        user.setName(name.toLowerCase());

        try {
            if (!avatarFile.isEmpty()) {
                String avatarPath = fileStorageService.storeAvatar(avatarFile, user.getId());
                user.setAvatar(avatarPath);
            }
            userRepository.save(user);
            session.setAttribute("user", user);
            return "redirect:/dashboard";
        } catch (FileStorageException e) { // Custom exception for file-related issues
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile-setup";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred.");
            return "redirect:/profile-setup";
        }
    }




    @GetMapping("/{userId}/avatar/{filename}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String userId,
                                            @PathVariable String filename,
                                            HttpSession session) throws IOException {






        Path uploadsDir = Paths.get(appUploadsBaseDir).toAbsolutePath().normalize();
        Path avatarPath = uploadsDir.resolve(userId).resolve("avatar").resolve(filename).normalize();

        // Security check: Prevent path traversal
        if (!avatarPath.startsWith(uploadsDir) || !Files.exists(avatarPath)) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes = Files.readAllBytes(avatarPath);
        String contentType = Files.probeContentType(avatarPath);

        if (contentType == null) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE)
                    .body(imageBytes); // ✅ fixed typo here
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(imageBytes);
    }


    @GetMapping("/search-users")
    @ResponseBody
    public List<Map<String, String>> searchUsers(@RequestParam String query, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");

        // Exclude yourself from the results
        // Exclude existing contacts
        List<User> users = userService.searchUsers(query, currentUser.getId());

        return users.stream().map(user -> {
            Map<String, String> m = new HashMap<>();
            m.put("name", user.getName());
            return m;
        }).collect(Collectors.toList());
    }



    @PostMapping("/add-contact")
    public String addContact(@RequestParam String contactSearch, HttpSession session, RedirectAttributes redirectAttributes) {


        User currentUser = (User) session.getAttribute("user");
        Optional<User> found = userRepository.findByName(contactSearch);

        if (found.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found.");
            return "redirect:/dashboard";
        }

        userService.addContact(currentUser.getId(), found.get().getId());
        System.out.println(currentUser.getName() + "Send Request to add "+ found.get().getName());
        return "redirect:/dashboard";
    }



    @PostMapping("/contact-request/accept")
    public String acceptRequest(@RequestParam String requestId, RedirectAttributes redirectAttributes, Principal principal)
    {




        ContactRequest request = contactRequestRepository.findById(requestId).orElse(null);
        if(request ==null)
        {
            redirectAttributes.addFlashAttribute("error", "Request not found.");
            return "redirect:/dashboard";
        }

        //make sure the request is for current user
        User currentUser = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
        if(currentUser ==null)
        {
            redirectAttributes.addFlashAttribute("error", "user not found.");
            return "redirect:/dashboard";
        }

        if(!Objects.equals(currentUser.getId(), request.getReceiverId()))
        {
            redirectAttributes.addFlashAttribute("error", "this request not for you !");
            return "redirect:/dashboard";
        }

        //ok the request is for the receiver = currentUser so we update the status
        System.out.println("ok the request is for the receiver = currentUser so we update the status");

        request.setRespondedAt(LocalDateTime.now());
        request.setStatus(ContactRequestStatus.ACCEPTED);
        contactRequestRepository.save(request);

        // ok because the receiver accept so we add in both side as contact
        List<String> receiverContacts = currentUser.getContactIds(); //get current contacts
        receiverContacts.add(request.getSenderId()); //add the new contact
        currentUser.setContactIds(receiverContacts); // set all contacts back
        userRepository.save(currentUser); // save

        User  senderUser = userRepository.findById(request.getSenderId()).orElse(null);
        if(senderUser ==null)
        {
            redirectAttributes.addFlashAttribute("error", "sender not found !");
            return "redirect:/dashboard";
        }

        List<String> senderContacts = senderUser.getContactIds();
        senderContacts.add(request.getReceiverId());
        senderUser.setContactIds(senderContacts);
        userRepository.save(senderUser);

        //inform the sender by websocket that the pending response has been updated so show it as contact now
        // 🔥 Broadcast to clients
        webSocketHandler.sendToUser(request.getSenderId(), request, "REQUEST_ACCEPTED");
        webSocketHandler.sendToUser(request.getReceiverId(), request, "REQUEST_ACCEPTED");

        return "redirect:/dashboard";
    }




}
