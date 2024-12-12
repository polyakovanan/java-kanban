package ru.terralink.kanban.http;

import com.sun.net.httpserver.HttpServer;
import ru.terralink.kanban.http.handler.*;
import ru.terralink.kanban.service.Managers;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static HttpServer httpServer;
    private static TaskManager taskManager;

    public static void main(String[] args) {
        start(Managers.getDefault());
    }

    public static void start(TaskManager manager) {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        taskManager = manager;
        httpServer.createContext("/tasks", new TaskHttpHandler(taskManager));
        httpServer.createContext("/subtasks", new SubtaskHttpHandler(taskManager));
        httpServer.createContext("/epics", new EpicHttpHandler(taskManager));
        httpServer.createContext("/history", new HistoryHttpHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(taskManager));
        httpServer.start();
    }

    public static void stop() {
        if (httpServer != null) {
            httpServer.stop(1);
        }
    }

}
