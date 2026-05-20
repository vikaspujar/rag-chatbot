# RAG Chatbot

A Spring Boot RAG (Retrieval-Augmented Generation) chatbot for querying IBM Redbooks and mainframe documentation.

## Features

- **Chat interface** — Ask questions, get answers grounded in your documents
- **Document upload** — PDF, TXT, MD files (up to 200 MB)
- **In-process embeddings** — No external embedding API needed (uses `all-MiniLM-L6-v2` via ONNX)
- **Groq LLM** — Free-tier LLaMA 3 via [console.groq.com](https://console.groq.com)
- **Login page** — Spring Security with dummy users
- **Railway-ready** — Dockerfile + `railway.toml` included

## Demo Credentials

| Username | Password | Role  |
|----------|----------|-------|
| admin    | admin123 | Admin |
| user1    | password1 | User  |
| demo     | demo123  | User  |

## Quick Start (Local)

```bash
# 1. Get a free Groq API key at https://console.groq.com/keys

# 2. Build and run
export GROQ_API_KEY=gsk_your_key_here
cd 40-tools/95-rag-chatbot
./mvnw spring-boot:run

# 3. Open http://localhost:8080
```

## Environment Variables

| Variable       | Required | Default                    | Description             |
|----------------|----------|----------------------------|-------------------------|
| `GROQ_API_KEY` | Yes      | —                          | Free key from Groq      |
| `GROQ_MODEL`   | No       | `llama-3.1-8b-instant`     | Groq model name         |
| `PORT`         | No       | `8080`                     | HTTP port (Railway sets)|

### Switching models (Groq free tier)

- `llama-3.1-8b-instant` — fastest, default
- `llama-3.3-70b-versatile` — best quality, slower
- `mixtral-8x7b-32768` — long context

## Deploy to Railway

1. Push this repo to GitHub
2. Create a new Railway project → "Deploy from GitHub"
3. Set env var: `GROQ_API_KEY=gsk_...`
4. Railway auto-detects the Dockerfile and deploys

## Architecture

```
Browser ──► Spring MVC (Thymeleaf)
               │
               ▼
         REST API (/api/*)
               │
         ┌─────┴──────┐
         │             │
    RagService    DocumentService
         │             │
   Groq LLM      LangChain4j
   (OpenAI-compatible)  EmbeddingStoreIngestor
         │             │
    ContentRetriever ◄─┘
         │
   InMemoryEmbeddingStore
   (all-MiniLM-L6-v2-q, in-process)
```

**Note:** The vector store is in-memory and resets on restart. Documents need to be re-uploaded after a redeploy. For production, swap `InMemoryEmbeddingStore` with a persistent store (e.g., PGVector, Chroma, Weaviate).

## Replacing the LLM

The Groq endpoint is OpenAI-compatible. To swap to a different provider, update `AiConfig.java`:

```java
// OpenAI
.apiKey(openAiKey).baseUrl("https://api.openai.com/v1").modelName("gpt-4o-mini")

// Ollama (local)
// Use langchain4j-ollama instead of langchain4j-open-ai
OllamaChatModel.builder().baseUrl("http://localhost:11434").modelName("llama3").build()
```
