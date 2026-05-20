package com.rag.chatbot.model;

import java.util.List;

public class ChatResponse {
    private String answer;
    private List<SourceInfo> sources;
    private boolean ragUsed;
    private String error;

    public ChatResponse() {}

    public ChatResponse(String answer, List<SourceInfo> sources, boolean ragUsed) {
        this.answer = answer;
        this.sources = sources;
        this.ragUsed = ragUsed;
    }

    public static ChatResponse error(String errorMessage) {
        ChatResponse r = new ChatResponse();
        r.error = errorMessage;
        return r;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public List<SourceInfo> getSources() { return sources; }
    public void setSources(List<SourceInfo> sources) { this.sources = sources; }
    public boolean isRagUsed() { return ragUsed; }
    public void setRagUsed(boolean ragUsed) { this.ragUsed = ragUsed; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public static class SourceInfo {
        private String fileName;
        private String excerpt;
        private double score;

        public SourceInfo(String fileName, String excerpt, double score) {
            this.fileName = fileName;
            this.excerpt = excerpt;
            this.score = score;
        }

        public String getFileName() { return fileName; }
        public String getExcerpt() { return excerpt; }
        public double getScore() { return score; }
    }
}
