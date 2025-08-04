package com.leafymart.Model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConversationResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message") // For status messages like "Conversation started", "Existing conversation found"
    private String apiResultMessage;

    @SerializedName("conversation_id")
    private int conversationId;

    @SerializedName("messages") // List of ApiMessage objects
    private List<ApiMessage> messages; // This will be empty for a brand new conversation

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getApiResultMessage() {
        return apiResultMessage;
    }

    public int getConversationId() {
        return conversationId;
    }

    public List<ApiMessage> getMessages() {
        return messages;
    }

    // Setters - generally not needed for response parsing but can be included
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setApiResultMessage(String apiResultMessage) {
        this.apiResultMessage = apiResultMessage;
    }

    public void setConversationId(int conversationId) {
        this.conversationId = conversationId;
    }

    public void setMessages(List<ApiMessage> messages) {
        this.messages = messages;
    }
}
