package ru.terralink.kanban.http;

import com.sun.net.httpserver.HttpServer;
import ru.terralink.kanban.http.handler.*;
import ru.terralink.kanban.service.Managers;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static HttpServer httpServer = null;
    public static void main(String[] args) {
        start();
    }

    public static void start() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TaskManager manager = Managers.getDefault();
        httpServer.createContext("/tasks", new TaskHttpHandler(manager));
        httpServer.createContext("/subtasks", new SubtaskHttpHandler(manager));
        httpServer.createContext("/epics", new EpicHttpHandler(manager));
        httpServer.createContext("/history", new HistoryHttpHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHttpHandler(manager));
        httpServer.start();
    }

    public static void stop(){
        if (httpServer != null) {
            httpServer.stop(5);
        }
    }

}
