package com.example.micromobil;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class BluetoothControlActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Handler handler;
    private byte[] buffer;
    private int bytes;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private ListView deviceList;
    private ArrayAdapter<String> deviceListAdapter;
    private ArrayList<String> deviceArrayList;
    private ArrayList<BluetoothDevice> bluetoothDeviceArrayList;
    private BluetoothDevice selectedDevice;
    private TextView responseTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_control);

        deviceList = findViewById(R.id.device_list);
        Button btnHeating = findViewById(R.id.btnHeating);
        Button btnCooling = findViewById(R.id.btnCooling);
        Button btnStop = findViewById(R.id.btnStop);
        responseTextView = findViewById(R.id.response_text);

        deviceArrayList = new ArrayList<>();
        bluetoothDeviceArrayList = new ArrayList<>();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceArrayList);
        deviceList.setAdapter(deviceListAdapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth desteklenmiyor", Toast.LENGTH_SHORT).show();
            finish();
        }

        checkPermissions();

        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedDevice = bluetoothDeviceArrayList.get(position);
                setupBluetoothConnection();
            }
        });

        btnHeating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("2");
                showTemperatureInputDialog(); // Sıcaklık değeri girmek için diyalog göster
            }
        });

        btnCooling.setOnClickListener(v -> {
            sendCommand("1");
            showTemperatureInputDialog(); // Sıcaklık değeri girmek için diyalog göster
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCommand("3");
            }
        });

        handler = new Handler(Looper.getMainLooper());
        buffer = new byte[1024];
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
                return;
            }

            bluetoothSocket = selectedDevice.createRfcommSocketToServiceRecord(MY_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();
            Toast.makeText(this, "Bağlantı kuruldu: " + selectedDevice.getName(), Toast.LENGTH_SHORT).show();

            // Arduino'dan gelen veriyi okumak için bir thread başlat
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
                            break;
                        }
                    }
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Bağlantı başarısız", Toast.LENGTH_SHORT).show();
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

    private void showTemperatureInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sıcaklık Değeri Girin");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Gönder", (dialog, which) -> {
            String temperature = input.getText().toString();
            sendTemperature(temperature);
        });
        builder.setNegativeButton("İptal", (dialog, which) -> dialog.cancel());

        builder.show();
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
}
