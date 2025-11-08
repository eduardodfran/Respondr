package com.example.respondr;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView toolbarTitle;
    private ImageButton menuBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        menuBtn = findViewById(R.id.menuBtn);

        menuBtn.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_chat);
            switchToFragment(new ChatFragment(), getString(R.string.menu_chat));
        }
    }

        @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_chat) {
            switchToFragment(new ChatFragment(), "Chat");
        } else if (id == R.id.nav_history) {
            switchToFragment(new HistoryFragment(), "History");
        } else if (id == R.id.nav_test_report) {
            // Open Test Report Activity
            Intent intent = new Intent(this, TestReportActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            // TODO: Implement Settings
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void switchToFragment(Fragment fragment, String title) {
        toolbarTitle.setText(title);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        super.onBackPressed();
    }

    public void setToolbarTitle(String title) {
        toolbarTitle.setText(title);
    }
}