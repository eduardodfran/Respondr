package com.example.respondr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import android.util.Log;
import java.util.ArrayList;

import io.noties.markwon.Markwon;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH = 1001;
    private static final int REQ_PERMISSIONS = 2001;

    private EditText input;
    private Button sendBtn;
    private ImageButton speakBtn;
    private RecyclerView chatRecyclerView;
    private ChatAdapter chatAdapter;
    private Markwon markwon;
    private GeminiClient geminiClient;
    private static final String TAG = "Respondr";
    
    private Button policeBtn, medicBtn, fireBtn, allBtn;
    private String selectedEmergencyType = ""; // Track manually selected type
    private boolean inFollowUpMode = false;
    private LinearLayout quickResponseButtons;
    private HorizontalScrollView quickResponseScroll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            
            // Apply system bars padding to top and sides, but handle bottom differently for keyboard
            v.setPadding(
                systemBars.left, 
                systemBars.top, 
                systemBars.right, 
                Math.max(systemBars.bottom, ime.bottom)
            );
            return insets;
        });

        input = findViewById(R.id.input);
        sendBtn = findViewById(R.id.sendBtn);
        speakBtn = findViewById(R.id.speakBtn);
        quickResponseButtons = findViewById(R.id.quickResponseButtons);
        quickResponseScroll = findViewById(R.id.quickResponseScroll);
        chatRecyclerView = findViewById(R.id.chatRecyclerView);

        // Set up RecyclerView for chat
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start from bottom
        chatRecyclerView.setLayoutManager(layoutManager);
        
        // Initialize Markwon for markdown rendering
        markwon = Markwon.create(this);
        chatAdapter = new ChatAdapter(markwon);
        chatRecyclerView.setAdapter(chatAdapter);

        // Add welcome message
        chatAdapter.addMessage(new ChatMessage(
            "💬 **Welcome to Respondr!**\n\nType or speak to describe an emergency situation and get instant AI assistance.",
            false
        ));

        // Emergency type buttons
        policeBtn = findViewById(R.id.policeBtn);
        medicBtn = findViewById(R.id.medicBtn);
        fireBtn = findViewById(R.id.fireBtn);
        allBtn = findViewById(R.id.allBtn);

        geminiClient = new GeminiClient(this);

        // Emergency type button listeners
        policeBtn.setOnClickListener(v -> {
            selectedEmergencyType = "POLICE";
            input.setText("I need police assistance. ");
            input.setSelection(input.getText().length());
        });

        medicBtn.setOnClickListener(v -> {
            selectedEmergencyType = "MEDICAL";
            input.setText("I need medical assistance. ");
            input.setSelection(input.getText().length());
        });

        fireBtn.setOnClickListener(v -> {
            selectedEmergencyType = "FIRE";
            input.setText("There's a fire emergency. ");
            input.setSelection(input.getText().length());
        });

        allBtn.setOnClickListener(v -> {
            selectedEmergencyType = "ALL";
            input.setText("I need all emergency units. ");
            input.setSelection(input.getText().length());
        });

        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Enter a message or use speech", Toast.LENGTH_SHORT).show();
                return;
            }
            sendToGemini(text);
        });

        speakBtn.setOnClickListener(v -> {
            if (checkAudioPermission()) {
                startSpeechRecognizer();
            } else {
                requestAudioPermission();
            }
        });

        // Scroll to bottom when keyboard opens and user focuses input
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && chatAdapter.getItemCount() > 0) {
                chatRecyclerView.postDelayed(() -> {
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                }, 300); // Delay to wait for keyboard animation
            }
        });
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQ_PERMISSIONS);
    }

    private void startSpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Describe the emergency");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH);
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_CODE_SPEECH && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spoken = results.get(0);
                input.setText(spoken);
                sendToGemini(spoken);
            }
        }
    }

    private void sendToGemini(String text) {
        sendBtn.setEnabled(false);
        String userMessage = text;
        input.setText(""); // Clear input after sending
        selectedEmergencyType = ""; // Reset selection
        
        // Add user message to chat
        chatAdapter.addMessage(new ChatMessage(userMessage, true));
        chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        
        // Enhanced prompt for emergency analysis
        String enhancedPrompt = "You are an emergency response AI assistant. Analyze this emergency report and respond with:\n\n" +
                "1. **EMERGENCY TYPE(S)**: Identify which emergency services are needed (Police 🚔, Medical 🚑, Fire 🚒, or multiple)\n" +
                "2. **URGENCY LEVEL**: Critical/High/Medium/Low\n" +
                "3. **KEY DETAILS**: Important information from the report\n" +
                "4. **RECOMMENDED ACTIONS**: Immediate steps the person should take\n\n" +
                "If you need more information to better assess the emergency, ask ONE follow-up question and provide 3-4 quick response options.\n" +
                "Format quick responses as: [QUICK_RESPONSES: option1 | option2 | option3 | option4]\n" +
                "Example: [QUICK_RESPONSES: 1 person | 2-3 people | 4+ people | Unknown]\n\n" +
                "Emergency Report: " + userMessage + "\n\n" +
                "Use markdown formatting (bold, lists, etc.) for clarity. Respond in a clear, concise format.";
        
        geminiClient.sendMessage(enhancedPrompt, new GeminiClient.ResultCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    Log.i(TAG, "Gemini success: " + reply);
                    
                    // Add AI response to chat
                    chatAdapter.addMessage(new ChatMessage(reply, false));
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    
                    parseAndShowQuickResponses(reply);
                    sendBtn.setEnabled(true);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Gemini error: " + error);
                    
                    // Add error message to chat
                    chatAdapter.addMessage(new ChatMessage("❌ **Error**: " + error, false));
                    chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                    
                    sendBtn.setEnabled(true);
                });
            }
        });
    }

    private void parseAndShowQuickResponses(String aiResponse) {
        try {
            // Check if response contains quick response suggestions in format:
            // [QUICK_RESPONSES: option1 | option2 | option3]
            if (aiResponse.contains("[QUICK_RESPONSES:")) {
                int startIndex = aiResponse.indexOf("[QUICK_RESPONSES:") + 17;
                int endIndex = aiResponse.indexOf("]", startIndex);
                
                if (endIndex > startIndex) {
                    String responsesStr = aiResponse.substring(startIndex, endIndex).trim();
                    String[] responses = responsesStr.split("\\|");
                    
                    if (responses.length > 0) {
                        showQuickResponseButtons(responses);
                        inFollowUpMode = true;
                        return;
                    }
                }
            }
            
            // If no quick responses found, hide the buttons
            hideQuickResponseButtons();
            inFollowUpMode = false;
            
        } catch (Exception e) {
            Log.e(TAG, "Error parsing quick responses", e);
            hideQuickResponseButtons();
        }
    }

    private void showQuickResponseButtons(String[] responses) {
        quickResponseButtons.removeAllViews();
        
        for (String response : responses) {
            String trimmedResponse = response.trim();
            
            MaterialButton button = new MaterialButton(this);
            button.setText(trimmedResponse);
            button.setTextSize(14);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(8, 0, 8, 0);
            button.setLayoutParams(params);
            
            button.setOnClickListener(v -> {
                // Send the button's text as user response
                input.setText(trimmedResponse);
                sendToGemini(trimmedResponse);
                hideQuickResponseButtons();
            });
            
            quickResponseButtons.addView(button);
        }
        
        quickResponseScroll.setVisibility(View.VISIBLE);
    }

    private void hideQuickResponseButtons() {
        quickResponseScroll.setVisibility(View.GONE);
        quickResponseButtons.removeAllViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startSpeechRecognizer();
            } else {
                Toast.makeText(this, "Audio permission required for speech", Toast.LENGTH_SHORT).show();
            }
        }
    }
}