package com.dante.diary.custom;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.dante.diary.base.App.context;

/**
 * Created by yons on 16/8/12.
 */
public class RecordHelper {

    public static final String DIR_TEMP = "timepill_record";
    private static final String TAG = "RecordHelper";
    public static long start;
    public static long stop;
    private static MediaRecorder recorder = new MediaRecorder();
    private static String filePath;
    private static File recordFile;
    private static boolean isRecord;
    private static MediaPlayer mediaPlayer = new MediaPlayer();

    private static File prepareFile() {
        File dir = new File(context.getCacheDir(), DIR_TEMP);
        if (!dir.exists()) {
            boolean success = dir.mkdirs();
            Log.i(TAG, "create Record directory: " + String.valueOf(success));
        }
        DateFormat format = new SimpleDateFormat("MM-dd_HHmmss", Locale.getDefault());
        String time = format.format(new Date());
        return new File(dir, "record_" + time + ".mp3");
    }

    public static File createFile() {
        File recordFile = prepareFile();
        if (!recordFile.exists()) {
            try {
                boolean success = recordFile.createNewFile();
                Log.i(TAG, "createFile: " + recordFile.getName() + " " + String.valueOf(success));
            } catch (IOException e) {
                Log.e(TAG, "createFile: failed " + recordFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
        return recordFile;
    }

    public static void clearCache() {
        deleteDirectory(DIR_TEMP);
        recordFile = null;
    }

    private static void deleteDirectory(String name) {
        if (context == null) {
            return;
        }
        try {
            File dir = new File(context.getCacheDir(), name);
            for (File file :
                    dir.listFiles()) {
                boolean s = file.delete();
                if (!s) {
                    Log.i(TAG, "clearCache: delete failed");
                }
            }
            dir.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void record() {
        start = System.currentTimeMillis();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        recorder.setAudioSamplingRate(16000);
        recorder.setAudioChannels(1);
        recordFile = createFile();
        recorder.setOutputFile(recordFile.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
            isRecord = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static File getRecordFile() {
        return recordFile;
    }

    public static void stopRecord() {
        if (!isRecord) {
            return;
        }

        stop = System.currentTimeMillis();
        try {
            if (stop - start < 1000) {
                recordFile.delete();
                recorder.reset();
                return;
            }

            if (recorder != null) {
                new Handler().postDelayed(() -> {
                    recorder.stop();
                    recorder.reset();
                    isRecord = false;
                }, 500);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static double playAudio(File audioFile) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        double seconds = 0;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(audioFile);
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
            int duration = mediaPlayer.getDuration();
            if (duration <= 0) {
                return 0;
            }
            seconds = duration / 1000.0;
            Log.i(TAG, "playAudio: OK");
            Log.i(TAG, "getDuration: duration-" + seconds);
        } catch (IOException e) {
            Log.i(TAG, "playAudio: failed");
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {
                }
            }

        }
        return seconds;
    }

    public static int playAudio(String audioUrl, RecordView record) {
        int seconds = 0;
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> record.endRecord());
        } catch (IOException e) {
            e.printStackTrace();
        }
        int duration = mediaPlayer.getDuration();
        if (duration <= 0) {
            return 0;
        }
        seconds = duration / 1000;
        Log.i(TAG, "playAudio: OK");
        Log.i(TAG, "getDuration: duration-" + seconds);
        return seconds;
    }

    public static void stopPlay() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public static int getDuration() {
        FileInputStream fis = null;
        int seconds = 0;
        try {
            fis = new FileInputStream(getRecordFile());
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fis.getFD());
            mediaPlayer.prepare();
            int duration = mediaPlayer.getDuration();
            if (duration <= 0) {
                return 0;
            }
            seconds = duration / 1000;
            Log.i(TAG, "getDuration: duration " + duration / 1000 + " duration:" + duration);
        } catch (IOException e) {
            Log.i(TAG, "playAudio: failed");
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException ignore) {

                }
            }
        }
        return seconds;
    }
}
