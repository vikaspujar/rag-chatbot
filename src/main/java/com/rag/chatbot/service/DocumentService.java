package com.rag.chatbot.service;

import com.rag.chatbot.model.DocumentInfo;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class DocumentService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final List<DocumentInfo> uploadedDocs = new CopyOnWriteArrayList<>();

    public DocumentService(EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
    }

    public DocumentInfo ingest(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
        String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase() : "txt";

        String text = switch (ext) {
            case "pdf" -> extractPdf(file.getInputStream());
            default -> new String(file.getBytes());
        };

        if (text.isBlank()) {
            throw new IOException("No text could be extracted from: " + fileName);
        }

        Metadata metadata = new Metadata();
        metadata.put("file_name", fileName);
        metadata.put("file_type", ext);

        Document document = Document.from(text, metadata);

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(600, 100))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();

        ingestor.ingest(document);

        // Rough chunk count estimate
        int chunkCount = Math.max(1, text.length() / 600);
        String docId = UUID.randomUUID().toString().substring(0, 8);
        DocumentInfo info = new DocumentInfo(docId, fileName, ext.toUpperCase(), chunkCount, file.getSize());
        uploadedDocs.add(info);
        return info;
    }

    public List<DocumentInfo> getDocuments() {
        return Collections.unmodifiableList(uploadedDocs);
    }

    public int getTotalChunks() {
        return uploadedDocs.stream().mapToInt(DocumentInfo::getChunkCount).sum();
    }

    private String extractPdf(InputStream stream) throws IOException {
        try (PDDocument doc = PDDocument.load(stream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }
}
