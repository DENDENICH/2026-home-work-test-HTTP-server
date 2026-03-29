package company.vk.edu.distrib.compute.dendenich;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import company.vk.edu.distrib.compute.Dao;

public class DaoI implements Dao<byte[]> {

    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    @Override
    public byte[] get(String key) {
        return storage.get(key);
    }

    @Override
    public void upsert(String key, byte[] value) {
        storage.put(key, value);
    }

    @Override
    public void delete(String key) {
        storage.remove(key);
    }

    @Override
    public void close() {
        storage.clear();
    }
}
