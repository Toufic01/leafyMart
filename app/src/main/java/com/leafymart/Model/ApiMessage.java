// com/leafymart/Model/ApiMessage.java
package com.leafymart.Model;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ApiMessage {
    @SerializedName("id")
    private int id;

    @SerializedName("message") // This will contain the actual text of the message
    private String message;

    @SerializedName("sender_id")
    private int senderId;

    @SerializedName("is_admin") // true if sender is an admin, false otherwise
    private boolean isAdminSender;

    // Setter (if you manually create these objects, not strictly needed for Gson deserialization)
    public void setAdminSender(boolean adminSender) {
        isAdminSender = adminSender;
    }

    @SerializedName("sender_name") // "admin1" or "user_xyz"
    private String senderName;

    @SerializedName("sender_type") // "admin" or "user"
    private String senderType;

    @SerializedName("created_at")
    private String createdAt; // ISO format string "YYYY-MM-DDTHH:MM:SS"

    // Getters...
    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public int getSenderId() {
        return senderId;
    }

    public boolean isAdminSender() {
        return isAdminSender;
    } // Or isIsAdminSender()

    public String getSenderName() {
        return senderName;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // And a class to hold the list
    public class ApiResponse {
        private List<ApiMessage> messages;

        public List<ApiMessage> getMessages() {
            return messages;
        }

    }
}
    