package org.example.dto;

public class ChatResponseDTO {
    private String response;
    private boolean success;

    public ChatResponseDTO(String response, boolean success) {
        this.response = response;
        this.success = success;
    }

    // Getters & Setters
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
}