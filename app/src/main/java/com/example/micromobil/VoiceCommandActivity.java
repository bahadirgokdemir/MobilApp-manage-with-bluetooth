package com.example.micromobil;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.IOException;

import android.media.MediaRecorder;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
public class VoiceCommandActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;
    private MediaRecorder recorder;
    private TextView textViewResult;
    private String fileName = null;
    private Button recordButton = null;
    private boolean isRecording = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_command);

        recordButton = findViewById(R.id.recordButton);
        textViewResult = findViewById(R.id.textViewResult);

        fileName = getExternalFilesDir(Environment.DIRECTORY_MUSIC).getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        Log.d("VoiceCommandActivity", "Requesting permission");

        recordButton.setOnClickListener(v -> {
            Log.d("VoiceCommandActivity", "Record button clicked");
            if (isRecording) {
                Log.d("VoiceCommandActivity", "Stop recording");
                stopRecording();
                recordButton.setText("Start Recording");
            } else {
                Log.d("VoiceCommandActivity", "Start recording");
                startRecording();
                recordButton.setText("Stop Recording");
            }
            isRecording = !isRecording;
        });
    }

    private Retrofit createRetrofit() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {}

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)  // Bağlanma zaman aşımı süresi (60 saniye)
                    .readTimeout(60, TimeUnit.SECONDS)    // Okuma zaman aşımı süresi (60 saniye)
                    .writeTimeout(60, TimeUnit.SECONDS)  // Yazma zaman aşımı süresi (60 saniye)
                    .sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true;
                        }
                    }).build();

            return new Retrofit.Builder()
                    .baseUrl("https://192.168.0.11:5000/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("VoiceCommandActivity", "Recorder prepare failed", e);
            Toast.makeText(this, "Recorder prepare failed", Toast.LENGTH_SHORT).show();
        }

        try {
            recorder.start();
            Log.d("VoiceCommandActivity", "Recorder started");
        } catch (IllegalStateException e) {
            Log.e("VoiceCommandActivity", "Recorder start failed", e);
            Toast.makeText(this, "Recorder start failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        try {
            recorder.stop();
        } catch (RuntimeException stopException) {
            Log.e("VoiceCommandActivity", "Recorder stop failed", stopException);
            Toast.makeText(this, "Recorder stop failed", Toast.LENGTH_SHORT).show();
        }
        recorder.release();
        recorder = null;

        // .wav dosyasının adını ve yolunu belirle
        String wavFileName = fileName.replace(".3gp", ".wav");
        String wavFilePath = wavFileName;

        // .3gp dosyasını .wav formatına dönüştür
        if (AudioConverter.convert3gpToWav(fileName, wavFilePath)) {
            Log.d("VoiceCommandActivity", "Dönüşüm başarıyla tamamlandı!");
            // Dönüşüm başarılı ise yükleme işlemini gerçekleştir
            uploadAudioFile(wavFilePath);
        } else {
            Log.e("VoiceCommandActivity", "Dönüşüm başarısız!");
            // Dönüşüm başarısız olduğunda uygun bir işlem yapabilirsiniz
        }
    }

    private void uploadAudioFile(String audioFilePath) {
        Retrofit retrofit = createRetrofit();
        ApiService apiService = retrofit.create(ApiService.class);

        File audioFile = new File(audioFilePath);
        Log.d("VoiceCommandActivity", "File audioFile adı: " + audioFile.getName());

        // Dosya yolunu içeren RequestBody oluştur
        RequestBody filePath = RequestBody.create(MediaType.parse("text/plain"), audioFilePath);

        // Dosyanın kendisini içeren RequestBody oluştur
        RequestBody requestFile = RequestBody.create(MediaType.parse("audio/wav"), audioFile);
        Log.d("VoiceCommandActivity", "request file türü: " + requestFile.contentType());

        // MultipartBody.Part oluştur
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", audioFile.getName(), requestFile);
        Log.d("VoiceCommandActivity", "request body türü: " + body.body().contentType());

        // API çağrısını gerçekleştir
        Call<ResponseBody> call = apiService.uploadAudio(filePath, body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        Log.d("VoiceCommandActivity", "Response successful");
                        String result = response.body().string();
                        Log.d("VoiceCommandActivity", "Response body: " + result);

                        textViewResult.setText(result);  // Yanıtı TextView'de göster
                        Toast.makeText(VoiceCommandActivity.this, "Text: " + result, Toast.LENGTH_LONG).show();
                    } catch (IOException e) {
                        Log.e("VoiceCommandActivity", "Response reading failed: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    Log.e("VoiceCommandActivity", "Upload failed on response else: " + response.message());
                    textViewResult.setText("Failed to upload: " + response.message());
                    Log.e("VoiceCommandActivity", "Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("VoiceCommandActivity", "Upload failed: " + t.getMessage());
                textViewResult.setText("Upload failed on failure: " + t.getMessage());
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
