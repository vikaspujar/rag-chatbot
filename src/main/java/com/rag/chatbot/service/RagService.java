package com.rag.chatbot.service;

import com.rag.chatbot.model.ChatResponse;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.query.Query;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final ChatLanguageModel chatModel;
    private final ContentRetriever contentRetriever;

    @Value("${groq.api.key:}")
    private String groqApiKey;

    public RagService(ChatLanguageModel chatModel, ContentRetriever contentRetriever) {
        this.chatModel = chatModel;
        this.contentRetriever = contentRetriever;
    }

    public ChatResponse chat(String userMessage) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return ChatResponse.error(
                "No LLM API key configured. Set the GROQ_API_KEY environment variable. " +
                "Get a free key at https://console.groq.com/keys"
            );
        }

        // Retrieve relevant document chunks from the vector store
        List<Content> relevantContents = contentRetriever.retrieve(Query.from(userMessage));

        // Build context string and source list
        StringBuilder contextBuilder = new StringBuilder();
        List<ChatResponse.SourceInfo> sources = new ArrayList<>();

        for (Content content : relevantContents) {
            String chunk = content.textSegment().text();
            String fileName = content.textSegment().metadata().getString("file_name");
            if (fileName == null) fileName = "Document";

            contextBuilder.append(chunk).append("\n\n---\n\n");

            String excerpt = chunk.length() > 200 ? chunk.substring(0, 200) + "…" : chunk;
            // EmbeddingMatch score not directly on Content in all versions; default to 0.0
            sources.add(new ChatResponse.SourceInfo(fileName, excerpt, 0.0));
        }

        boolean ragUsed = !relevantContents.isEmpty();
        String systemPrompt = buildSystemPrompt(contextBuilder.toString(), ragUsed);

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(systemPrompt));
        messages.add(UserMessage.from(userMessage));

        try {
            Response<AiMessage> response = chatModel.generate(messages);
            String answer = response.content().text();

            // Deduplicate sources by file name
            List<ChatResponse.SourceInfo> dedupedSources = sources.stream()
                .collect(Collectors.toMap(
                    ChatResponse.SourceInfo::getFileName,
                    s -> s,
                    (a, b) -> a
                ))
                .values()
                .stream()
                .toList();

            return new ChatResponse(answer, dedupedSources, ragUsed);
        } catch (Exception e) {
            return ChatResponse.error("LLM call failed: " + e.getMessage());
        }
    }

    private String buildSystemPrompt(String context, boolean ragUsed) {
        if (!ragUsed) {
            return """
                You are a helpful assistant specializing in mainframe and IBM systems.
                No documents have been loaded into the knowledge base yet. Answer from your
                general knowledge and suggest the user uploads relevant documentation.
                Be concise and helpful.
                """;
        }
        return """
            You are a helpful assistant specializing in mainframe, IBM systems, and COBOL migration.
            Use ONLY the following context extracted from the uploaded documents to answer the question.
            If the context does not contain enough information, say so clearly and briefly.
            Cite relevant details from the context. Be concise and accurate.

            CONTEXT:
            """ + context;
    }
}
