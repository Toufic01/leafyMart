package com.leafymart.Model;

public class SendMessageResponse {
    private boolean success;
    private String message;
    private int message_id;
    private String created_at;

    public boolean isSuccess() {
        return success;
    }

    public int getMessageId() {
        return message_id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public String getApiResultMessage() {
        return message;
    }
}