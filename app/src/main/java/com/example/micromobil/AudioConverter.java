package com.example.micromobil;

import android.util.Log;

import java.io.IOException;

public class AudioConverter {
    public static boolean convert3gpToFlac(String inputFilePath, String outputFilePath) {
        try {
            // FFmpeg komutunu oluştur
            String[] command = {"ffmpeg", "-i", inputFilePath, "-c:a", "flac", outputFilePath};
            System.out.println("FFmpeg Command: " + String.join(" ", command));
            // FFmpeg'i çağır ve dönüşümü başlat
            Process process = new ProcessBuilder(command).start();

            // Dönüşüm sürecini bekle
            int exitCode = process.waitFor();

            return exitCode == 0;  // Dönüşüm başarılı ise true döndür
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;  // Dönüşüm başarısız ise false döndür
        }
    }

    public static void main(String[] args) {
        String inputFilePath = "input.3gp";
        String outputFilePath = "output.flac";

        if (convert3gpToFlac(inputFilePath, outputFilePath)) {
            System.out.println("Dönüşüm başarıyla tamamlandı!");
        } else {
            System.out.println("Dönüşüm başarısız!");
        }
    }
}
