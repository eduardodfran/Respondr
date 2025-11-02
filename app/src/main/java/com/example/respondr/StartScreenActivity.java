package com.example.respondr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_screen);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        
        btnGetStarted.setOnClickListener(v -> {
            // Navigate to MainActivity
            Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); // Close start screen so back button doesn't return here
        });
    }
}
