package company.vk.edu.distrib.compute.dendenich;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.KVService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

public class KVServiceI implements KVService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KVServiceI.class);
    private final HttpServer server;
    private final Dao<byte[]> dao;

    public KVServiceI(int port, Dao<byte[]> dao) throws IOException {
        this.dao = dao;
        // Создаем сервер и привязываем его к порту
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Регистрируем обработчики (Handlers)
        server.createContext("/v0/status", this::handleStatus);
        server.createContext("/v0/entity", this::handleEntity);

        // любой запрос, не попавший в верхние пути - будет выброшен с ошибкой
        server.createContext("/", exchange -> {
            try (exchange) {
                exchange.sendResponseHeaders(400, -1);
            }
        });

        // Задаются Executor (пул потоков)
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
    }

    @Override
    public void start() {
        LOGGER.info("Server starting");
        server.start();
    }

    // Завершаем работу сервера, вместе с
    @Override
    public void stop() {
        LOGGER.info("Server shutdown");
        server.stop(0);
    }

    // Обработка GET /v0/status
    private void handleStatus(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Возвращаем 200 OK в нормальной ситуации
            exchange.sendResponseHeaders(200, -1);
        } else {
            exchange.sendResponseHeaders(503, -1); // Server error
        }
        exchange.close();
    }

    private void handleEntity(HttpExchange exchange) throws IOException {
        try (exchange) {
            try {
                String method = exchange.getRequestMethod();
                String query = exchange.getRequestURI().getQuery();
                String id = extractId(query);

                switch (method) {
                    case "GET" -> {
                        LOGGER.info("Begin start GET query");
                        byte[] data = dao.get(id);
                        exchange.sendResponseHeaders(200, data.length);
                        try (OutputStream os = exchange.getResponseBody()) {
                            os.write(data);
                        }
                    }
                    case "PUT" -> {
                        try (InputStream is = exchange.getRequestBody()) {
                            LOGGER.info("Begin start PUT query");
                            byte[] body = is.readAllBytes();
                            dao.upsert(id, body);
                        }
                        exchange.sendResponseHeaders(201, -1);
                    }
                    case "DELETE" -> {
                        LOGGER.info("Begin start DELETE query");
                        dao.delete(id);
                        exchange.sendResponseHeaders(202, -1);
                    }
                    default -> exchange.sendResponseHeaders(405, -1);
                }
            } catch (NoSuchElementException e) {
                LOGGER.error("NoSuchElementException");
                exchange.sendResponseHeaders(404, -1);
            } catch (IllegalArgumentException e) {
                LOGGER.error("IllegalArgumentException");
                exchange.sendResponseHeaders(400, -1);
            } catch (IOException e) {
                LOGGER.error("IOException");
                exchange.sendResponseHeaders(500, -1);
            }

        }
    }

    private String extractId(String query) {
        LOGGER.info("Get id from query ");

        if (query == null || query.isBlank()) {
            LOGGER.error("Param 'id' is not in query");
            throw new IllegalArgumentException("Missing query");
        }

        if (!query.startsWith("id=")) {
            LOGGER.error("Param 'id' is not in query");
            throw new IllegalArgumentException("Missing id parameter");
        }

        if (query.contains("&")) {
            LOGGER.error("Unexpected parameters");
            throw new IllegalArgumentException("Unexpected parameters");
        }

        String param = query.substring(3);

        if (param.isEmpty()) {
            throw new IllegalArgumentException("Empty id");
        }

        LOGGER.debug("Get id from query");
        return param;

    }
}
