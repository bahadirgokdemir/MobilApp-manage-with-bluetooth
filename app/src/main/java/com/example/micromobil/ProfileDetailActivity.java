package com.example.micromobil;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ProfileDetailActivity extends AppCompatActivity {
    private String profileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_detail);

        profileName = getIntent().getStringExtra("profileName");
        TextView profileTitle = findViewById(R.id.profile_title);
        profileTitle.setText(profileName);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleBottomNavigationItemSelected(item);
            }
        });

        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Menü butonuna tıklanınca menü seçeneklerini göster
                openOptionsMenu();
            }
        });

        Button addDrinkButton = findViewById(R.id.add_drink_button);
        addDrinkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileDetailActivity.this, DrinkActivity.class);
                intent.putExtra("profileName", profileName);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "Ayarlar", Toast.LENGTH_SHORT).show();
            // Ayarlar ekranına git
            return true;
        } else if (id == R.id.action_about) {
            Toast.makeText(this, "Hakkında", Toast.LENGTH_SHORT).show();
            // Hakkında ekranına git
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean handleBottomNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_home) {
            // Ana sayfaya dön
            Intent intent = new Intent(ProfileDetailActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_search) {
            // Search action
            return true;
        } else if (id == R.id.action_drink) {
            // Drink action
            Intent intent = new Intent(ProfileDetailActivity.this, DrinkActivity.class);
            intent.putExtra("profileName", profileName);
            startActivity(intent);
            return true;
        }

        return false;
    }
}
