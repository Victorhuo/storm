package org.apache.storm.daemon.worker;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



public class SharedCache {
    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, Object>> cache;

    private ArrayList<ArrayList<Integer>> data = new ArrayList<>();
    Integer index = 0;

    public SharedCache() {
        cache = new ConcurrentHashMap<>();
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("/home/victorhuo/workspace/storm/output.txt"));
            bw.write("test1");
            bw.newLine();
            bw.write("start");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public ConcurrentHashMap<String, Object> setStateKey(Integer taskId) {
        cache.putIfAbsent(taskId, new ConcurrentHashMap<>());
        return cache.get(taskId);
    }

    public void remove(Integer taskId) {
        cache.remove(taskId);
    }

    public ConcurrentHashMap<Integer, ConcurrentHashMap<String, Object>> getCache() {
        return cache;
    }

    public void dateStore() {
        ArrayList<Integer> tick = new ArrayList<>();
        tick.add(index++);
        int memoryNum = 0;
        for (Map.Entry<Integer, ConcurrentHashMap<String, Object>> entry : cache.entrySet()) {
            memoryNum += entry.getValue().size();
        }
        tick.add(memoryNum);
        data.add(tick);
        if (index == 60) {
            writeToFile(data, "/home/victorhuo/workspace/storm/output.txt");
            data.clear();
            index = 0;
        }
    }

    private static void writeToFile(ArrayList<ArrayList<Integer>> data, String fileName) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(fileName, true));

            for (ArrayList<Integer> list : data) {
                for (Integer i : list) {
                    bw.write(i.toString() + " ");
                }
                bw.newLine();  // Empty line to separate different ArrayLists
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}