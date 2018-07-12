package com.netease.nim.demo.utils;

import android.os.Looper;
import android.util.Printer;

public class BlockDetectByPrinter {
    public static void start() {

        Looper.getMainLooper().setMessageLogging(new Printer() {

            private static final String START = ">>>>> Dispatching";
            private static final String END = "<<<<< Finished";

            @Override
            public void println(String x) {
                if (x.startsWith(START)) {
                    LogMonitor.getInstance().startMonitor();
                }
                if (x.startsWith(END)) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                LogMonitor.getInstance().removeMonitor();
                            }
                        }).start();
                }
            }
        });
    }


//    public static void start1() {
//        Choreographer.getInstance()
//                .postFrameCallback(new Choreographer.FrameCallback() {
//                    @Override
//                    public void doFrame(long l) {
//                        if (LogMonitor.getInstance().isMonitor()) {
//                            LogMonitor.getInstance().removeMonitor();
//                        }
//                        LogMonitor.getInstance().startMonitor();
//                        Choreographer.getInstance().postFrameCallback(this);
//                    }
//                });
//    }
}
