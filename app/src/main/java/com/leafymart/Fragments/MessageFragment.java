package com.leafymart.Fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.leafymart.Adapter.MessageAdapter;
import com.leafymart.Manager.SessionManager;
import com.leafymart.Model.ApiMessage;
import com.leafymart.Model.ConversationResponse;
import com.leafymart.Model.Message;
import com.leafymart.Model.SendMessageResponse;
import com.leafymart.R;
import com.leafymart.Structure.ApiConfig;
import com.leafymart.Structure.MySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessageFragment extends Fragment {

    private static final String TAG = "MessageFragment";
    private RecyclerView recyclerView;
    private EditText inputMessage;
    private Button btnSend;

    private MessageAdapter adapter;
    private List<Message> uiMessageList = new ArrayList<>();
    private int currentConversationId = -1;
    private int currentUserId = -1;   // Get from session

    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private Runnable pollingRunnable;
    private final long POLLING_INTERVAL_MS = 5000;
    private boolean isPollingActive = false;

    private final Gson gson = new Gson();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        inputMessage = view.findViewById(R.id.inputMessage);
        btnSend = view.findViewById(R.id.btnSend);

        setupRecyclerView();
        setupSendButton();

        currentUserId = SessionManager.getUserId(requireContext());

        if (currentUserId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            disableChatUI();
        } else {
            createOrGetConversation();
        }

        return view;
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter(uiMessageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void setupSendButton() {
        btnSend.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!messageText.isEmpty()) {
                if (currentConversationId != -1) {
                    sendMessageToApi(messageText);
                    inputMessage.setText("");
                } else {
                    Toast.makeText(getContext(), "Chat not initialized yet", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void createOrGetConversation() {
        if (currentUserId == -1) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            disableChatUI();
            return;
        }

        String url = buildUrl("conversations/start");
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("user_id", currentUserId);
            // No admin_id sent because backend assigns automatically
        } catch (JSONException e) {
            Log.e(TAG, "JSON error creating conversation", e);
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> handleConversationResponse(response),
                error -> handleConversationError(error));

        request.setTag(TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(10000, 1, 1));
        MySingleton.get(requireContext()).add(request);
    }

    private void handleConversationResponse(JSONObject response) {
        try {
            ConversationResponse data = gson.fromJson(response.toString(), ConversationResponse.class);
            if (data != null && data.isSuccess()) {
                currentConversationId = data.getConversationId();
                if (data.getMessages() != null) {
                    processMessages(data.getMessages(), true);
                }
                startPollingForMessages();
                enableChatUI();
            } else {
                showChatError(data != null ? data.getApiResultMessage() : "Unknown error");
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing conversation response", e);
            showChatError("Error processing chat data");
        }
    }

    private void handleConversationError(VolleyError error) {
        handleVolleyError("createConversation", error);
        disableChatUI();
    }

    private void processMessages(List<ApiMessage> apiMessages, boolean clearExisting) {
        if (clearExisting) {
            uiMessageList.clear();
        }

        for (ApiMessage msg : apiMessages) {
            boolean isReceived = "admin".equalsIgnoreCase(msg.getSenderType());

            uiMessageList.add(new Message(
                    msg.getMessage(),
                    formatDisplayTimestamp(msg.getCreatedAt()),
                    isReceived,
                    msg.getSenderName(),
                    msg.getCreatedAt(),
                    msg.getId()
            ));
        }
        adapter.notifyDataSetChanged();
        safeScrollToBottom();
    }

    private void fetchMessages() {
        if (currentConversationId == -1 || getContext() == null || !isAdded()) return;

        String url = buildUrl("conversations/" + currentConversationId + "/messages");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        ConversationResponse data = gson.fromJson(response.toString(), ConversationResponse.class);
                        if (data != null && data.isSuccess() && data.getMessages() != null) {
                            processMessages(data.getMessages(), true);
                        }
                    } catch (JsonSyntaxException e) {
                        Log.e(TAG, "Error parsing messages", e);
                    }
                },
                error -> Log.w(TAG, "Polling error: " + error.toString())
        );

        request.setTag(TAG);
        MySingleton.get(requireContext()).add(request);
    }

    private void sendMessageToApi(String messageText) {
        String url = buildUrl("conversations/" + currentConversationId + "/messages");
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("sender_id", currentUserId);
            requestBody.put("sender_type", "user");
            requestBody.put("message", messageText);
        } catch (JSONException e) {
            Log.e(TAG, "JSON error in sendMessage", e);
            return;
        }

        Message tempMessage = new Message(
                messageText,
                getDisplayTime() + " (Sending...)",
                false,
                "user",
                getCurrentTimestamp(),
                -1
        );
        addMessageToUI(tempMessage);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> updateSentMessage(response, tempMessage),
                error -> handleSendError(error, tempMessage));

        request.setTag(TAG);
        MySingleton.get(requireContext()).add(request);
    }

    private void addMessageToUI(Message message) {
        uiMessageList.add(message);
        adapter.notifyItemInserted(uiMessageList.size() - 1);
        safeScrollToBottom();
    }

    private void updateSentMessage(JSONObject response, Message tempMessage) {
        try {
            SendMessageResponse data = gson.fromJson(response.toString(), SendMessageResponse.class);
            if (data != null && data.isSuccess()) {
                tempMessage.setMessageId(data.getMessageId());
                tempMessage.setDisplayTimestamp(getDisplayTime());
                adapter.notifyItemChanged(uiMessageList.indexOf(tempMessage));
            } else {
                markMessageFailed(tempMessage);
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Error parsing send response", e);
            markMessageFailed(tempMessage);
        }
    }

    private void handleSendError(VolleyError error, Message tempMessage) {
        Log.e(TAG, "Send message error", error);
        tempMessage.setContent(tempMessage.getContent() + " (Failed to send)");
        adapter.notifyItemChanged(uiMessageList.indexOf(tempMessage));

        if (error.networkResponse != null && error.networkResponse.data != null) {
            String errorBody = new String(error.networkResponse.data, StandardCharsets.UTF_8);
            Log.e(TAG, "Error details: " + errorBody);
            if (errorBody.contains("foreign key constraint")) {
                Toast.makeText(getContext(), "Database error: Invalid user or conversation", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void markMessageFailed(Message message) {
        message.setContent(message.getContent() + " (Failed)");
        adapter.notifyItemChanged(uiMessageList.indexOf(message));
    }

    private void safeScrollToBottom() {
        if (uiMessageList.isEmpty()) return;

        recyclerView.post(() -> {
            int targetPosition = uiMessageList.size() - 1;
            if (targetPosition >= 0 && targetPosition < adapter.getItemCount()) {
                recyclerView.smoothScrollToPosition(targetPosition);
            }
        });
    }

    private void startPollingForMessages() {
        if (isPollingActive || currentConversationId == -1) return;

        isPollingActive = true;
        pollingRunnable = new Runnable() {
            @Override
            public void run() {
                if (isPollingActive) {
                    fetchMessages();
                    pollingHandler.postDelayed(this, POLLING_INTERVAL_MS);
                }
            }
        };
        pollingHandler.postDelayed(pollingRunnable, POLLING_INTERVAL_MS);
    }

    private void stopPollingForMessages() {
        isPollingActive = false;
        pollingHandler.removeCallbacksAndMessages(null);
    }

    private String buildUrl(String endpoint) {
        String base = ApiConfig.BASE_URL;  // e.g. "http://10.0.2.2:5000/"
        if (!base.endsWith("/") && !endpoint.startsWith("/")) {
            base += "/";
        }
        return base + endpoint;
    }

    private void handleVolleyError(String operation, VolleyError error) {
        Log.e(TAG, operation + " error", error);
        Toast.makeText(getContext(), operation + " failed", Toast.LENGTH_SHORT).show();
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
    }

    private String getDisplayTime() {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
    }

    private String formatDisplayTimestamp(String apiTimestamp) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH);
            if (apiTimestamp.endsWith("Z")) inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(apiTimestamp);
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        } catch (ParseException e) {
            return apiTimestamp;
        }
    }

    private void enableChatUI() {
        btnSend.setEnabled(true);
        inputMessage.setEnabled(true);
    }

    private void disableChatUI() {
        btnSend.setEnabled(false);
        inputMessage.setEnabled(false);
    }

    private void showChatError(String message) {
        Toast.makeText(getContext(), "Chat error: " + message, Toast.LENGTH_LONG).show();
        disableChatUI();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentConversationId != -1) {
            fetchMessages();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stopPollingForMessages();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopPollingForMessages();
        MySingleton.get(requireContext()).cancelAllRequests(TAG);
    }
}
