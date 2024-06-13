package com.example.micromobil;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;

public class BluetoothService extends Service {

    private static final String CHANNEL_ID = "drink_notifications_channel";
    private BluetoothSocket bluetoothSocket;
    private InputStream inputStream;
    private Handler handler;
    private byte[] buffer;
    private int bytes;

    @Override
    public void onCreate() {
        super.onCreate();
        handler = new Handler();
        buffer = new byte[1024];
        createNotificationChannel();
    }

    public void setBluetoothSocket(BluetoothSocket socket) {
        this.bluetoothSocket = socket;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    try {
                        bytes = inputStream.read(buffer);
                        final String incomingMessage = new String(buffer, 0, bytes);
                        handler.post(new Runnable() {
                            public void run() {
                                Log.d("BluetoothService", "Received: " + incomingMessage);
                                sendNotification(incomingMessage);
                            }
                        });
                    } catch (IOException e) {
                        Log.e("BluetoothService", "Error reading from input stream", e);
                        break;
                    }
                }
            }
        }).start();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Drink Notifications";
            String description = "Notifications for drink status updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, DrinkActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Drink Status Update")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (bluetoothSocket != null) {
                bluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
