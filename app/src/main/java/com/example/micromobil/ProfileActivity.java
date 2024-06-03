package com.example.micromobil;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {
    private ProfileManager profileManager;
    private LinearLayout fieldsLayout;
    private List<View> fieldViews;
    private ArrayAdapter<String> drinkTypeAdapter;
    private List<String> drinkTypes;
    private boolean isProfileSaved = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profileManager = ProfileManager.getInstance(this);
        fieldsLayout = findViewById(R.id.fieldsLayout);
        fieldViews = new ArrayList<>();
        drinkTypes = new ArrayList<>();
        addDefaultDrinkTypes();

        EditText profileNameEditText = findViewById(R.id.profileName);
        Button saveProfileButton = findViewById(R.id.saveProfileButton);

        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isProfileSaved) {
                    return;
                }

                String profileName = profileNameEditText.getText().toString();

                if (profileName.isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter a profile name", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Profil zaten mevcut mu kontrol et
                if (profileManager.getProfile(profileName) != null) {
                    Toast.makeText(ProfileActivity.this, "Profile already exists", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, List<Integer>> drinkTemperatures = new HashMap<>();
                for (View fieldView : fieldViews) {
                    Spinner profileTypeSpinner = fieldView.findViewById(R.id.profileTypeSpinner);
                    EditText temperatureField = fieldView.findViewById(R.id.temperatureField);

                    String drinkType = profileTypeSpinner.getSelectedItem().toString();
                    String tempStr = temperatureField.getText().toString();
                    if (!tempStr.isEmpty()) {
                        int temperature = Integer.parseInt(tempStr);
                        if (!drinkTemperatures.containsKey(drinkType)) {
                            drinkTemperatures.put(drinkType, new ArrayList<>());
                        }
                        drinkTemperatures.get(drinkType).add(temperature);
                    }
                }

                Profile profile = new Profile(profileName, drinkTemperatures);
                profileManager.addProfile(profile);

                // Profile'ı dosyaya yaz
                writeProfileToFile(profile);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("profileName", profileName);
                setResult(RESULT_OK, resultIntent);
                isProfileSaved = true;
                finish();
            }
        });

        // Initial field
        addFields(null);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::handleBottomNavigationItemSelected);
    }

    private void addDefaultDrinkTypes() {
        drinkTypes.add("Kola");
        drinkTypes.add("Çay");
        drinkTypes.add("Kahve");
        drinkTypes.add("Soğuk Çay");
        drinkTypes.add("+ Ekle");  // + Ekle seçeneği
        drinkTypeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, drinkTypes);
        drinkTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    public void addFields(View view) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View fieldView = inflater.inflate(R.layout.field_item, null);

        Spinner profileTypeSpinner = fieldView.findViewById(R.id.profileTypeSpinner);
        profileTypeSpinner.setAdapter(drinkTypeAdapter);
        profileTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == drinkTypes.size() - 1) {  // "+ Ekle" seçeneğine tıklandıysa
                    showAddDrinkDialog(profileTypeSpinner);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Seçim yapılmadığında "Kola" varsayılan olarak seçilsin
                profileTypeSpinner.setSelection(0);
            }
        });

        ImageButton addButton = fieldView.findViewById(R.id.addDrinkButton);
        addButton.setOnClickListener(v -> addFields(null));

        ImageButton deleteButton = fieldView.findViewById(R.id.deleteDrinkButton);
        deleteButton.setOnClickListener(v -> removeField(fieldView));

        if (fieldsLayout.getChildCount() > 0) {
            deleteButton.setVisibility(View.VISIBLE);
        }

        fieldViews.add(fieldView);
        fieldsLayout.addView(fieldView);
    }

    private void removeField(View fieldView) {
        fieldsLayout.removeView(fieldView);
        fieldViews.remove(fieldView);
    }

    private void showAddDrinkDialog(Spinner profileTypeSpinner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yeni İçecek Ekle");

        final EditText input = new EditText(this);
        input.setHint("İçecek adı");

        builder.setView(input);
        builder.setPositiveButton("Ekle", (dialog, which) -> {
            String newDrink = input.getText().toString();
            if (!newDrink.isEmpty()) {
                // İçecek türü listesini güncelle
                drinkTypes.add(drinkTypes.size() - 1, newDrink);  // "+ Ekle" seçeneğinden önce ekle
                drinkTypeAdapter.notifyDataSetChanged();
                profileTypeSpinner.setSelection(drinkTypeAdapter.getPosition(newDrink));
            } else {
                profileTypeSpinner.setSelection(0); // Yeni içecek eklenmezse varsayılan seçimi ayarla
            }
        });
        builder.setNegativeButton("İptal", (dialog, which) -> {
            dialog.cancel();
            profileTypeSpinner.setSelection(0); // İptal edilirse varsayılan seçimi ayarla
        });

        builder.show();
    }

    private void writeProfileToFile(Profile profile) {
        try {
            // External storage directory
            File externalDir = new File(getExternalFilesDir(null), "profiles");
            if (!externalDir.exists()) {
                externalDir.mkdirs();
            }

            File profileFile = new File(externalDir, profile.getName() + ".dat");
            FileOutputStream fos = new FileOutputStream(profileFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(profile);
            oos.close();
            fos.close();

            Toast.makeText(this, "Profile saved to " + profileFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
        }
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
            Intent intentDrink = new Intent(ProfileActivity.this, DrinkActivity.class);
            intentDrink.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentDrink);
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isProfileSaved", isProfileSaved);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        isProfileSaved = savedInstanceState.getBoolean("isProfileSaved", false);
    }
}
