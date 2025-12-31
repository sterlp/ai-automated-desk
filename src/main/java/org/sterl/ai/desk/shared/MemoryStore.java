package org.sterl.ai.desk.shared;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MemoryStore<T> {

    private final Lock lock = new ReentrantLock();
    private final int maxElements;

    private final Map<String, T> data = new LinkedHashMap<>() {
        // called during put and ensures the map doesn't grow
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
            return size() > maxElements;
        }
    };

    public MemoryStore(int maxElements) {
        if (maxElements < 1) {
            throw new IllegalArgumentException("maxElements must be > 1");
        }
        this.maxElements = maxElements;
    }
    
    public void store(T value) {
        this.store(UUID.randomUUID().toString(), value);
    }

    public void store(String key, T value) {
        lock.lock();
        try {
            data.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    public T get(String key) {
        lock.lock();
        try {
            return data.get(key);
        } finally {
            lock.unlock();
        }
    }
    
    public int size() {
        lock.lock();
        try {
            return data.size();
        } finally {
            lock.unlock();
        }
    }
    
    public LinkedHashMap<String, T> data() {
        lock.lock();
        try {
            return new LinkedHashMap<>(this.data);
        } finally {
            lock.unlock();
        }
    }
}
