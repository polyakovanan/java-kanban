package ru.terralink.kanban.http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
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
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class HttpTaskServerEpicsTest {
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
    public void httpTaskServerCreatesEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> epics = manager.getTasksByType(TaskType.EPIC);

        assertEquals(1, epics.size(), "Сервер не добавил эпик в менеджер");
        assertEquals("Эпик", epics.get(0).getName(), "Сервер добавил эпик с некорректным именем");
    }

    @Test
    public void httpTaskServerUpdatesEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        epic.setName("Кекик");
        url = URI.create("http://localhost:8080/epics/1");
        taskJson = TaskGson.getGson().toJson(epic);
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> epics = manager.getTasksByType(TaskType.EPIC);

        assertEquals(1, epics.size(), "Сервер некорректно обновил эпик");
        assertEquals("Кекик", epics.get(0).getName(), "Сервер не обновил атрибут эпика");
    }

    @Test
    public void httpTaskServerGetsEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic requestedEpic = TaskGson.getGson().fromJson(response.body(), Epic.class);

        assertEquals(1, requestedEpic.getId(), "Сервер вернул некорректный эпик");
        assertEquals(epic.getName(), requestedEpic.getName(), "Сервер вернул некорректный эпик");
        assertEquals(epic.getDescription(), requestedEpic.getDescription(), "Сервер вернул некорректный эпик");
    }

    @Test
    public void httpTaskServerGetsEpicList() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Epic epic1 = new Epic("Эпик 2", "Описание эпика 2");
        taskJson = TaskGson.getGson().toJson(epic1);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Epic>>() {}.getType();
        List<Task> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);
        List<Task> managerTasks = manager.getTasksByType(TaskType.EPIC);

        assertIterableEquals(managerTasks, requestedTasks, "Сервер вернул некорректный список эпиков");
    }

    @Test
    public void httpTaskServerDeletesEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/epics/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> epics = manager.getTasksByType(TaskType.EPIC);

        assertEquals(0, epics.size(), "Сервер не удалил эпик в менеджере");
    }

    @Test
    public void httpTaskServerRequiresIdForDeletingEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/epics");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об обязательности id при удалении эпика");
    }

    @Test
    public void httpTaskServerReturnsEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        epic.setId(1);
        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic);
        subtask.setDuration(Duration.ofMinutes(60));
        subtask.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        taskJson = TaskGson.getGson().toJson(subtask);
        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask subtask1 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic);
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 2, 0));
        taskJson = TaskGson.getGson().toJson(subtask1);
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/epics/1/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Subtask>>() {}.getType();
        List<Subtask> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);
        List<Subtask> managerTasks = manager.getSubtasksByEpic(1);

        assertIterableEquals(managerTasks, requestedTasks, "Сервер вернул некорректный список подзадач эпика");
    }

    @Test
    public void httpTaskServerDoNotGetAbsentEpic() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об отсутствии эпика по id");
    }

    @Test
    public void httpTaskServerReportsMalformedRequests() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Сервер не сообщил о некорректном запросе");
    }

    @Test
    public void httpTaskServerReportsUnsupportedMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Сервер не сообщил о неподдерживаемом методе");
    }
}
