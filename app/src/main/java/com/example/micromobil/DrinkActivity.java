package com.example.micromobil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class DrinkActivity extends AppCompatActivity {
    private ProfileManager profileManager;
    private LinearLayout drinkListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        Log.d("DrinkActivity", "DrinkActivity started successfully");

        profileManager = ProfileManager.getInstance(this);
        drinkListLayout = findViewById(R.id.drink_list_layout);
        Log.d("DrinkActivity", "drinkListLayout found");

        loadDrinks();
        Log.d("DrinkActivity", "loadDrinks called");

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_home) {
                Log.d("DrinkActivity", "Home button clicked");
                finish();
                return true;
            } else if (id == R.id.action_search) {
                Log.d("DrinkActivity", "Search button clicked");
                return true;
            } else if (id == R.id.action_drink) {
                Log.d("DrinkActivity", "Already in drink activity");
                return true;
            }
            return false;
        });
    }


    private void loadDrinks() {
        drinkListLayout.removeAllViews();
        Log.d("DrinkActivity", "loadDrinks: profiles size = " + profileManager.getProfiles().size());

        List<Profile> profiles = profileManager.getProfiles();
        for (Profile profile : profiles) {
            Log.d("DrinkActivity", "Profile: " + profile.getName());
            for (String drink : profile.getDrinks()) {
                Log.d("DrinkActivity", "Drink: " + drink);
                View drinkView = getLayoutInflater().inflate(R.layout.item_drink, drinkListLayout, false);

                TextView drinkNameTextView = drinkView.findViewById(R.id.drink_name);
                LinearLayout temperatureLayout = drinkView.findViewById(R.id.temperature_layout);

                drinkNameTextView.setText(drink);

                List<Integer> temperatures = profile.getTemperaturesForDrink(drink);
                for (Integer temperature : temperatures) {
                    Log.d("DrinkActivity", "Temperature: " + temperature);
                    View tempView = getLayoutInflater().inflate(R.layout.item_temperature, temperatureLayout, false);
                    TextView tempTextView = tempView.findViewById(R.id.temperature_value);
                    tempTextView.setText(String.valueOf(temperature));
                    temperatureLayout.addView(tempTextView);
                }

                drinkListLayout.addView(drinkView);
            }
        }
    }



}
