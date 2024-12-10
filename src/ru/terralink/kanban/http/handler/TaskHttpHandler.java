package ru.terralink.kanban.http.handler;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.service.TaskManager;
import ru.terralink.kanban.util.TaskError;
import ru.terralink.kanban.util.TaskUtils;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class TaskHttpHandler extends BaseHttpHandler {
    TaskType entityType;
    String entityName;
    String locationPath;

    public TaskHttpHandler(TaskManager taskManager){
        super(taskManager);
        this.allowedMethods = List.of("GET", "POST", "DELETE");
        this.entityType = TaskType.TASK;
        this.entityName = "Задача";
        this.locationPath = "tasks/";
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
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
                    Task task = TaskGson.getGson().fromJson(new String(exchange.getRequestBody().readAllBytes()), Task.class);
                    processPost(exchange, taskId, task);
                }
                case "DELETE" -> processDelete(exchange, taskId);
                default -> sendMethodNotAllowed(exchange, this.allowedMethods);
            }
        } catch (IllegalArgumentException | IOException | JsonSyntaxException | JsonIOException e) {
            sendServerFailed(exchange);
        }
    }

    protected void processDelete(HttpExchange exchange, Integer taskId) throws IOException {
        if (taskId != null) {
            int errCode = this.taskManager.deleteTaskByIdAndType(taskId, entityType);
            if (errCode == 0) {
                sendSuccessResponse(exchange, entityName + " успешно удалена");
            } else {
                processError(exchange, errCode);
            }
        } else {
            sendNotFound(exchange, "Не предоставлен ID для удаления");
        }
    }

    protected void processError(HttpExchange exchange, int errCode) throws IOException {
        Optional<TaskError> error = TaskUtils.ERROR_CODES.entrySet().stream()
                .filter(entry -> Objects.equals(entry.getValue(), errCode))
                .map(Map.Entry::getKey)
                .findFirst();

        if (error.isPresent()) {
            switch (error.get()) {
                case TaskError.INTERSECT -> sendIntersectError(exchange);
                case TaskError.ABSENT_EPIC -> sendAbsentEpicError(exchange);
                case TaskError.UNKNOWN -> sendServerFailed(exchange);
            }
        } else {
            sendImATeapot(exchange);
        }
    }

    protected void processPost(HttpExchange exchange, Integer taskId, Task task) throws IOException {
        if (taskId != null) {
            Task originalTask = this.taskManager.getTaskById(taskId);
            if (originalTask == null) {
                sendNotFound(exchange, entityName + " с таким ID отсутствует");
            } else {
                int errCode = this.taskManager.updateTaskByIdAndType(task, taskId, this.entityType);
                if (errCode == 0) {
                    sendSuccessResponse(exchange,  "Обновление прошло успешно");
                } else {
                    processError(exchange, errCode);
                }
            }
        } else {
            int createdTaskId = this.taskManager.createTaskByType(task, this.entityType);
            if (createdTaskId > -1) {
                sendSuccessCreateResponse(exchange, createdTaskId);
            } else {
                processError(exchange, createdTaskId);
            }
        }
    }

    protected void processGet(HttpExchange exchange, Integer taskId) throws IOException {
        if (taskId != null) {
            Task task = this.taskManager.getTaskByIdAndType(taskId, this.entityType);
            if (task == null) {
                sendNotFound(exchange, entityName + " с таким ID отсутствует");
            } else {
                sendJSONSuccessResponse(exchange, TaskGson.getGson().toJson(task));
            }
        } else {
            List<Task> tasks = this.taskManager.getTasksByType(this.entityType);
            sendJSONSuccessResponse(exchange, TaskGson.getGson().toJson(tasks));
        }
    }

    protected void sendSuccessCreateResponse(HttpExchange exchange, int createdTaskId) throws IOException {
        exchange.getResponseHeaders().add("Location",  locationPath + createdTaskId);
        sendResponse(exchange, entityName + " успешно создана",201);
    }

    protected void sendIntersectError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, entityName + " пересекается по срокам с существующими", 406);
    }

    protected void sendAbsentEpicError(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "В системе нет эпика с таким ID для привязки", 406);
    }

    protected void sendImATeapot(HttpExchange exchange) throws IOException {
        sendResponse(exchange, "Не знаю, как ты сюда попал, но выпей чаю", 418);
    }
}
