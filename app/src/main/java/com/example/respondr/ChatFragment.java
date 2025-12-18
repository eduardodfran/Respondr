package com.example.respondr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.ArrayList;

import io.noties.markwon.Markwon;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";
    private static final int REQ_CODE_SPEECH = 1001;
    private static final int REQ_PERMISSIONS = 2001;
    private static final int REQ_LOCATION = 3001;

    private EditText input;
    private FloatingActionButton sendBtn;
    private FloatingActionButton speakBtn;
    private RecyclerView chatRecyclerView;
    private LinearLayout quickResponseButtons;
    private HorizontalScrollView quickResponseScroll;
    private Button policeBtn;
    private Button medicBtn;
    private Button fireBtn;

    private ChatAdapter chatAdapter;
    private Markwon markwon;
    private GeminiClient geminiClient;
    private FirebaseReportManager firebaseReportManager;
    private FusedLocationProviderClient fusedLocationClient;
    private Location currentLocation = null;
    private String currentAddress = "";

    private String selectedEmergencyType = "";
    private String currentUserMessage = "";
    private StringBuilder conversationHistory = new StringBuilder();
    private boolean isFirstMessage = true;
    private String currentReportId = null; // Track the current report ID to update instead of creating duplicates

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);

        markwon = Markwon.create(requireContext());
        chatAdapter = new ChatAdapter(markwon);
        chatRecyclerView.setAdapter(chatAdapter);

        geminiClient = new GeminiClient(requireContext());
        firebaseReportManager = new FirebaseReportManager();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Check if restoring existing conversation from history
        Bundle args = getArguments();
        if (args != null && args.containsKey("reportId")) {
            restoreConversation(args);
        } else {
            showWelcomeMessage();
        }
        
        setupEmergencyButtons();
        setupInputActions();
        requestLocationPermissionAndFetch();

        return view;
    }
    
    private void restoreConversation(Bundle args) {
        currentReportId = args.getString("reportId");
        String description = args.getString("description");
        String aiResponse = args.getString("aiResponse");
        selectedEmergencyType = args.getString("emergencyType", "");
        isFirstMessage = false;
        
        Log.d(TAG, "Restoring conversation - User message: " + (description != null));
        Log.d(TAG, "Restoring conversation - AI response: " + (aiResponse != null));
        
        // Add user's original message
        if (description != null && !description.isEmpty()) {
            ChatMessage userMsg = new ChatMessage(description, true);
            chatAdapter.addMessage(userMsg);
            conversationHistory.append("User: ").append(description).append("\n");
            Log.d(TAG, "Added user message, isUser=" + userMsg.isUser());
        }
        
        // Add AI's response
        if (aiResponse != null && !aiResponse.isEmpty()) {
            ChatMessage aiMsg = new ChatMessage(aiResponse, false);
            chatAdapter.addMessage(aiMsg);
            conversationHistory.append("Assistant: ").append(aiResponse).append("\n");
            Log.d(TAG, "Added AI message, isUser=" + aiMsg.isUser());
        }
        
        // Add continuation prompt
        chatRecyclerView.post(() -> {
            chatAdapter.addMessage(new ChatMessage("You can continue this conversation below.", false));
            chatRecyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
        });
    }

    private void showWelcomeMessage() {
        chatRecyclerView.post(() -> chatAdapter.addMessage(new ChatMessage(
                "**Ready to help!**\n\nDescribe your emergency by typing, speaking, or tapping a button above.",
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
    }

    private void resetConversation() {
        conversationHistory = new StringBuilder();
        isFirstMessage = true;
        currentReportId = null; // Reset report ID for new conversation
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
                    
                    // Save or update report after each message
                    saveOrUpdateEmergencyReport(emergencyTypeForReport, conversationHistory.toString(), reply);
                    
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
        
        // If this is not the first message, include ONLY the last 2-3 exchanges to reduce token usage
        // This prevents quota exhaustion while maintaining context
        if (!isFirstMessage && conversationHistory.length() > 0) {
            String fullHistory = conversationHistory.toString();
            String[] exchanges = fullHistory.split("\n\n");
            
            // Keep only the last 2 complete exchanges (user + assistant pairs) max ~300 chars
            StringBuilder recentContext = new StringBuilder();
            int exchangesToKeep = Math.min(2, exchanges.length);
            int startIdx = Math.max(0, exchanges.length - exchangesToKeep);
            
            for (int i = startIdx; i < exchanges.length; i++) {
                recentContext.append(exchanges[i]).append("\n\n");
            }
            
            // Limit total context to 300 characters to save tokens
            String limitedContext = recentContext.toString();
            if (limitedContext.length() > 300) {
                limitedContext = limitedContext.substring(limitedContext.length() - 300);
            }
            
            prompt.append("Recent conversation:\n")
                    .append(limitedContext)
                    .append("---\n\n");
        }
        
        prompt.append("You are a professional emergency dispatcher with 10+ years of experience. ")
                .append("Your role is to quickly assess emergencies, provide life-saving instructions, and dispatch appropriate help. ")
                .append("Be calm, direct, and efficient. Get critical information fast.\n\n");
        
        // Add GPS location info if available
        if (currentLocation != null && !currentAddress.isEmpty()) {
            prompt.append("**DETECTED LOCATION (GPS):** ").append(currentAddress).append("\n")
                    .append("**IMPORTANT:** Start your response by confirming this location with the caller. ")
                    .append("Say: 'I'm showing your location as [address]. Is that correct?' ")
                    .append("If they say yes, proceed. If no, ask for the correct address.\n\n");
        } else if (currentLocation != null) {
            prompt.append("**DETECTED GPS COORDINATES:** ")
                    .append(String.format("%.6f", currentLocation.getLatitude())).append("° N, ")
                    .append(String.format("%.6f", currentLocation.getLongitude())).append("° E\n")
                    .append("**IMPORTANT:** Start by confirming: 'I have your GPS location. Can you tell me the address or landmark you're at?'\n\n");
        } else {
            prompt.append("**NO GPS LOCATION DETECTED** - You MUST ask for their exact location/address immediately.\n\n");
        }
        
        if (isFirstMessage) {
            prompt.append("**Initial Assessment Protocol:**\n\n")
                    .append("1. **EMERGENCY TYPE**: Identify services needed (Police 🚔, Medical 🚑, Fire 🚒, or multiple)\n")
                    .append("2. **URGENCY**: Critical/High/Medium - be decisive\n")
                    .append("3. **IMMEDIATE ACTION**: Tell them what to do RIGHT NOW (e.g., 'Apply pressure to the wound', 'Get everyone out of the building')\n")
                    .append("4. **DISPATCH STATUS**: Confirm help is being sent\n\n")
                    .append("Keep your response SHORT and ACTION-ORIENTED. Ask for critical missing information ONLY if absolutely necessary (e.g., 'Is the person breathing?', 'Are you in a safe location?').\n\n")
                    .append("If you must ask follow-ups, limit to ONE critical question with 2-3 quick options.\n")
                    .append("Format: [QUICK_RESPONSES: option1 | option2 | option3]\n\n");

            if (!TextUtils.isEmpty(selectedEmergencyType)) {
                prompt.append("Emergency type indicated: ").append(selectedEmergencyType).append('\n');
            }

            prompt.append("Caller's report: ").append(userMessage).append("\n\n")
                    .append("Respond like a real dispatcher: calm, confident, brief. Use markdown for clarity.");
        } else {
            // For follow-up messages - wrap up quickly
            prompt.append("Continue as the dispatcher. The caller is providing more information.\n\n")
                    .append("If you have enough info, provide FINAL INSTRUCTIONS and confirm dispatch. ")
                    .append("Do NOT ask unnecessary questions. ")
                    .append("Only ask ONE more critical question if vital information is missing.\n\n")
                    .append("Caller's response: ").append(userMessage).append("\n\n")
                    .append("Stay brief and action-focused. Close the call when you have what you need.");
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
        } else if (requestCode == REQ_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchCurrentLocation();
            } else if (isAdded()) {
                Toast.makeText(requireContext(), "Location permission needed for accurate emergency response", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void requestLocationPermissionAndFetch() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        } else {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION);
        }
    }

    private void fetchCurrentLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            return;
        }
        
        Log.d(TAG, "Fetching current location...");
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, 
                cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        Log.d(TAG, "✓ Location fetched successfully: " + location.getLatitude() + ", " + location.getLongitude());
                        // Convert coordinates to address
                        getAddressFromLocation(location);
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "📍 Location acquired", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.w(TAG, "Location is null, trying last known location");
                        getLastKnownLocation();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location: " + e.getMessage());
                    getLastKnownLocation();
                });
    }

    private void getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted for last known location");
            return;
        }
        
        Log.d(TAG, "Trying to get last known location...");
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        Log.d(TAG, "✓ Last known location: " + location.getLatitude() + ", " + location.getLongitude());
                        // Convert coordinates to address
                        getAddressFromLocation(location);
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "📍 Using last known location", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ No location available - both current and last known are null");
                        if (isAdded()) {
                            Toast.makeText(requireContext(), "⚠️ Could not get location - using default", Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get last known location: " + e.getMessage());
                });
    }

    private void getAddressFromLocation(Location location) {
        if (location == null) {
            return;
        }
        
        // Run geocoding in background thread
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), java.util.Locale.getDefault());
                
                // Check if geocoder is available on this device
                if (!Geocoder.isPresent()) {
                    Log.e(TAG, "Geocoder not available on this device");
                    currentAddress = "";
                    return;
                }
                
                java.util.List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), 
                    location.getLongitude(), 
                    1
                );
                
                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder addressText = new StringBuilder();
                    
                    Log.d(TAG, "=== Geocoder Debug ===");
                    Log.d(TAG, "Feature: " + address.getFeatureName());
                    Log.d(TAG, "SubThoroughfare: " + address.getSubThoroughfare());
                    Log.d(TAG, "Thoroughfare: " + address.getThoroughfare());
                    Log.d(TAG, "Locality: " + address.getLocality());
                    Log.d(TAG, "SubAdminArea: " + address.getSubAdminArea());
                    Log.d(TAG, "AdminArea: " + address.getAdminArea());
                    Log.d(TAG, "AddressLine(0): " + address.getAddressLine(0));
                    Log.d(TAG, "===================");
                    
                    // Strategy 1: Use thoroughfare (street) + locality (city)
                    String street = address.getThoroughfare();
                    String subStreet = address.getSubThoroughfare();
                    String locality = address.getLocality();
                    String subLocality = address.getSubLocality();
                    String subAdminArea = address.getSubAdminArea();
                    String adminArea = address.getAdminArea();
                    
                    // Build address from most specific to general
                    boolean hasValidStreet = false;
                    
                    // Add street number and name
                    if (subStreet != null && !subStreet.matches("^[A-Z0-9+]+$")) {
                        addressText.append(subStreet).append(" ");
                        hasValidStreet = true;
                    }
                    if (street != null && !street.matches("^[A-Z0-9+]+$")) {
                        addressText.append(street);
                        hasValidStreet = true;
                    }
                    
                    // Add separator if we have street
                    if (hasValidStreet && addressText.length() > 0) {
                        addressText.append(", ");
                    }
                    
                    // Add sublocality (barangay/neighborhood)
                    if (subLocality != null && !subLocality.matches("^[A-Z0-9+]+$")) {
                        addressText.append(subLocality).append(", ");
                    }
                    
                    // Add city
                    if (locality != null && !locality.matches("^[A-Z0-9+]+$")) {
                        addressText.append(locality);
                    } else if (subAdminArea != null && !subAdminArea.matches("^[A-Z0-9+]+$")) {
                        addressText.append(subAdminArea);
                    }
                    
                    // Add province/region
                    if (adminArea != null && !adminArea.matches("^[A-Z0-9+]+$") && 
                        !addressText.toString().contains(adminArea)) {
                        addressText.append(", ").append(adminArea);
                    }
                    
                    String result = addressText.toString().trim();
                    
                    // Remove trailing commas
                    result = result.replaceAll(",\\s*$", "");
                    
                    // Validate result - reject if it's a Plus Code or too short
                    if (result.isEmpty() || 
                        result.matches(".*[A-Z0-9]{4}\\+[A-Z0-9]{2,3}.*") || 
                        result.length() < 5) {
                        Log.w(TAG, "Invalid address format, rejecting: " + result);
                        currentAddress = "";
                    } else {
                        currentAddress = result;
                        Log.d(TAG, "✓ Valid address resolved: " + currentAddress);
                        
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> 
                                Toast.makeText(requireContext(), "📍 " + currentAddress, Toast.LENGTH_LONG).show()
                            );
                        }
                    }
                } else {
                    Log.w(TAG, "No address found for coordinates");
                    currentAddress = "";
                }
            } catch (Exception e) {
                Log.e(TAG, "Geocoding error: " + e.getMessage());
                e.printStackTrace();
                currentAddress = "";
            }
        }).start();
    }

    private void saveOrUpdateEmergencyReport(String emergencyType, String description, String aiResponse) {
        // Refresh location before saving
        refreshLocationAndSave(emergencyType, description, aiResponse);
    }
    
    private void refreshLocationAndSave(String emergencyType, String description, String aiResponse) {
        // Try to get fresh location before saving
        if (ContextCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
            CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, 
                    cancellationTokenSource.getToken())
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            currentLocation = location;
                            Log.d(TAG, "Fresh location for report: " + location.getLatitude() + ", " + location.getLongitude());
                            // Get fresh address too
                            getAddressFromLocation(location);
                        }
                        // Continue saving with updated or existing location
                        performSaveOrUpdate(emergencyType, description, aiResponse);
                    })
                    .addOnFailureListener(e -> {
                        Log.w(TAG, "Could not refresh location, using cached: " + e.getMessage());
                        // Continue with cached location
                        performSaveOrUpdate(emergencyType, description, aiResponse);
                    });
        } else {
            // No permission, use cached location
            performSaveOrUpdate(emergencyType, description, aiResponse);
        }
    }
    
    private void performSaveOrUpdate(String emergencyType, String description, String aiResponse) {
        // Extract address from conversation (user might have corrected it)
        String extractedAddress = extractAddressFromConversation(description);
        
        // Prefer geocoded address if user hasn't provided one
        String finalAddress = extractedAddress.isEmpty() ? currentAddress : extractedAddress;
        String addressStatus = finalAddress.isEmpty() ? "not_provided" : "provided";
        
        // Log extraction results for debugging
        Log.d(TAG, "Address - Extracted from chat: " + extractedAddress);
        Log.d(TAG, "Address - From GPS: " + currentAddress);
        Log.d(TAG, "Address - Final used: " + finalAddress);
        Log.d(TAG, "Address - Status: " + addressStatus);
        
        // Use real GPS coordinates if available
        String latitude = currentLocation != null 
            ? String.format("%.6f", currentLocation.getLatitude())
            : "14.5995"; // Fallback
            
        String longitude = currentLocation != null 
            ? String.format("%.6f", currentLocation.getLongitude())
            : "120.9842"; // Fallback
        
        Log.d(TAG, "Saving with coordinates - Lat: " + latitude + ", Lon: " + longitude + ", Has currentLocation: " + (currentLocation != null));
            
        String location = finalAddress.isEmpty() 
            ? latitude + "° N, " + longitude + "° E (GPS only - no address provided)"
            : finalAddress + " (" + latitude + "° N, " + longitude + "° E)";
        
        if (currentReportId == null) {
            // First save - create new report
            FirebaseReportManager.EmergencyReport report = new FirebaseReportManager.EmergencyReport(
                emergencyType,
                description,
                location,
                latitude,
                longitude,
                finalAddress.isEmpty() ? "Not provided" : finalAddress,
                addressStatus,
                aiResponse
            );
            
            Log.d(TAG, "Creating new report with address: " + report.address + ", status: " + report.addressStatus);
            
            firebaseReportManager.saveReport(report, new FirebaseReportManager.SaveCallback() {
                @Override
                public void onSuccess() {
                    currentReportId = report.id; // Store the report ID for future updates
                    Log.d(TAG, "Emergency report created in Firebase: " + emergencyType + " with ID: " + currentReportId);
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
        } else {
            // Update existing report
            Log.d(TAG, "Updating report " + currentReportId + " with address: " + finalAddress + ", status: " + addressStatus);
            
            firebaseReportManager.updateReport(currentReportId, description, finalAddress, 
                    addressStatus, location, aiResponse, new FirebaseReportManager.SaveCallback() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Emergency report updated in Firebase: " + currentReportId);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "✓ Report updated", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "Failed to update report: " + error);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Failed to update report", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void saveEmergencyReport(String emergencyType, String description, String aiResponse) {
        // Deprecated - now using saveOrUpdateEmergencyReport
        saveOrUpdateEmergencyReport(emergencyType, description, aiResponse);
    }

    private String extractAddressFromConversation(String conversation) {
        // Simple address extraction - looks for common address patterns
        String lowerConv = conversation.toLowerCase();
        
        Log.d(TAG, "Extracting address from: " + conversation);
        
        // Check for explicit address mentions with colon
        if (lowerConv.contains("address:")) {
            int startIdx = lowerConv.indexOf("address:") + 8;
            String remaining = conversation.substring(startIdx);
            String address = remaining.split("[\\n.!?]")[0].trim();
            Log.d(TAG, "Found 'address:' pattern - extracted: " + address);
            if (address.length() > 3 && address.length() < 200) {
                return address;
            }
        }
        
        if (lowerConv.contains("location:")) {
            int startIdx = lowerConv.indexOf("location:") + 9;
            String remaining = conversation.substring(startIdx);
            String address = remaining.split("[\\n.!?]")[0].trim();
            Log.d(TAG, "Found 'location:' pattern - extracted: " + address);
            if (address.length() > 3 && address.length() < 200) {
                return address;
            }
        }
        
        // Check for "I'm at [location]" or "at [location]" patterns
        if (lowerConv.contains(" at ")) {
            String[] parts = conversation.split("(?i) at ", 2);
            if (parts.length > 1) {
                // Get the part after "at" and clean it up
                String possibleAddress = parts[1].split("[.!?\\n]")[0].trim();
                // Remove common phrases after the address
                possibleAddress = possibleAddress.split("(?i),? (and |with |please |help )")[0].trim();
                Log.d(TAG, "Found 'at' pattern - extracted: " + possibleAddress);
                if (possibleAddress.length() > 5 && possibleAddress.length() < 200) {
                    return possibleAddress;
                }
            }
        }
        
        // Look for "I'm in [location]" or "in [location]" patterns
        if (lowerConv.contains(" in ")) {
            String[] parts = conversation.split("(?i) in ", 2);
            if (parts.length > 1) {
                String possibleAddress = parts[1].split("[.!?\\n]")[0].trim();
                possibleAddress = possibleAddress.split("(?i),? (and |with |please |help )")[0].trim();
                Log.d(TAG, "Found 'in' pattern - extracted: " + possibleAddress);
                if (possibleAddress.length() > 5 && possibleAddress.length() < 200 && 
                    !possibleAddress.toLowerCase().matches("(trouble|danger|need|pain).*")) {
                    return possibleAddress;
                }
            }
        }
        
        // Look for "near [landmark]" pattern
        if (lowerConv.contains(" near ")) {
            String[] parts = conversation.split("(?i) near ", 2);
            if (parts.length > 1) {
                String possibleAddress = parts[1].split("[.!?\\n]")[0].trim();
                Log.d(TAG, "Found 'near' pattern - extracted: " + possibleAddress);
                if (possibleAddress.length() > 3 && possibleAddress.length() < 200) {
                    return "Near " + possibleAddress;
                }
            }
        }
        
        // Look for street/building patterns (e.g., "123 Main Street", "Building A")
        if (lowerConv.matches(".*(\\d+\\s+[a-z]+\\s+(street|st|avenue|ave|road|rd|drive|dr|boulevard|blvd)).*")) {
            String[] words = conversation.split("\\s+");
            StringBuilder address = new StringBuilder();
            boolean foundNumber = false;
            for (int i = 0; i < words.length; i++) {
                if (words[i].matches("\\d+")) {
                    foundNumber = true;
                    address.append(words[i]).append(" ");
                } else if (foundNumber) {
                    address.append(words[i]).append(" ");
                    if (words[i].toLowerCase().matches(".*(street|st|avenue|ave|road|rd|drive|dr|boulevard|blvd).*")) {
                        String extractedStreet = address.toString().trim();
                        Log.d(TAG, "Found street pattern - extracted: " + extractedStreet);
                        return extractedStreet;
                    }
                }
            }
        }
        
        Log.d(TAG, "No address pattern matched - returning empty");
        return ""; // No address found
    }
}
