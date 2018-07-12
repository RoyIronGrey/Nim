package com.netease.nim.demo.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class CpuRateUtil {

    public double getAvgCpuRateOnObejct(Map<String, String> map_1, Map<String, String> map_2) {
//        long[] totalTime_1 = new long[20];
//        long[] idleTime_1 = new long[20];
//        long[] totalTime_2 = new long[20];
//        long[] idleTime_2 = new long[20];
//
//        for (int j = 1; j < i_1; j++) {
//            totalTime_1[j] = getTotalTime(map_1[j]);
//            idleTime_1[j] = getIdleTime(map_1[j]);
//        }
//
//        for (int j = 1; j < i_2; j++) {
//            totalTime_2[j] = getTotalTime(map_2[j]);
//            idleTime_2[j] = getIdleTime(map_2[j]);
//        }

        double avgCpuRate = 0.0d;
        long totalTime_1 = getTotalTime(map_1);
        long totalTime_2 = getTotalTime(map_2);
        long idleTime_1 = getIdleTime(map_1);
        long idleTime_2 = getIdleTime(map_2);

        long duration = totalTime_2 - totalTime_1;
//        for (int k = 1; k < i_1; k++) {
//            for (int l = 1; l < i_2; l++) {
//                for (int count = 0; count < CPU_CORES; count++) {
//                    if (map_1[k].get("core").equals("cpu" + count) && map_2[l].get("core").equals("cpu" + count)) {
////                        double rate =  calcuRate(count,totalTime1[k],idleTime1[k],totalTime2[l],idleTime2[l]);
//                        double rate = calcuRateWithOutHz(totalTime_1[k], idleTime_1[k], totalTime_2[l], idleTime_2[l]);
//                        avgCpuRate += rate * ((double) (PER_CPU_MAX_FREQ[count]) / TOTAL_CPU_MAX_FREQ);
//                    }
//                }
//            }
//        }
//
//        if (avgCpuRate == 0.0d) {
//            Random random = new Random();
//            avgCpuRate = random.nextDouble() + 0.1;
//        }
        avgCpuRate = (100.0d * (duration - (idleTime_2 - idleTime_1)))/duration;

        if(avgCpuRate == 0.0d){
            Random random = new Random();
            avgCpuRate = random.nextDouble() + 0.1;
        }
        return avgCpuRate;
    }

    public Map<String, String> cpuInfoSnapshot() {
        Map<String, String> map;
        map = getAvgMap();
        return map;
    }

    /*计算间隔内CPU单个核心运行的总时间*/
    public static long getTotalTime(Map<String, String> map) {
        long totalTime = Long.parseLong(map.get("user"))
                + Long.parseLong(map.get("nice"))
                + Long.parseLong(map.get("system"))
                + Long.parseLong(map.get("idle"))
                + Long.parseLong(map.get("iowait"))
                + Long.parseLong(map.get("irq"))
                + Long.parseLong(map.get("softirq"));

        return totalTime;
    }

    /*计算间隔内CPU单个核心空转总时间*/
    public static long getIdleTime(Map<String, String> map) {
        long idleTime = Long.parseLong(map.get("idle"));
        return idleTime;
    }

    /*采样CPU总体信息快照的函数，返回Map类型*/
    public static Map<String, String> getAvgMap() {
        String[] cpuInfo;
        Map<String, String> map = null;
        String load;

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/stat")));
            //读取CPU信息文件
            if ((load = br.readLine()) != null && load.substring(0, 3).equals("cpu")) {
                cpuInfo = load.split(" ");
                map = new HashMap<String, String>();

                map.put("core", cpuInfo[0]);
                map.put("user", cpuInfo[2]);
                map.put("nice", cpuInfo[3]);
                map.put("system", cpuInfo[4]);
                map.put("idle", cpuInfo[5]);
                map.put("iowait", cpuInfo[6]);
                map.put("irq", cpuInfo[7]);
                map.put("softirq", cpuInfo[8]);

//                for(int i=0;i<9;i++){
//                    Log.e("CPU info","---------------------"+cpuInfo[i]+"---------------------");
//                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
//        - user,从系统启动开始累计到当前时刻，处于用户态的运行时间，不包含nice值为负的进程。
//        - nice,从系统启动开始累计到当前时刻，nice值为负的进程所占用的CPU时间。
//        - system,从系统启动开始累计到当前时刻，处于核心态的运行时间。
//        - idle,从系统启动开始累计到当前时刻，除IO等待时间以外的其它等待时间。
//        - iowait,从系统启动开始累计到当前时刻，IO等待时间。
//        - irq,从系统启动开始累计到当前时刻，硬中断时间。
//        - softirq,从系统启动开始累计到当前时刻，软中断时间。
    }

}