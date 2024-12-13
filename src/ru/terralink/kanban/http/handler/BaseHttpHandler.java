package ru.terralink.kanban.http.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public abstract class BaseHttpHandler implements HttpHandler {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected final TaskManager taskManager;
    protected List<String> allowedMethods;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Nothing to see here", 404);
    }

    protected void sendJSONSuccessResponse(HttpExchange exchange, String responseString) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        sendResponse(exchange, responseString,200);
    }

    protected void sendSuccessResponse(HttpExchange exchange, String responseString) throws IOException {
        sendResponse(exchange, responseString,200);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, List<String> allowedMethods) throws IOException {
        exchange.getResponseHeaders().add("Allowed", allowedMethods.stream().collect(Collectors.joining(", ")));
        sendResponse(exchange, "Метод для данного ресурса не поддерживается",405);
    }

    protected void sendNotFound(HttpExchange exchange, String responseString) throws IOException {
        sendResponse(exchange, responseString,404);
    }

    protected void sendResponse(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            exchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes(DEFAULT_CHARSET));
        }
        exchange.close();
    }

    protected void sendServerFailed(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Произошла внутренняя ошибка сервера",500);
    }

}
