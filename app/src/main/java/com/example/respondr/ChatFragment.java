package com.example.respondr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import io.noties.markwon.Markwon;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private static final int REQ_CODE_SPEECH = 1001;
    private static final int REQ_PERMISSIONS = 2001;

    private EditText input;
    private Button sendBtn;
    private ImageButton speakBtn;
    private RecyclerView chatRecyclerView;
    private LinearLayout quickResponseButtons;
    private HorizontalScrollView quickResponseScroll;
    private Button policeBtn;
    private Button medicBtn;
    private Button fireBtn;
    private Button allBtn;

    private ChatAdapter chatAdapter;
    private Markwon markwon;
    private GeminiClient geminiClient;
    private FirebaseReportManager firebaseReportManager;

    private String selectedEmergencyType = "";
    private String currentUserMessage = "";
    private StringBuilder conversationHistory = new StringBuilder();
    private boolean isFirstMessage = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        input = view.findViewById(R.id.input);
        sendBtn = view.findViewById(R.id.sendBtn);
        speakBtn = view.findViewById(R.id.speakBtn);
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        quickResponseButtons = view.findViewById(R.id.quickResponseButtons);
        quickResponseScroll = view.findViewById(R.id.quickResponseScroll);
        policeBtn = view.findViewById(R.id.policeBtn);
        medicBtn = view.findViewById(R.id.medicBtn);
        fireBtn = view.findViewById(R.id.fireBtn);
        allBtn = view.findViewById(R.id.allBtn);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        markwon = Markwon.create(requireContext());
        chatAdapter = new ChatAdapter(markwon);
        chatRecyclerView.setAdapter(chatAdapter);

        geminiClient = new GeminiClient(requireContext());
        firebaseReportManager = new FirebaseReportManager();

        showWelcomeMessage();
        setupEmergencyButtons();
        setupInputActions();

        return view;
    }

    private void showWelcomeMessage() {
        chatRecyclerView.post(() -> chatAdapter.addMessage(new ChatMessage(
                "🚨 **Ready to help!**\n\nDescribe your emergency by typing, speaking, or tapping a button above.",
                false
        )));
    }

    private void setupEmergencyButtons() {
        policeBtn.setOnClickListener(v -> {
            resetConversation();
            prefillEmergency("POLICE", "I need police assistance. ");
        });
        medicBtn.setOnClickListener(v -> {
            resetConversation();
            prefillEmergency("MEDICAL", "I need medical assistance. ");
        });
        fireBtn.setOnClickListener(v -> {
            resetConversation();
            prefillEmergency("FIRE", "There's a fire emergency. ");
        });
        allBtn.setOnClickListener(v -> {
            resetConversation();
            prefillEmergency("ALL", "Emergency! I need all emergency services. ");
        });
    }

    private void resetConversation() {
        conversationHistory = new StringBuilder();
        isFirstMessage = true;
    }

    private void prefillEmergency(String type, String message) {
        selectedEmergencyType = type;
        input.setText(message);
        input.setSelection(message.length());
    }

    private void setupInputActions() {
        sendBtn.setOnClickListener(v -> {
            String userMessage = input.getText().toString().trim();
            if (userMessage.isEmpty()) {
                Toast.makeText(requireContext(), "Enter a message or use speech", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessage(userMessage);
        });

        speakBtn.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechRecognizer();
            } else {
                requestAudioPermission();
            }
        });
    }

    private void sendMessage(String userMessage) {
        hideQuickResponseButtons();
        chatAdapter.addMessage(new ChatMessage(userMessage, true));
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);

        // Add to conversation history
        conversationHistory.append("User: ").append(userMessage).append("\n");
        
        currentUserMessage = userMessage;
        String emergencyTypeForReport = selectedEmergencyType.isEmpty() ? "General Emergency" : selectedEmergencyType + " Emergency";
        
        input.setText("");
        String tempSelectedType = selectedEmergencyType;
        selectedEmergencyType = "";
        toggleSendingState(true);

        String prompt = buildEnhancedPrompt(userMessage);

        geminiClient.sendMessage(prompt, new GeminiClient.ResultCallback() {
            @Override
            public void onSuccess(String reply) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    Log.i(TAG, "Gemini success: " + reply);
                    
                    // Add AI response to conversation history
                    conversationHistory.append("Assistant: ").append(reply).append("\n\n");
                    
                    chatAdapter.addMessage(new ChatMessage(reply, false));
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    parseAndShowQuickResponses(reply);
                    toggleSendingState(false);
                    
                    // Only save report after conversation seems complete (no follow-up questions)
                    if (!reply.contains("[QUICK_RESPONSES:") && !isFirstMessage) {
                        saveEmergencyReport(emergencyTypeForReport, conversationHistory.toString(), reply);
                    }
                    
                    isFirstMessage = false;
                });
            }

            @Override
            public void onFailure(String error) {
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    Log.e(TAG, "Gemini error: " + error);
                    chatAdapter.addMessage(new ChatMessage("❌ **Error**: " + error, false));
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    toggleSendingState(false);
                });
            }
        });
    }

    private String buildEnhancedPrompt(String userMessage) {
        StringBuilder prompt = new StringBuilder();
        
        // If this is not the first message, include conversation context
        if (!isFirstMessage && conversationHistory.length() > 0) {
            prompt.append("Previous conversation context:\n")
                    .append(conversationHistory.toString())
                    .append("\n---\n\n");
        }
        
        prompt.append("You are an emergency response AI assistant. ");
        
        if (isFirstMessage) {
            prompt.append("Analyze this emergency report and respond with:\n\n")
                    .append("1. **EMERGENCY TYPE(S)**: Identify which emergency services are needed (Police 🚔, Medical 🚑, Fire 🚒, or multiple)\n")
                    .append("2. **URGENCY LEVEL**: Critical/High/Medium/Low\n")
                    .append("3. **KEY DETAILS**: Important information from the report\n")
                    .append("4. **RECOMMENDED ACTIONS**: Immediate steps the person should take\n\n")
                    .append("If you need more information to better assess the emergency, ask ONE follow-up question and provide 3-4 quick response options.\n")
                    .append("Format quick responses as: [QUICK_RESPONSES: option1 | option2 | option3 | option4]\n")
                    .append("Example: [QUICK_RESPONSES: 1 person | 2-3 people | 4+ people | Unknown]\n\n");

            if (!TextUtils.isEmpty(selectedEmergencyType)) {
                prompt.append("Selected emergency type: ").append(selectedEmergencyType).append('\n');
            }

            prompt.append("Emergency Report: ").append(userMessage).append("\n\n")
                    .append("Use markdown formatting (bold, lists, etc.) for clarity. Respond in a clear, concise format.");
        } else {
            // For follow-up messages, maintain context
            prompt.append("The user is providing additional details about the ongoing emergency situation. ")
                    .append("Continue the conversation naturally, asking clarifying questions if needed, or provide final assessment and recommendations.\n\n")
                    .append("If you need more information, ask ONE follow-up question with 3-4 quick response options.\n")
                    .append("Format quick responses as: [QUICK_RESPONSES: option1 | option2 | option3 | option4]\n\n")
                    .append("User's response: ").append(userMessage).append("\n\n")
                    .append("Use markdown formatting (bold, lists, etc.) for clarity.");
        }
        
        return prompt.toString();
    }

    private void parseAndShowQuickResponses(String aiResponse) {
        try {
            if (aiResponse.contains("[QUICK_RESPONSES:")) {
                int startIndex = aiResponse.indexOf("[QUICK_RESPONSES:") + 17;
                int endIndex = aiResponse.indexOf(']', startIndex);
                if (endIndex > startIndex) {
                    String responsesStr = aiResponse.substring(startIndex, endIndex).trim();
                    String[] responses = responsesStr.split("\\|");
                    if (responses.length > 0) {
                        showQuickResponseButtons(responses);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing quick responses", e);
        }
        hideQuickResponseButtons();
    }

    private void showQuickResponseButtons(String[] responses) {
        quickResponseButtons.removeAllViews();

        for (String response : responses) {
            final String trimmedResponse = response.trim();
            if (trimmedResponse.isEmpty()) {
                continue;
            }

            MaterialButton button = new MaterialButton(requireContext());
            button.setText(trimmedResponse);
            button.setTextSize(14);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            button.setLayoutParams(params);

            button.setOnClickListener(v -> {
                input.setText(trimmedResponse);
                input.setSelection(trimmedResponse.length());
                sendMessage(trimmedResponse);
            });

            quickResponseButtons.addView(button);
        }

        quickResponseScroll.setVisibility(View.VISIBLE);
    }

    private void hideQuickResponseButtons() {
        quickResponseScroll.setVisibility(View.GONE);
        quickResponseButtons.removeAllViews();
    }

    private void toggleSendingState(boolean isSending) {
        sendBtn.setEnabled(!isSending);
        speakBtn.setEnabled(!isSending);
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO}, REQ_PERMISSIONS);
    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe your emergency...");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH && resultCode == requireActivity().RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spokenText = results.get(0);
                input.setText(spokenText);
                input.setSelection(spokenText.length());
                sendMessage(spokenText.trim());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognizer();
            } else if (isAdded()) {
                Toast.makeText(requireContext(), "Audio permission required for speech", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveEmergencyReport(String emergencyType, String description, String aiResponse) {
        // Mock location data - in production, get actual GPS coordinates
        String latitude = "14.5995";
        String longitude = "120.9842";
        String location = latitude + "° N, " + longitude + "° E";
        
        FirebaseReportManager.EmergencyReport report = new FirebaseReportManager.EmergencyReport(
            emergencyType,
            description,
            location,
            latitude,
            longitude,
            aiResponse
        );
        
        firebaseReportManager.saveReport(report, new FirebaseReportManager.SaveCallback() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Emergency report saved to Firebase: " + emergencyType);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "✓ Report saved", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Failed to save report to Firebase: " + error);
                if (isAdded()) {
                    Toast.makeText(requireContext(), "Failed to save report", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
