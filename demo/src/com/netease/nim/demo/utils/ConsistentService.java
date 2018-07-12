package com.netease.nim.demo.utils;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.netease.nim.demo.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ConsistentService extends Service implements MediaPlayer.OnCompletionListener {

    final String TAG = "ConsistentService";
//    private int batteryLevel;
    private MediaPlayer mediaPlayer;
//    private boolean mPausePlay= false;

    private String currentFile = null;
    public static FileWriter writer;
    public static String filePath;

    CpuRateUtil cpuRateUtil = new CpuRateUtil();
    Map<String, String> cpuInfoSnapshot_1;
    Map<String, String> cpuInfoSnapshot_2;

    /*通过后台播放低解析媒体来实现保活，效果很好且比多服务保活消耗低*/
    @Override
    public void onCompletion(MediaPlayer mp) {
//        playMedia();
    }

//    private void initMedia() {
//        if (mediaPlayer == null) {
//            mediaPlayer = MediaPlayer.create(this, R.raw.help);
//            mediaPlayer.setVolume(0f, 0f);
//            mediaPlayer.setOnCompletionListener(this);
//        }
//    }

//    private void playMedia() {
//        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
//            mediaPlayer.start();
//        }
//    }

//    private void pauseMedia() {
//        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//            mediaPlayer.pause();
//        }
//        mPausePlay = true;
//    }


    private void startNotification(String title, String text) {
//        Intent intent = new Intent(this, PageControler.);
//        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        Notification notification = null;

            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.drawable.logo)
                    .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.logo))
//                    .setContentIntent(pi)
                    .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(1, notification);
    }

    private String getStorageDir() {
//        Log.e("Path", this.getExternalFilesDir(null).getAbsolutePath());
        return this.getExternalFilesDir(null).getAbsolutePath();
    }

    private void createFileAndInitWriter() {
        PackageManager pm = this.getPackageManager();
        String appName = getApplicationInfo().loadLabel(pm).toString();
        currentFile = android.os.Build.MODEL+'_'+appName;
        filePath = getStorageDir() + "/" + currentFile + ".txt";
        File file = new File(filePath);
        try {
            writer = new FileWriter(file, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFile() {
        SharedPreferences pref = getSharedPreferences("data", MODE_PRIVATE);
        int times = pref.getInt("Times", 0);
        try {
            if (times == 0) {
                writer.write(String.format("---Time---\t"));
                writer.write(String.format("CPU\t"));
                writer.write(String.format("Frames\t"));
                writer.write("\r\n");
                ++times;
                Log.e("Times","-----------------------"+times+"----------------------");
                SharedPreferences.Editor editor = getSharedPreferences("data", MODE_PRIVATE).edit();
                editor.putInt("Times", times);
                editor.commit();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean upload(String filePath) throws FileNotFoundException {

        String server_upload_path = "http://192.168.137.1/MobilePhoneCpuRateReceiver/FileUpLoadServlet";
        final File file = new File(filePath);

        if (file.exists() && file.length() > 0) {
            OkHttpClient client = new OkHttpClient();
            MediaType type = MediaType.parse("txt");
            RequestBody fileBody = RequestBody.create(type, file);
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("tempFile", file.getName(), fileBody)
                    .build();
            Request request = new Request.Builder()
                    .url(server_upload_path)
                    .post(requestBody)
                    .build();
            Call call = client.newCall(request);
            call.enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
//                    Log.d(TAG, "failure upload!");
//                    Toast.makeText(getApplicationContext(), "failure upload!", Toast.LENGTH_SHORT);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
//                    Log.d(TAG, "success upload!");
//                    Toast.makeText(getApplicationContext(), "success upload!", Toast.LENGTH_SHORT);
                    file.delete();
                }
            });
        }
        return true;
    }


    Timer timer;
    //timer是一个可以每隔一段时间运行一次

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
                //要定期执行的任务
            cpuInfoSnapshot_1 = cpuRateUtil.cpuInfoSnapshot();
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cpuInfoSnapshot_2 = cpuRateUtil.cpuInfoSnapshot();
            double cpuRate = cpuRateUtil.getAvgCpuRateOnObejct(cpuInfoSnapshot_1,cpuInfoSnapshot_2);
            Log.e("CPU Rate","------------------"+cpuRate+"---------------------");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startNotification("Title","this is text");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        initMedia();
//        playMedia();

        createFileAndInitWriter();
        initFile();

//        timer = new Timer(true);
//        MyTimerTask myTimerTask = new MyTimerTask();
//        timer.schedule(myTimerTask, 100, 999);
        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            upload(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        stopForeground(true);
//        mediaPlayer.stop();
//        timer.cancel();
    }
}
