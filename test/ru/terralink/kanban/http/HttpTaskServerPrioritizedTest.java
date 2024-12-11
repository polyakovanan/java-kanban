package ru.terralink.kanban.http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.service.InMemoryTaskManager;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerPrioritizedTest {
    HttpTaskServer taskServer;
    TaskManager manager;

    @BeforeEach
    public void setUp() {
        manager = new InMemoryTaskManager();
        taskServer = new HttpTaskServer();
        taskServer.start(manager);
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void httpTaskServerGetsPrioritizedList() throws IOException, InterruptedException {
        Task task1 = new Task("Задача 1", "Задача 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        task1.setDuration(Duration.ofMinutes(120));
        manager.createTask(task1);

        Task task2 = new Task("Задача 2", "Задача 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 10,0,0));
        task2.setDuration(Duration.ofMinutes(120));

        manager.createTask(task2);
        Task task3 = new Task("Задача 3", "Задача 3");
        manager.createTask(task3);

        Epic epic = new Epic("Эпик", "Эпик");

        manager.createTask(epic);

        Subtask subtask1 = new Subtask( "Подзадача 1", "Подзадача 1", epic);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 3,0,0));
        subtask1.setDuration(Duration.ofMinutes(120));
        manager.createTask(subtask1);

        Subtask subtask2 = new Subtask( "Подзадача 2", "Подзадача 2", epic);
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 7,0,0));
        subtask2.setDuration(Duration.ofMinutes(120));
        manager.createTask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);

        assertEquals(4, requestedTasks.size(), "Сервер отправил список приоритета неправильного размера");
        assertEquals(1, requestedTasks.get(0).getId(), "Сервер отправил список приоритета в неправильно порядке");
        assertEquals(5, requestedTasks.get(1).getId(), "Сервер отправил список приоритета в неправильно порядке");
        assertEquals(6, requestedTasks.get(2).getId(), "Сервер отправил список приоритета в неправильно порядке");
        assertEquals(2, requestedTasks.get(3).getId(), "Сервер отправил список приоритета в неправильно порядке");
    }

    @Test
    public void httpTaskServerReportsUnsupportedMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Сервер не сообщил о неподдерживаемом методе");
    }
}
