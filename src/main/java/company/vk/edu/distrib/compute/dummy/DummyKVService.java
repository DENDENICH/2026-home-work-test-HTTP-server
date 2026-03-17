package company.vk.edu.distrib.compute.dummy;

import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.KVService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DummyKVService implements KVService {
    private static final Logger log = LoggerFactory.getLogger(DummyKVService.class);

    private final HttpServer server;

    public DummyKVService() {
        server = null;
        initServer();
    }

    private void initServer() {
        if (server == null) {
            log.error("Server is null");
        }
    }

    @Override
    public void start() {
        log.info("Started");
    }

    @Override
    public void stop() {
        log.info("Stopping");
    }
}
