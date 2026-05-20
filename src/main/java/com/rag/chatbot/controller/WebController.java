package com.rag.chatbot.controller;

import com.rag.chatbot.service.DocumentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    private final DocumentService documentService;

    public WebController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/chat";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/chat")
    public String chatPage(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("docCount", documentService.getDocuments().size());
        model.addAttribute("chunkCount", documentService.getTotalChunks());
        return "chat";
    }

    @GetMapping("/documents")
    public String documentsPage(Model model, Authentication auth) {
        model.addAttribute("username", auth.getName());
        model.addAttribute("documents", documentService.getDocuments());
        model.addAttribute("docCount", documentService.getDocuments().size());
        model.addAttribute("chunkCount", documentService.getTotalChunks());
        return "documents";
    }
}
