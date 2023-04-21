package org.apache.storm.daemon.worker;

import java.util.concurrent.ConcurrentHashMap;

public class SharedCache {
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> cache;

    public SharedCache() {
        cache = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, Object> setStateKey(String name) {
        cache.putIfAbsent(name, new ConcurrentHashMap<>());
        return cache.get(name);
    }

    public void remove(String name) {
        cache.remove(name);
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> getCache() {
        return cache;
    }
}