package ru.terralink.kanban.http;

import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.http.json.adapter.TaskGson;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.service.InMemoryTaskManager;
import ru.terralink.kanban.service.TaskManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;
import java.net.http.HttpClient;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class HttpTaskServerTasksTest {

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
    public void httpTaskServerCreatesTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasks = manager.getTasksByType(TaskType.TASK);

        assertEquals(1, tasks.size(), "Сервер не добавил задачу в менеджер");
        assertEquals("Задача", tasks.get(0).getName(), "Сервер добавил задачу с некорректным именем");
    }

    @Test
    public void httpTaskServerUpdatesTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        task.setName("Бабача");
        url = URI.create("http://localhost:8080/tasks/1");
        taskJson = TaskGson.getGson().toJson(task);
        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getTasksByType(TaskType.TASK);

        assertEquals(1, tasks.size(), "Сервер некорректно обновил задачу");
        assertEquals("Бабача", tasks.get(0).getName(), "Сервер не обновил атрибут задачи");
    }

    @Test
    public void httpTaskServerGetsTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task requestedTask = TaskGson.getGson().fromJson(response.body(), Task.class);

        assertEquals(1, requestedTask.getId(), "Сервер вернул некорректную задачу");
        assertEquals(task.getName(), requestedTask.getName(), "Сервер вернул некорректную задачу");
        assertEquals(task.getDescription(), requestedTask.getDescription(), "Сервер вернул некорректную задачу");
        assertEquals(task.getDuration(), requestedTask.getDuration(), "Сервер вернул некорректную задачу");
        assertEquals(task.getStartTime(), requestedTask.getStartTime(), "Сервер вернул некорректную задачу");
    }

    @Test
    public void httpTaskServerGetsTaskList() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task task1 = new Task("Задача 2", "Описание задачи 2");
        task1.setDuration(Duration.ofMinutes(60));
        task1.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 4, 0));
        taskJson = TaskGson.getGson().toJson(task1);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).GET().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Type listType = new TypeToken<List<Task>>() {}.getType();
        List<Task> requestedTasks = TaskGson.getGson().fromJson(response.body(), listType);
        List<Task> managerTasks = manager.getTasksByType(TaskType.TASK);

        assertIterableEquals(managerTasks, requestedTasks, "Сервер вернул некорректный список задач");
    }

    @Test
    public void httpTaskServerDeletesTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/tasks/1");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasks = manager.getTasksByType(TaskType.TASK);

        assertEquals(0, tasks.size(), "Сервер не удалил задачу в менеджере");
    }

    @Test
    public void httpTaskServerRequiresIdForDeletingTask() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(60));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        url = URI.create("http://localhost:8080/tasks");
        request = HttpRequest.newBuilder().uri(url).DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об обязательности id при удалении задачи");
    }

    @Test
    public void httpTaskServerDoNotGetAbsentTask() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Сервер не сообщает об отсутствии задачи по id");
    }

    @Test
    public void httpTaskServerDoNotLetOverlapingTasks() throws IOException, InterruptedException {
        Task task = new Task("Задача", "Описание задачи");
        task.setDuration(Duration.ofMinutes(120));
        task.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 0, 0));
        String taskJson = TaskGson.getGson().toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Task task1 = new Task("Задача 2", "Описание задачи 2");
        task1.setDuration(Duration.ofMinutes(60));
        task1.setStartTime(LocalDateTime.of(2024, Month.JANUARY, 1, 1, 0));
        taskJson = TaskGson.getGson().toJson(task1);

        request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode(), "Сервер не сообщил о пересечении задач по срокам");
    }

    @Test
    public void httpTaskServerReportsMalformedRequests() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(500, response.statusCode(), "Сервер не сообщил о некорректном запросе");
    }

    @Test
    public void httpTaskServerReportsUnsupportedMethod() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).PUT(HttpRequest.BodyPublishers.ofString("SomeGarbageString")).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(405, response.statusCode(), "Сервер не сообщил о неподдерживаемом методе");
    }
}
