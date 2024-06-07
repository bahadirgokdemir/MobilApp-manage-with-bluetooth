package com.example.micromobil;

import android.util.Log;
import com.arthenica.ffmpegkit.FFmpegKit;
import com.arthenica.ffmpegkit.FFmpegSession;
import com.arthenica.ffmpegkit.ReturnCode;

public class AudioConverter {

    public static boolean convert3gpToWav(String inputFilePath, String outputFilePath) {
        try {
            // FFmpeg komutunu oluştur ve overwrite özelliğini ekle
            String command = String.format("-y -i %s -acodec pcm_s16le -ar 44100 -ac 2 %s", inputFilePath, outputFilePath);

            Log.w("AudioConverter", "FFmpeg command: " + command);

            // FFmpeg'i çağır ve dönüşümü başlat
            FFmpegSession session = FFmpegKit.execute(command);

            // Dönüşüm sürecini kontrol et
            if (ReturnCode.isSuccess(session.getReturnCode())) {
                Log.i("AudioConverter", "Conversion successful");
                return true;  // Dönüşüm başarılı
            } else {
                Log.e("AudioConverter", "Conversion failed with return code: " + session.getReturnCode());
                Log.e("AudioConverter", "FFmpeg output: " + session.getOutput());
                return false;  // Dönüşüm başarısız
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // Dönüşüm sırasında bir hata oluştu
        }
    }
}
