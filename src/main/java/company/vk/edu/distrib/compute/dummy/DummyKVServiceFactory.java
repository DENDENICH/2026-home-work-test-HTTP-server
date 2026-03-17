package company.vk.edu.distrib.compute.dummy;

import java.io.IOException;

import company.vk.edu.distrib.compute.KVService;
import company.vk.edu.distrib.compute.KVServiceFactory;

public class DummyKVServiceFactory extends KVServiceFactory {
    @Override
    protected KVService doCreate(int port) throws IOException {
        return new DummyKVService();
    }
}
