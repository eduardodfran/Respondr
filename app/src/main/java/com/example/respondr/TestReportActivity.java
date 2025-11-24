package com.example.respondr;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TestReportActivity extends AppCompatActivity {

    private static final String TAG = "TestReportActivity";
    
    private Spinner emergencyTypeSpinner;
    private EditText descriptionInput;
    private EditText aiResponseInput;
    private Button saveButton;
    private Button backButton;
    
    private FirebaseReportManager firebaseReportManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate started");
        
        try {
            setContentView(R.layout.activity_test_report);
            Log.d(TAG, "Layout set");

            // Find views first
            emergencyTypeSpinner = findViewById(R.id.emergencyTypeSpinner);
            descriptionInput = findViewById(R.id.descriptionInput);
            aiResponseInput = findViewById(R.id.aiResponseInput);
            saveButton = findViewById(R.id.saveButton);
            backButton = findViewById(R.id.backButton);
            Log.d(TAG, "Views found");

            // Setup spinner with emergency types
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.emergency_types, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            emergencyTypeSpinner.setAdapter(adapter);
            Log.d(TAG, "Spinner adapter set");

            // Initialize Firebase manager (do this after UI setup)
            firebaseReportManager = new FirebaseReportManager();
            Log.d(TAG, "Firebase manager initialized");

            // Save button click
            saveButton.setOnClickListener(v -> saveTestReport());

            // Back button click
            backButton.setOnClickListener(v -> finish());
            
            Log.d(TAG, "onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error initializing: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            finish();
        }
    }

    private void saveTestReport() {
        String emergencyType = emergencyTypeSpinner.getSelectedItem().toString();
        String description = descriptionInput.getText().toString().trim();
        String aiResponse = aiResponseInput.getText().toString().trim();

        // Validate inputs
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (aiResponse.isEmpty()) {
            Toast.makeText(this, "Please enter AI response", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mock location data
        String latitude = "14.5995";
        String longitude = "120.9842";
        String location = latitude + "° N, " + longitude + "° E (Test Report)";
        String testAddress = "Test Address - Manual Entry";
        String addressStatus = "provided";

        // Create report
        FirebaseReportManager.EmergencyReport report = new FirebaseReportManager.EmergencyReport(
            emergencyType,
            description,
            location,
            latitude,
            longitude,
            testAddress,
            addressStatus,
            aiResponse
        );

        // Disable button while saving
        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        // Save to Firebase
        firebaseReportManager.saveReport(report, new FirebaseReportManager.SaveCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(TestReportActivity.this, 
                        "✅ Test report saved successfully!", 
                        Toast.LENGTH_LONG).show();
                    
                    // Clear inputs
                    descriptionInput.setText("");
                    aiResponseInput.setText("");
                    emergencyTypeSpinner.setSelection(0);
                    
                    // Re-enable button
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Test Report");
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(TestReportActivity.this, 
                        "❌ Failed to save: " + error, 
                        Toast.LENGTH_LONG).show();
                    
                    // Re-enable button
                    saveButton.setEnabled(true);
                    saveButton.setText("Save Test Report");
                });
            }
        });
    }
}
