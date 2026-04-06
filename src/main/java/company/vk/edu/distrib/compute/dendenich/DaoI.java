package company.vk.edu.distrib.compute.dendenich;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import company.vk.edu.distrib.compute.Dao;

public class DaoI implements Dao<byte[]> {

    private final Map<String, byte[]> storage = new ConcurrentHashMap<>();

    @Override
    public byte[] get(String key) throws IllegalArgumentException, NoSuchElementException, IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key is null or blank");
        }
        final var value = storage.get(key);
        if (value == null) {
            throw new NoSuchElementException("no value for key " + key);
        }
        return value;
    }

    @Override
    public void upsert(String key, byte[] value) throws IllegalArgumentException, IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key is null or blank");
        }
        storage.put(key, value);
    }

    @Override
    public void delete(String key) throws IllegalArgumentException, NoSuchElementException, IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Key is null or blank");
        }
        storage.remove(key);
    }

    @Override
    public void close() {
        storage.clear();
    }
}
