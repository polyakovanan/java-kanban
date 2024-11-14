package ru.terralink.kanban.service;

import org.junit.jupiter.api.*;
import ru.terralink.kanban.model.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileBackedTaskManagerTest {
    File saveFile;
    static File loadFile;
    String saveFileContentCheck;
    static final String resourcePath = "resources" + File.separator + "test" + File.separator + "service" + File.separator + "FileBackedTaskManager" + File.separator;

    @BeforeAll
    static void createFileForLoadingCheck(){
        loadFile = Paths.get(resourcePath + "createFileForLoadingCheck").toFile();
    }

    @BeforeEach
    void createSaveFile(){
        try {
            saveFile = Files.createTempFile("fileBackedTest", ".txt").toFile();
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
    }

    @AfterEach
    void checkSaveFileContent(){
        try {
            String saveContent = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
            String checkContent = Files.readString(Path.of(resourcePath + saveFileContentCheck), StandardCharsets.UTF_8);
            Assertions.assertEquals(saveContent, checkContent, "Содержимое файла сохранения не совпадает с проверочными для " + saveFileContentCheck);
        } catch (IOException e) {
            Assertions.fail(saveFileContentCheck + " " + e.getMessage());
        }
    }

    @Test
    void managersReturnNotNullFileBackedTaskManager(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Assertions.assertNotNull(taskManager, "Managers не отдает менеджер задач с файлом сохранения");
        saveFileContentCheck = "managersReturnNotNullFileBackedTaskManager";
    }

    @Test
    void managersLoadFileBackedTaskManagerCorrectly(){
        TaskManager taskManager = null;
        try {
            taskManager = Managers.loadFromFile(loadFile);
        } catch (IOException e) {
            Assertions.fail("Ошибка создания менеджера " + e.getMessage());
        }
        Assertions.assertNotNull(taskManager, "Managers не отдает менеджер задач с файлом сохранения");

        Task task = taskManager.getTaskById(1);
        Assertions.assertNotNull(task, "Менеджер задач не загрузил задачу из файла");

        Epic epic = (Epic) taskManager.getTaskById(2);
        Assertions.assertNotNull(epic, "Менеджер задач не загрузил эпик из файла");

        Subtask subtask = (Subtask) taskManager.getTaskById(3);
        Assertions.assertNotNull(subtask, "Менеджер задач не загрузил подзадачу из файла");

        Assertions.assertEquals(epic.getSubtasks().get(3), subtask, "Менеджер задач не связал подзадачу с эпиком из файла");

        saveFile = ((FileBackedTaskManager) taskManager).getSaveFile();
        saveFileContentCheck = "managersLoadFileBackedTaskManagerCorrectly";
    }

    @Test
    void fileBackedTaskManagerCreatesTasksAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);

        Task task1 = new Task("Задача 1", "Задача 1");
        Task task2 = new Task("Задача 2", "Задача 2");

        int id = taskManager.createTaskByType(task1, TaskType.TASK);
        Assertions.assertEquals(1, id, "Менеджер задач создал неверный id задачи");

        id = taskManager.createTaskByType(task2, TaskType.TASK);
        Assertions.assertEquals(2, id, "Менеджер задач создал неверный id задачи");

        List<Task> tasks = taskManager.getTasksByType(TaskType.TASK);
        Assertions.assertEquals(2, tasks.size(), "Менеджер задач не добавил задачи в список");

        saveFileContentCheck = "fileBackedTaskManagerCreatesTasksAndAssignsIncrementalIds";
    }

    @Test
    void fileBackedTaskManagerCreatesEpicsAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);

        Epic epic1 = new Epic("Эпик 1", "Эпик 1");
        Epic epic2 = new Epic("Эпик 2", "Эпик 2");

        int id = taskManager.createTaskByType(epic1, TaskType.EPIC);
        Assertions.assertEquals(1, id, "Менеджер задач создал неверный id эпика");

        id = taskManager.createTaskByType(epic2, TaskType.EPIC);
        Assertions.assertEquals(2, id, "Менеджер задач создал неверный id эпика");

        List<Task> tasks = taskManager.getTasksByType(TaskType.EPIC);
        Assertions.assertEquals(2, tasks.size(), "Менеджер задач не добавил эпики в список");

        saveFileContentCheck = "fileBackedTaskManagerCreatesEpicsAndAssignsIncrementalIds";
    }

    @Test
    void fileBackedTaskManagerCreatesSubtasksAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик 1", "Эпик 1");

        int id = taskManager.createTaskByType(epic, TaskType.EPIC);
        Assertions.assertEquals(1, id, "Менеджер задач создал неверный id эпика");

        Subtask subtask1 = new Subtask("Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = new Subtask("Подзадача 2", "Подзадача 2", epic);

        id = taskManager.createTaskByType(subtask1, TaskType.SUBTASK);
        Assertions.assertEquals(2, id, "Менеджер задач создал неверный id подзадачи");

        id = taskManager.createTaskByType(subtask2, TaskType.SUBTASK);
        Assertions.assertEquals(3, id, "Менеджер задач создал неверный id подзадачи");

        List<Task> tasks = taskManager.getTasksByType(TaskType.SUBTASK);
        Assertions.assertEquals(2, tasks.size(), "Менеджер задач не добавил подзадачи в список");

        Epic returnEpic = (Epic) taskManager.getTaskByIdAndType(1, TaskType.EPIC);
        Assertions.assertNotNull("Менеджер задач не добавил эпик в список");

        Assertions.assertEquals(2, returnEpic.getSubtasks().values().size(), "Менеджер задач не добавил подзадачи в эпик");

        saveFileContentCheck = "fileBackedTaskManagerCreatesSubtasksAndAssignsIncrementalIds";
    }

    @Test
    void fileBackedTaskManagerDoesNotCreateSubtaskWithAbsentEpic(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        taskManager.removeTasksByType(TaskType.EPIC);
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.EPIC).size(), "Менеджер задач не очищает список эпиков");

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        int id = taskManager.createTask(subtask);

        Assertions.assertEquals(-1, id, "Менеджер задач не вернул ошибку при создании подзадачи с несуществующим эпиком");
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.SUBTASK).size(), "Менеджер задач создает подзадачу с несуществующим эпиком");

        saveFileContentCheck = "fileBackedTaskManagerDoesNotCreateSubtaskWithAbsentEpic";
    }

    @Test
    void fileBackedTaskManagerDeterminesCreatedType() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.TASK).size(), "Менеджер задач не закидывает задачу в нужный тип");
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.EPIC).size(), "Менеджер задач не закидывает эпик в нужный тип");
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.SUBTASK).size(), "Менеджер задач не закидывает подзадачу в нужный тип");

        saveFileContentCheck = "fileBackedTaskManagerDeterminesCreatedType";
    }

    @Test
    void fileBackedTaskManagerGetsTaskById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        Assertions.assertEquals("Задача", taskManager.getTaskById(task.getId()).getName(), "Менеджер задач не может найти задачу по id");
        Assertions.assertEquals("Эпик", taskManager.getTaskById(epic.getId()).getName(), "Менеджер задач не может найти эпик по id");
        Assertions.assertEquals("Подзадача", taskManager.getTaskById(subtask.getId()).getName(), "Менеджер задач не может найти подзадачу по id");

        saveFileContentCheck = "fileBackedTaskManagerGetsTaskById";
    }

    @Test
    void fileBackedTaskManagerUpdatesTaskById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);

        task.setName("Бабача");

        taskManager.updateTaskById(task, task.getId());

        Task returnTask = taskManager.getTaskById(task.getId());

        Assertions.assertEquals("Бабача", returnTask.getName(), "Менеджер задач не может обновить задачу по id");

        saveFileContentCheck = "fileBackedTaskManagerUpdatesTaskById";
    }

    @Test
    void fileBackedTaskManagerUpdatesSubtaskReferenceToEpicById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic1 = new Epic("Эпик 1", "Эпик 1");
        taskManager.createTask(epic1);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic1);
        taskManager.createTask(subtask);

        Epic epic2 = new Epic("Эпик 2", "Эпик 2");
        taskManager.createTask(epic2);

        subtask.setEpicId(epic2.getId());
        taskManager.updateTaskById(subtask, subtask.getId());
        Subtask returnTask = (Subtask) taskManager.getTaskById(subtask.getId());

        Assertions.assertEquals(epic2.getId(), returnTask.getEpicId(), "Менеджер задач не может обновить подзадачу по id");

        saveFileContentCheck = "fileBackedTaskManagerUpdatesSubtaskReferenceToEpicById";
    }

    @Test
    void fileBackedTaskManagerUpdatesEpicById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        epic.setName("Кекик");

        taskManager.updateTaskById(epic, epic.getId());
        Assertions.assertEquals("Кекик", epic.getName(), "Менеджер задач не может обновить эпик по id");

        saveFileContentCheck = "fileBackedTaskManagerUpdatesEpicById";
    }

    @Test
    void fileBackedTaskManagerUpdatesEpicSubtaskListAndCalcsStatus() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        subtask.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTaskByIdAndType(subtask, subtask.getId(),TaskType.SUBTASK);

        Epic returnEpic = (Epic) taskManager.getTaskById(epic.getId());

        Assertions.assertEquals(1, returnEpic.getSubtasks().size(), "Менеджер задач не может добавить подзадачу в эпик");
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, returnEpic.getStatus(), "Менеджер задач не может пересчитать статус эпика после добавления подзадачи");

        saveFileContentCheck = "fileBackedTaskManagerUpdatesEpicSubtaskListAndCalcsStatus";
    }

    @Test
    void fileBackedTaskManagerClearsTasksByTypes() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        taskManager.removeTasksByType(TaskType.TASK);
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.TASK).size(), "Менеджер задач не очищает список задач");


        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);
        taskManager.removeTasksByType(TaskType.SUBTASK);
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.SUBTASK).size(), "Менеджер задач не очищает список подзадач");

        taskManager.removeTasksByType(TaskType.EPIC);
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.EPIC).size(), "Менеджер задач не очищает список эпиков");

        saveFileContentCheck = "fileBackedTaskManagerClearsTasksByTypes";
    }

    @Test
    void fileBackedTaskManagerDeletesTaskById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        Assertions.assertNull(taskManager.getTaskById(task.getId()), "Менеджер задач не удаляет задачу по id");

        saveFileContentCheck = "fileBackedTaskManagerDeletesTaskById";
    }

    @Test
    void fileBackedTaskManagerDeletesEpicAndItsSubtasksById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        taskManager.deleteTaskById(epic.getId());
        Assertions.assertNull(taskManager.getTaskById(epic.getId()), "Менеджер задач не удаляет эпик по id");
        Assertions.assertNull(taskManager.getTaskById(subtask.getId()), "Менеджер задач не удаляет подзадачи удаленного эпика");

        saveFileContentCheck = "fileBackedTaskManagerDeletesEpicAndItsSubtasksById";
    }

    @Test
    void fileBackedTaskManagerDeletesSubtaskAndFromEpicById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        taskManager.deleteTaskById(subtask.getId());
        Assertions.assertNull(taskManager.getTaskById(subtask.getId()), "Менеджер задач не удаляет подзадачу по id");

        Epic returnEpic = (Epic) taskManager.getTaskByIdAndType(epic.getId(), TaskType.EPIC);

        Assertions.assertEquals(0, returnEpic.getSubtasks().size(), "Менеджер задач не удаляет удаленные подзадачи из эпика");

        saveFileContentCheck = "fileBackedTaskManagerDeletesSubtaskAndFromEpicById";
    }

    @Test
    void fileBackedTaskManagerReturnsEpicSubtasksById() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);
        List<Subtask> subtasks = taskManager.getSubtasksByEpic(epic.getId());

        Assertions.assertNotEquals(0, subtasks.size(), "Менеджер задач вернул пустой список подзадач эпика");
        Assertions.assertEquals(subtask, subtasks.get(0), "Менеджер задач вернул неверный список подзадач эпика");

        saveFileContentCheck = "fileBackedTaskManagerReturnsEpicSubtasksById";
    }

    @Test
    void fileBackedTaskManagerReturnsAddsTaskHistory() {
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        taskManager.getTaskById(2);
        taskManager.getTaskById(2);
        taskManager.getTaskById(1);
        taskManager.getTaskById(3);

        List<Task> taskHistory = taskManager.getHistory();
        Assertions.assertEquals(3, taskHistory.size(), "Менеджер задач вернул неверный журнал истории");

        taskManager.deleteTaskById(1);
        taskHistory = taskManager.getHistory();
        Assertions.assertEquals(2, taskHistory.size(), "Менеджер задач вернул неверный журнал истории");

        saveFileContentCheck = "fileBackedTaskManagerReturnsAddsTaskHistory";
    }

    @Test
    void tasksInFileBackedTaskManagerProtectedFromOuterChanges(){
        TaskManager taskManager = Managers.getFileBackedTaskManager(saveFile);
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        task.setName("Изменили имя у оригинала");
        epic.setName("Изменили имя у оригинала");
        subtask.setName("Изменили имя у оригинала");

        Task taskInManager = taskManager.getTaskById(1);
        Assertions.assertEquals("Задача", taskInManager.getName(), "Задача в менеджере изменена извне");

        Epic epicInManager = (Epic) taskManager.getTaskById(2);
        Assertions.assertEquals("Эпик", epicInManager.getName(), "Эпик в менеджере изменен извне");

        Subtask subtaskInManager = (Subtask) taskManager.getTaskById(3);
        Assertions.assertEquals("Подзадача", subtaskInManager.getName(), "Подзадача в менеджере изменена извне");


        taskInManager.setName("Изменили имя у полученного из менеджера");
        epicInManager.setName("Изменили имя у полученного из менеджера");
        subtaskInManager.setName("Изменили имя у полученного из менеджера");

        Task taskInManager2 = taskManager.getTaskById(1);
        Assertions.assertEquals("Задача", taskInManager2.getName(), "Задача в менеджере изменена извне");

        Epic epicInManager2 = (Epic) taskManager.getTaskById(2);
        Assertions.assertEquals("Эпик", epicInManager2.getName(), "Эпик в менеджере изменен извне");

        Subtask subtaskInManager2 = (Subtask) taskManager.getTaskById(3);
        Assertions.assertEquals("Подзадача", subtaskInManager2.getName(), "Подзадача в менеджере изменена извне");

        saveFileContentCheck = "tasksInFileBackedTaskManagerProtectedFromOuterChanges";
    }
}
