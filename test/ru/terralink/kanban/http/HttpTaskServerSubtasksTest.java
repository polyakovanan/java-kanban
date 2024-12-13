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

public class HttpTaskServerSubtasksTest {

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
    public void httpTaskServerCreatesSubtask() throws IOException, InterruptedException {
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

        List<Task> tasks = manager.getTasksByType(TaskType.SUBTASK);

        assertEquals(1, tasks.size(), "Сервер не добавил подзадачу в менеджер");
        assertEquals("Подзадача", tasks.get(0).getName(), "Сервер добавил подзадачу с некорректным именем");
    }

    @Test
    public void httpTaskServerUpdatesSubtask() throws IOException, InterruptedException {
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

        subtask.setName("Подбабача");
        url = URI.create("http://localhost:8080/subtasks/2");
        taskJson = TaskGson.getGson().toJson(subtask);
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getTasksByType(TaskType.SUBTASK);

        assertEquals(1, tasks.size(), "Сервер некорректно обновил задачу");
        assertEquals("Подбабача", tasks.get(0).getName(), "Сервер не обновил атрибут задачи");
    }

    @Test
    public void httpTaskServerDoNotCreateSubtaskWithAbsentEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        epic.setId(1);

        Subtask subtask = new Subtask(1, "Подзадача", "Описание подзадачи", epic);
        subtask.setDuration(Duration.ofMinutes(60));
        subtask.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));

        String taskJson = TaskGson.getGson().toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Сервер не сообщил об отсутствии эпика для подзадачи");
    }

    @Test
    public void httpTaskServerGetsSubtask() throws IOException, InterruptedException {
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

        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task requestedTask = TaskGson.getGson().fromJson(response.body(), Task.class);

        assertEquals(2, requestedTask.getId(), "Сервер вернул некорректную подзадачу");
        assertEquals(subtask.getName(), requestedTask.getName(), "Сервер вернул некорректную подзадачу");
        assertEquals(subtask.getDescription(), requestedTask.getDescription(), "Сервер вернул некорректную подзадачу");
        assertEquals(subtask.getDuration(), requestedTask.getDuration(), "Сервер вернул некорректную подзадачу");
        assertEquals(subtask.getStartTime(), requestedTask.getStartTime(), "Сервер вернул некорректную подзадачу");
    }

    @Test
    public void httpTaskServerGetsSubtaskList() throws IOException, InterruptedException {
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

        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Subtask>>() {}.getType();
        List<Task> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);
        List<Task> managerTasks = manager.getTasksByType(TaskType.SUBTASK);

        assertIterableEquals(managerTasks, requestedTasks, "Сервер вернул некорректный список подзадач");
    }

    @Test
    public void httpTaskServerDeletesSubtask() throws IOException, InterruptedException {
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

        url = URI.create("http://localhost:8080/subtasks/2");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getTasksByType(TaskType.SUBTASK);

        assertEquals(0, tasks.size(), "Сервер не удалил подзадачу в менеджере");
    }

    @Test
    public void httpTaskServerRequiresIdForDeletingSubtask() throws IOException, InterruptedException {
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

        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об обязательности id при удалении подзадачи");
    }

    @Test
    public void httpTaskServerDoNotGetAbsentSubtask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об отсутствии подзадачи по id");
    }

    @Test
    public void httpTaskServerDoNotLetOverlapingSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Эпик", "Описание эпика");
        String taskJson = TaskGson.getGson().toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        epic.setId(1);

        Subtask subtask = new Subtask("Подзадача", "Описание подзадачи", epic);
        subtask.setDuration(Duration.ofMinutes(120));
        subtask.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        taskJson = TaskGson.getGson().toJson(subtask);

        url = URI.create("http://localhost:8080/subtasks");
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask subtask1 = new Subtask("Подзадача 2", "Описание подзадачи 2", epic);
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 1, 0));
        taskJson = TaskGson.getGson().toJson(subtask1);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Сервер не сообщил о пересечении подзадач по срокам");
    }

    @Test
    public void httpTaskServerReportsMalformedRequests() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Сервер не сообщил о некорректном запросе");
    }

    @Test
    public void httpTaskServerReportsUnsupportedMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Сервер не сообщил о неподдерживаемом методе");
    }
}
