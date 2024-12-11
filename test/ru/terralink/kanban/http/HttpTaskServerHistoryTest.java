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

public class HttpTaskServerHistoryTest {
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
        Task task = new Task("Задача", "Задача");
        manager.createTask(task);
        Epic epic = new Epic("Эпик", "Эпик");
        manager.createTask(epic);
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        manager.createTask(subtask);

        manager.getTaskById(2);
        manager.getTaskById(2);
        manager.getTaskById(1);
        manager.getTaskById(3);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);

        Assertions.assertEquals(3, requestedTasks.size(), "Менеджер задач вернул неверный журнал истории");
        assertEquals(2, requestedTasks.get(0).getId(), "Сервер отправил журнал истории в неправильно порядке");
        assertEquals(1, requestedTasks.get(1).getId(), "Сервер отправил журнал истории в неправильно порядке");
        assertEquals(3, requestedTasks.get(2).getId(), "Сервер отправил журнал истории в неправильно порядке");
    }

    @Test
    public void httpTaskServerReportsUnsupportedMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Сервер не сообщил о неподдерживаемом методе");
    }
}
