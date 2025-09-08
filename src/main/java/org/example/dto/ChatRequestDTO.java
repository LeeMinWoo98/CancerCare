package org.example.dto;

public class ChatRequestDTO {
    private String message;
    private Integer diagnosisId;

    // Constructors
    public ChatRequestDTO() {}

    public ChatRequestDTO(String message, Integer diagnosisId) {
        this.message = message;
        this.diagnosisId = diagnosisId;
    }

    // Getters & Setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getDiagnosisId() { return diagnosisId; }
    public void setDiagnosisId(Integer diagnosisId) { this.diagnosisId = diagnosisId; }
}