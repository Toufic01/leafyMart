package com.leafymart.Model;

public class Message {
    private String content;
    private String displayTimestamp;
    private boolean isReceived;
    private String originalSenderType;
    private String rawApiTimestamp;
    private int messageId; // Optional: store API message ID for reference/updates

    public Message(String content, String displayTimestamp, boolean isReceived, String originalSenderType, String rawApiTimestamp, int messageId) {
        this.content = content;
        this.displayTimestamp = displayTimestamp;
        this.isReceived = isReceived;
        this.originalSenderType = originalSenderType;
        this.rawApiTimestamp = rawApiTimestamp;
        this.messageId = messageId;
    }

    // Getters
    public String getContent() { return content; }
    public String getDisplayTimestamp() { return displayTimestamp; }
    public boolean isReceived() { return isReceived; }
    public String getOriginalSenderType() { return originalSenderType; }
    public String getRawApiTimestamp() { return rawApiTimestamp; }
    public int getMessageId() { return messageId; }


    public void setDisplayTimestamp(String displayTimestamp) {
        this.displayTimestamp = displayTimestamp;
    }

    public void setContent(String content) { // For updating "sending..."
        this.content = content;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }
}
