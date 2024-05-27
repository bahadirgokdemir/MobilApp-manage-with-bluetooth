package com.example.micromobil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private ProfileManager profileManager;
    private String oldProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileManager = ProfileManager.getInstance();

        EditText profileNameEditText = findViewById(R.id.profileName);
        EditText profileTypeEditText = findViewById(R.id.profileType);
        Button saveProfileButton = findViewById(R.id.saveProfileButton);

        Intent intent = getIntent();
        if (intent.hasExtra("profileName")) {
            oldProfileName = intent.getStringExtra("profileName");
            Profile profile = profileManager.getProfile(oldProfileName);
            if (profile != null) {
                profileNameEditText.setText(profile.getName());
                profileTypeEditText.setText(profile.getType());
            }
        }

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String profileName = profileNameEditText.getText().toString();
                String profileType = profileTypeEditText.getText().toString();

                if (!profileName.isEmpty() && !profileType.isEmpty()) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("profileName", profileName);

                    if (oldProfileName != null) {
                        resultIntent.putExtra("oldProfileName", oldProfileName);
                        resultIntent.putExtra("newProfileName", profileName);
                        resultIntent.putExtra("newProfileType", profileType);
                    }

                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.bottom_navigation_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return handleBottomNavigationItemSelected(item);
    }

    private boolean handleBottomNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            Intent intentHome = new Intent(ProfileActivity.this, MainActivity.class);
            intentHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentHome);
            finish();
            return true;
        } else if (id == R.id.action_search) {
            // Search action
            return true;
        } else if (id == R.id.action_drink) {
            // Drink action
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
