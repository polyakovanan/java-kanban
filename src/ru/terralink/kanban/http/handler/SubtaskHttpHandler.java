package ru.terralink.kanban.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class SubtaskHttpHandler extends TaskHttpHandler{
    public SubtaskHttpHandler(TaskManager taskManager) {
        super(taskManager);
        this.entityType = TaskType.SUBTASK;
        this.entityName = "Подзадача";
        this.locationPath = "subtasks/";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String[] uriElements = path.split("/");
        if (uriElements.length > 3) {
            sendNotFound(exchange, "Не удалось найти ресурс");
            return;
        }

        Integer taskId = null;
        if (uriElements.length > 2) {
            try {
                taskId = Integer.parseInt(uriElements[2]);
            } catch (NumberFormatException e) {
                sendNotFound(exchange, "Неверный формат ID");
                return;
            }
        }
        switch (method) {
            case "GET" -> processGet(exchange, taskId);
            case "POST" -> {
                Task task = TaskGson.getGson().fromJson(new String(exchange.getRequestBody().readAllBytes()), Subtask.class);
                processPost(exchange, taskId, task);
            }
            case "DELETE" -> processDelete(exchange, taskId);
            default -> sendMethodNotAllowed(exchange, this.allowedMethods);
        }
    }
}
