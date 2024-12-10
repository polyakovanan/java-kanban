package ru.terralink.kanban.http.handler;

import com.sun.net.httpserver.HttpExchange;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.util.List;

public class HistoryHttpHandler extends BaseHttpHandler {

    public HistoryHttpHandler(TaskManager taskManager){
        super(taskManager);
        this.allowedMethods = List.of("GET");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (exchange.getRequestMethod().equals("GET")) {
            List history = this.taskManager.getHistory();
            sendJSONSuccessResponse(exchange, TaskGson.getGson().toJson(history));
        } else {
            sendMethodNotAllowed(exchange, this.allowedMethods);
        }
    }
}
