package com.rag.chatbot.controller;

import com.rag.chatbot.model.ChatRequest;
import com.rag.chatbot.model.ChatResponse;
import com.rag.chatbot.model.DocumentInfo;
import com.rag.chatbot.service.DocumentService;
import com.rag.chatbot.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final RagService ragService;
    private final DocumentService documentService;

    public ApiController(RagService ragService, DocumentService documentService) {
        this.ragService = ragService;
        this.documentService = documentService;
    }

    @PostMapping("/chat")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            return ResponseEntity.badRequest().body(ChatResponse.error("Message cannot be empty"));
        }
        ChatResponse response = ragService.chat(request.getMessage().trim());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/documents/upload")
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No file selected"));
        }

        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (!fileName.endsWith(".pdf") && !fileName.endsWith(".txt") && !fileName.endsWith(".md")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Supported formats: PDF, TXT, MD"));
        }

        try {
            DocumentInfo info = documentService.ingest(file);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Ingested '" + info.getFileName() + "' → " + info.getChunkCount() + " chunks",
                "document", info
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to process document: " + e.getMessage()));
        }
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentInfo>> getDocuments() {
        return ResponseEntity.ok(documentService.getDocuments());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "documents", documentService.getDocuments().size(),
            "chunks", documentService.getTotalChunks()
        ));
    }
}
