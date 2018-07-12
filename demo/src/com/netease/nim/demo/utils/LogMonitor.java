package com.netease.nim.demo.utils;

import android.os.Handler;
import android.os.HandlerThread;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class LogMonitor {
    private static LogMonitor sInstance = new LogMonitor();
    private HandlerThread mLogThread = new HandlerThread("log");
    CpuRateUtil cpuRateUtil = new CpuRateUtil();
    private Handler mIoHandler;
    private static final long TIME_BLOCK = 68L;
    private long startTime;
    Map<String, String> cpuInfoSnapshot_1;
    Map<String, String> cpuInfoSnapshot_2;


    private LogMonitor() {
        mLogThread.start();
        mIoHandler = new Handler(mLogThread.getLooper());
    }

//    private static Runnable mLogRunnable = new Runnable() {
//        @Override
//        public void run() {
////            StringBuilder sb = new StringBuilder();
////            StackTraceElement[] stackTrace = Looper.getMainLooper().getThread().getStackTrace();
////            for (StackTraceElement s : stackTrace) {
////                sb.append(s.toString() + "\n");
////            }
////            Log.e("TAG", sb.toString());
////            Log.e("Lagging","--------------------Lagging happend!------------------");
//        }
//    };

    public static LogMonitor getInstance() {
        return sInstance;
    }

//    public boolean isMonitor() {
//        return mIoHandler.hasCallbacks(mLogRunnable);
//    }

    public synchronized void startMonitor() {
//        mIoHandler.postDelayed(mLogRunnable, TIME_BLOCK);
        cpuInfoSnapshot_1 = cpuRateUtil.cpuInfoSnapshot();
        startTime = System.currentTimeMillis();
    }

    public synchronized void removeMonitor() {
//        mIoHandler.removeCallbacks(mLogRunnable);
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        if (duration > 10) {
//            Log.e("Duration", "--------------------" + (System.currentTimeMillis() - startTime + 16) / 17 + " Frames--------------------");
        cpuInfoSnapshot_2 = cpuRateUtil.cpuInfoSnapshot();
        double cpuRate = cpuRateUtil.getAvgCpuRateOnObejct(cpuInfoSnapshot_1, cpuInfoSnapshot_2);
        long Frames = ((duration + 16) / 17);
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss.SS");
        Date date = new Date(endTime);
        try {
            ConsistentService.writer.write(formatter.format(date) + '\t');
            ConsistentService.writer.write(String.format("%.1f%%\t", cpuRate));
            ConsistentService.writer.write(String.format("%d", Frames));
            ConsistentService.writer.write("\r\n");
            ConsistentService.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        }
    }
}
