package company.vk.edu.distrib.compute.dendenich;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import company.vk.edu.distrib.compute.Dao;
import company.vk.edu.distrib.compute.KVService;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.NoSuchElementException;

public class KVServiceI implements KVService {
    private final HttpServer server;
    private final Dao<byte[]> dao;

    public KVServiceI(int port, Dao<byte[]> dao) throws IOException {
        this.dao = dao;
        // Создаем сервер и привязываем его к порту
        this.server = HttpServer.create(new InetSocketAddress(port), 0);

        // Регистрируем обработчики (Handlers)
        server.createContext("/v0/status", this::handleStatus);
        server.createContext("/v0/entity", this::handleEntity);

        // В Enterprise-системах важно задавать Executor (пул потоков)
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop(0);
    }

    // Обработка GET /v0/status
    private void handleStatus(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // Возвращаем 200 OK в нормальной ситуации
            exchange.sendResponseHeaders(200, -1);
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
        exchange.close();
    }

    private void handleEntity(HttpExchange exchange) throws IOException {
        try (exchange) {
            String method = exchange.getRequestMethod();
            String query = exchange.getRequestURI().getQuery();
            String id = extractId(query);

            if (id == null || id.isEmpty()) {
                exchange.sendResponseHeaders(400, -1);
                return;
            }

            switch (method) {
                case "GET" -> {
                    byte[] data = dao.get(id);
                    exchange.sendResponseHeaders(200, data.length);
                    try (OutputStream os = exchange.getResponseBody()) {
                        os.write(data);
                    }
                }
                case "PUT" -> {
                    try (InputStream is = exchange.getRequestBody()) {
                        byte[] body = is.readAllBytes();
                        dao.upsert(id, body);
                    }
                    exchange.sendResponseHeaders(201, -1);
                }
                case "DELETE" -> {
                    dao.delete(id);
                    exchange.sendResponseHeaders(202, -1);
                }
                default -> exchange.sendResponseHeaders(405, -1);
            }
        } catch (NoSuchElementException e) {
            exchange.sendResponseHeaders(404, -1);
        }
    }

    // Вспомогательный метод для парсинга ?id=<ID>
    private String extractId(String query) {
        if (query != null && query.startsWith("id=")) {
            return query.substring(3);
        }
        return null;
    }
}
