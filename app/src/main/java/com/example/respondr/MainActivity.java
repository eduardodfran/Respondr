package com.example.respondr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_SPEECH = 1001;
    private static final int REQ_PERMISSIONS = 2001;

    private EditText input;
    private Button sendBtn;
    private ImageButton speakBtn;
    private TextView responseView;
    private GeminiClient geminiClient;
    private static final String TAG = "Respondr";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        input = findViewById(R.id.input);
        sendBtn = findViewById(R.id.sendBtn);
        speakBtn = findViewById(R.id.speakBtn);
        responseView = findViewById(R.id.responseView);
        responseView.setMovementMethod(new ScrollingMovementMethod());

    geminiClient = new GeminiClient(this);

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
        input.setText(""); // Clear input after sending
        appendResponse("🚨 You: " + text + "\n");
        geminiClient.sendMessage(text, new GeminiClient.ResultCallback() {
            @Override
            public void onSuccess(String reply) {
                runOnUiThread(() -> {
                    Log.i(TAG, "Gemini success: " + reply);
                    appendResponse("🤖 Gemini AI:\n" + reply + "\n");
                    sendBtn.setEnabled(true);
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Log.e(TAG, "Gemini error: " + error);
                    appendResponse("❌ Error:\n" + error + "\n");
                    sendBtn.setEnabled(true);
                });
            }
        });
    }

    private void appendResponse(String text) {
        responseView.append(text + "\n");
        // keep scroll at bottom
        final int scrollAmount = responseView.getLayout().getLineTop(responseView.getLineCount()) - responseView.getHeight();
        if (scrollAmount > 0)
            responseView.scrollTo(0, scrollAmount);
        else
            responseView.scrollTo(0, 0);
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