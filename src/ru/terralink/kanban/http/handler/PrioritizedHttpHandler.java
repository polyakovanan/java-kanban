package ru.terralink.kanban.http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class PrioritizedHttpHandler extends BaseHttpHandler {

    public PrioritizedHttpHandler(TaskManager taskManager){
        super(taskManager);
        this.allowedMethods = List.of("GET");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("GET")) {
            List prioritizedTasks = this.taskManager.getPrioritizedTasks();
            Gson gson = new Gson();
            sendJSONSuccessResponse(exchange, gson.toJson(prioritizedTasks));
        } else {
            sendMethodNotAllowed(exchange, this.allowedMethods);
        }
    }
}
