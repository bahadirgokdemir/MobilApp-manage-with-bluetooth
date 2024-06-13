package com.example.micromobil;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class DrinkActivity extends AppCompatActivity {

    private LinearLayout drinkListLayout;
    private ProfileManager profileManager;

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler;
    private byte[] buffer;
    private int bytes;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Spinner deviceSpinner;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<String> deviceArrayList;
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList;
    private BluetoothDevice selectedDevice;
    private TextView responseTextView;
    private Button btnStop;
    private Button btnConnect;

    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);
        lineChart = findViewById(R.id.lineChart);


        drinkListLayout = findViewById(R.id.drink_list_layout);
        profileManager = ProfileManager.getInstance(this);
        deviceSpinner = findViewById(R.id.device_spinner);
        btnStop = findViewById(R.id.btnStop);
        btnConnect = findViewById(R.id.connect_button);
        responseTextView = findViewById(R.id.response_text);

        deviceArrayList = new ArrayList<>();
        bluetoothDeviceArrayList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, deviceArrayList);
        deviceListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        deviceSpinner.setAdapter(deviceListAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth desteklenmiyor", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkPermissions();

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnConnect.setEnabled(false);
                btnConnect.setText("Bağlanıyor...");
                int position = deviceSpinner.getSelectedItemPosition();
                if (position != AdapterView.INVALID_POSITION) {
                    selectedDevice = bluetoothDeviceArrayList.get(position);
                    setupBluetoothConnection();
                } else {
                    Toast.makeText(DrinkActivity.this, "Lütfen bir cihaz seçin", Toast.LENGTH_SHORT).show();
                    btnConnect.setEnabled(true);
                    btnConnect.setText("Bağlan");
                }
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("3");
            }
        });

        handler = new Handler();
        buffer = new byte[1024];

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                return handleBottomNavigationItemSelected(item);
            }
        });

        loadDrinks();
        setupChart();
    }

    private boolean handleBottomNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        Log.d("DrinkActivity", "BottomNavigation item selected: " + id);

        if (id == R.id.action_home) {
            finish();
            return true;
        } else if (id == R.id.action_drink) {
            // Drink işlemleri
            return true;
        }
        return false;
    }

    private void loadDrinks() {
        drinkListLayout.removeAllViews();
        drinkListLayout.setVisibility(View.VISIBLE);
        responseTextView.setVisibility(View.VISIBLE);

        Log.d("DrinkActivity", "loadDrinks: profiles size = " + profileManager.getProfiles().size());

        List<Profile> profiles = profileManager.getProfiles();
        Map<String, Profile> profileMap = new HashMap<>();

        for (Profile profile : profiles) {
            if (!profileMap.containsKey(profile.getName())) {
                profileMap.put(profile.getName(), profile);
            } else {
                Profile existingProfile = profileMap.get(profile.getName());
                if (!existingProfile.getDrinks().isEmpty()) {
                    profileMap.put(profile.getName(), existingProfile);
                } else if (!profile.getDrinks().isEmpty()) {
                    profileMap.put(profile.getName(), profile);
                }
            }
        }

        for (Profile profile : profileMap.values()) {
            if (profile.getDrinks().isEmpty()) {
                continue;
            }

            Log.d("DrinkActivity", "Profile: " + profile.getName());

            TextView profileNameTextView = new TextView(this);
            profileNameTextView.setText(profile.getName());
            profileNameTextView.setTextSize(20);
            profileNameTextView.setTypeface(null, Typeface.BOLD);
            profileNameTextView.setPadding(0, 16, 0, 8);

            View lineView = new View(this);
            lineView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            lineView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray));

            drinkListLayout.addView(profileNameTextView);
            drinkListLayout.addView(lineView);

            for (String drink : profile.getDrinks()) {
                Log.d("DrinkActivity", "Drink: " + drink);
                View drinkView = getLayoutInflater().inflate(R.layout.item_drink, null);

                TextView drinkNameTextView = drinkView.findViewById(R.id.drink_name);
                TextView temperatureValueTextView = drinkView.findViewById(R.id.temperature_value);
                Button increaseButton = drinkView.findViewById(R.id.button_increase);
                Button decreaseButton = drinkView.findViewById(R.id.button_decrease);

                drinkNameTextView.setText(drink);

                List<Integer> temperatures = profile.getTemperaturesForDrink(drink);
                if (!temperatures.isEmpty()) {
                    int currentTemperature = temperatures.get(0);
                    temperatureValueTextView.setText(String.valueOf(currentTemperature));
                }

                increaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTemperatureInputDialog(temperatureValueTextView, true);
                    }
                });

                decreaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTemperatureInputDialog(temperatureValueTextView, false);
                    }
                });

                drinkListLayout.addView(drinkView);
            }
        }
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            listPairedDevices();
        }
    }

    private void listPairedDevices() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Bluetooth izinlerine ihtiyaç var", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            }, REQUEST_BLUETOOTH_PERMISSIONS);
            return;
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                deviceArrayList.add(device.getName() + "\n" + device.getAddress());
                bluetoothDeviceArrayList.add(device);
            }
            deviceListAdapter.notifyDataSetChanged();
        }
    }

    private void setupBluetoothConnection() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }

            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bluetoothSocket = selectedDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            Log.d("DrinkActivity", "setupBluetoothConnection: " + selectedDevice.getName());

            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();
                inputStream = bluetoothSocket.getInputStream();
                Toast.makeText(this, "Bağlantı kuruldu: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();

                Intent serviceIntent = new Intent(this, BluetoothService.class);
                startService(serviceIntent);

                new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                bytes = inputStream.read(buffer);
                                final String incomingMessage = new String(buffer, 0, bytes);
                                handler.post(new Runnable() {
                                    public void run() {
                                        responseTextView.setText(incomingMessage);
                                    }
                                });
                            } catch (IOException e) {
                                handler.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(DrinkActivity.this, "Bağlantı kesildi", Toast.LENGTH_SHORT).show();
                                        responseTextView.setText("");
                                        btnConnect.setEnabled(true);
                                        btnConnect.setText("Bağlan");
                                    }
                                });
                                break;
                            }
                        }
                    }
                }).start();
            } catch (IOException connectException) {
                try {
                    bluetoothSocket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                connectException.printStackTrace();
                Toast.makeText(this, "Bağlantı başarısız", Toast.LENGTH_SHORT).show();
                btnConnect.setEnabled(true);
                btnConnect.setText("Bağlan");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Bağlantı başarısız", Toast.LENGTH_SHORT).show();
            btnConnect.setEnabled(true);
            btnConnect.setText("Bağlan");
        }
    }

    private void sendCommand(String command) {
        if (outputStream != null) {
            try {
                outputStream.write(command.getBytes());
                Toast.makeText(this, "Komut gönderildi", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Komut gönderilemedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showTemperatureInputDialog(TextView temperatureValueTextView, boolean isIncrease) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isIncrease ? "Artırmak için sıcaklık değeri girin" : "Azaltmak için sıcaklık değeri girin");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Gönder", (dialog, which) -> {
            String temperature = input.getText().toString();
            int currentTemp = Integer.parseInt(temperatureValueTextView.getText().toString());
            int newTemp = isIncrease ? currentTemp + Integer.parseInt(temperature) : currentTemp - Integer.parseInt(temperature);
            temperatureValueTextView.setText(String.valueOf(newTemp));
            sendTemperature(String.valueOf(newTemp));
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void sendTemperature(String temperature) {
        if (outputStream != null) {
            try {
                outputStream.write(temperature.getBytes());
                Toast.makeText(this, "Sıcaklık gönderildi", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Sıcaklık gönderilemedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                listPairedDevices();
            } else {
                Toast.makeText(this, "Bluetooth izinleri reddedildi", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupChart() {
        List<String> drinkNames = new ArrayList<>();
        drinkNames.add("Kola");
        drinkNames.add("Soğuk Çay");
        drinkNames.add("Kahve");

        List<Entry> entries = new ArrayList<>();
        entries.add(new Entry(0, 25));
        entries.add(new Entry(1, 2));
        entries.add(new Entry(2, 60));

        LineDataSet dataSet = new LineDataSet(entries, "Drink Temperatures");
        dataSet.setColor(Color.BLUE);
        dataSet.setValueTextColor(Color.BLACK);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(drinkNames));
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        Description description = new Description();
        description.setText("Drink Temperatures");
        lineChart.setDescription(description);

        lineChart.invalidate();
    }
}
