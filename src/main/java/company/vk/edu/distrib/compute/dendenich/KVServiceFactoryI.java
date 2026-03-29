package company.vk.edu.distrib.compute.dendenich;

import company.vk.edu.distrib.compute.KVService;
import company.vk.edu.distrib.compute.KVServiceFactory;

import java.io.IOException;

public class KVServiceFactoryI extends KVServiceFactory {

    @Override
    protected KVService doCreate(int port) throws IOException {
        // Так как конструктор KVServiceI (в котором создается HttpServer)
        // может выбросить IOException, мы просто позволяем этому исключению
        // "лететь" дальше наверх, так как это разрешено сигнатурой (throws IOException).
        return new KVServiceI(port, new DaoI());
    }
}
