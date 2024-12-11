package ru.terralink.kanban.http.handler;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class EpicHttpHandler extends TaskHttpHandler {
    public EpicHttpHandler(TaskManager taskManager) {
        super(taskManager);
        this.entityType = TaskType.EPIC;
        this.entityName = "Эпик";
        this.locationPath = "epics/";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String[] uriElements = path.split("/");
            if (uriElements.length > 4) {
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

            boolean subtasks = false;
            if (uriElements.length > 3) {
                if (!uriElements[3].equals("subtasks")) {
                    sendNotFound(exchange, "Не удалось найти ресурс");
                    return;
                } else {
                    subtasks = true;
                }
            }

            switch (method) {
                case "GET" -> {
                    if (subtasks) {
                        processSubtasks(exchange, taskId);
                    } else {
                        processGet(exchange, taskId);
                    }
                }
                case "POST" -> {
                    Task task = TaskGson.getGson().fromJson(new String(exchange.getRequestBody().readAllBytes()), Epic.class);
                    processPost(exchange, taskId, task);
                }
                case "DELETE" -> processDelete(exchange, taskId);
                default -> sendMethodNotAllowed(exchange, this.allowedMethods);
            }
        } catch (IllegalArgumentException | IOException | JsonSyntaxException | JsonIOException e) {
            sendServerFailed(exchange);
        }
    }

    private void processSubtasks(HttpExchange exchange, Integer taskId) throws IOException {
        if (taskId != null) {
            List<Subtask> subTasks = this.taskManager.getSubtasksByEpic(taskId);
            sendJSONSuccessResponse(exchange, TaskGson.getGson().toJson(subTasks));
        } else {
            sendNotFound(exchange, "Неверный формат ID");
        }
    }
}
