package com.example.micromobil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ProfileManager profileManager;
    private LinearLayout profileListLayout;
    private Button addProfileButton;
    private String selectedProfileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profileManager = ProfileManager.getInstance(this);
        profileListLayout = findViewById(R.id.profile_list);
        addProfileButton = findViewById(R.id.addProfileButton);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView profileName = findViewById(R.id.profile_name);

        // Placeholder için örnek veriler
        profileImage.setImageResource(R.drawable.ic_profile_placeholder);
        profileName.setText("Name Surname");

        loadProfiles();

        addProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleBottomNavigationItemSelected(item);
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

        Log.d("MainActivity", "BottomNavigation item selected: " + id);

        if (id == R.id.action_home) {
            Log.d("MainActivity", "Home button clicked");
            finish();
            startActivity(getIntent());
            return true;
        } else if (id == R.id.action_search) {
            Log.d("MainActivity", "Search button clicked");
            return true;
        } else if (id == R.id.action_drink) {
            Log.d("MainActivity", "Drink button clicked");
            if (selectedProfileName == null) {
                Toast.makeText(this, "Lütfen önce bir profil seçin.", Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "No profile selected");
                return false;
            }
            Log.d("MainActivity", "Selected profile: " + selectedProfileName);
            Intent intent = new Intent(MainActivity.this, DrinkActivity.class);
            intent.putExtra("profileName", selectedProfileName);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_bluetooth_control) {
            Log.d("MainActivity", "Bluetooth Control button clicked");
            Intent intent = new Intent(MainActivity.this, BluetoothControlActivity.class);
            startActivity(intent);
            return true;
        }

        return false;
    }


    private void loadProfiles() {
        profileListLayout.removeAllViews();

        // Varsayılan "Home Profile" profilini ekle
        Profile homeProfile = new Profile("Home Profile", "Default", new ArrayList<>());
        addProfileButton(homeProfile);

        List<Profile> profiles = profileManager.getProfiles();
        for (Profile profile : profiles) {
            addProfileButton(profile);
        }

        // "Add Profile" butonunun her zaman en altta olmasını sağla
        if (addProfileButton.getParent() != null) {
            ((RelativeLayout) addProfileButton.getParent()).removeView(addProfileButton);
        }
        profileListLayout.addView(addProfileButton);
    }

    private void addProfileButton(Profile profile) {
        LinearLayout profileItemLayout = new LinearLayout(this);
        profileItemLayout.setOrientation(LinearLayout.HORIZONTAL);
        profileItemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));

        Button profileButton = new Button(this);
        profileButton.setText(profile.getName());
        profileButton.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1.0f
        ));
        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedProfileName = profile.getName(); // Profil seçildiğinde güncelle
                Intent intent = new Intent(MainActivity.this, ProfileDetailActivity.class);
                intent.putExtra("profileName", profile.getName());
                startActivity(intent);
            }
        });

        // "Home Profile" için silme ve düzenleme butonlarını eklemiyoruz
        if (!profile.getName().equals("Home Profile")) {
            ImageButton editButton = new ImageButton(this);
            editButton.setImageResource(R.drawable.ic_edit);
            editButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            editButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            editButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Edit profile action
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    intent.putExtra("profileName", profile.getName());
                    startActivityForResult(intent, 2);
                }
            });

            ImageButton deleteButton = new ImageButton(this);
            deleteButton.setImageResource(R.drawable.ic_delete);
            deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            deleteButton.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileManager.removeProfile(profile.getName());
                    loadProfiles();
                }
            });

            profileItemLayout.addView(editButton);
            profileItemLayout.addView(deleteButton);
        }

        profileItemLayout.addView(profileButton);
        profileListLayout.addView(profileItemLayout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String profileName = data.getStringExtra("profileName");
            Profile profile = new Profile(profileName, "Type", new ArrayList<>()); // Profile tipi eklenmeli
            profileManager.addProfile(profile);
            Log.d("ProfileManager", "Profile added: " + profile.getName());
            loadProfiles();
        } else if (requestCode == 2 && resultCode == RESULT_OK) {
            String oldProfileName = data.getStringExtra("oldProfileName");
            String newProfileName = data.getStringExtra("newProfileName");
            String newProfileType = data.getStringExtra("newProfileType");
            profileManager.updateProfile(oldProfileName, newProfileName, newProfileType, new ArrayList<>());
            loadProfiles();
        }
    }
}
