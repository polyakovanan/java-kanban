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

public class FileBackedTaskManagerTest extends TaskManagerTest {
    File saveFile;
    static File loadFile;
    String saveFileContentCheck;
    static final String resourcePath = "resources" + File.separator + "test" + File.separator + "service" + File.separator + "FileBackedTaskManager" + File.separator;

    @BeforeAll
    static void createFileForLoadingCheck(){
        loadFile = Paths.get(resourcePath + "createFileForLoadingCheck").toFile();
    }

    @BeforeEach
    void initTaskManager() {
        try {
            saveFile = Files.createTempFile("fileBackedTest", ".csv").toFile();
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        taskManager = Managers.getFileBackedTaskManager(saveFile);
        Assertions.assertNotNull(taskManager, "Managers не отдает менеджер задач c файлом сохранения");
        saveFileContentCheck = "managersReturnNotNullFileBackedTaskManager";
    }

    @AfterEach
    void checkSaveFileContent() {
        try {
            String saveContent = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
            String checkContent = Files.readString(Path.of(resourcePath + saveFileContentCheck), StandardCharsets.UTF_8);
            Assertions.assertEquals(saveContent, checkContent, "Содержимое файла сохранения не совпадает с проверочными для " + saveFileContentCheck);
        } catch (IOException e) {
            Assertions.fail(saveFileContentCheck + " " + e.getMessage());
        }
    }

    @Test
    void managersLoadFileBackedTaskManagerCorrectly() {
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

    @Override
    @Test
    void taskManagerCreatesTasksAndAssignsIncrementalIds() {
        super.taskManagerCreatesTasksAndAssignsIncrementalIds();
        saveFileContentCheck = "fileBackedTaskManagerCreatesTasksAndAssignsIncrementalIds";
    }

    @Override
    @Test
    void taskManagerCreatesEpicsAndAssignsIncrementalIds() {
        super.taskManagerCreatesEpicsAndAssignsIncrementalIds();
        saveFileContentCheck = "fileBackedTaskManagerCreatesEpicsAndAssignsIncrementalIds";
    }

    @Override
    @Test
    void taskManagerCreatesSubtasksAndAssignsIncrementalIds() {
        super.taskManagerCreatesSubtasksAndAssignsIncrementalIds();
        saveFileContentCheck = "fileBackedTaskManagerCreatesSubtasksAndAssignsIncrementalIds";
    }

    @Override
    @Test
    void taskManagerDoesNotCreateSubtaskWithAbsentEpic() {
        super.taskManagerDoesNotCreateSubtaskWithAbsentEpic();
        saveFileContentCheck = "fileBackedTaskManagerDoesNotCreateSubtaskWithAbsentEpic";
    }

    @Override
    @Test
    void taskManagerDeterminesCreatedType() {
        super.taskManagerDeterminesCreatedType();
        saveFileContentCheck = "fileBackedTaskManagerDeterminesCreatedType";
    }

    @Override
    @Test
    void taskManagerGetsTaskById() {
        super.taskManagerGetsTaskById();
        saveFileContentCheck = "fileBackedTaskManagerGetsTaskById";
    }

    @Override
    @Test
    void taskManagerUpdatesTaskById() {
        super.taskManagerUpdatesTaskById();
        saveFileContentCheck = "fileBackedTaskManagerUpdatesTaskById";
    }

    @Override
    @Test
    void taskManagerUpdatesSubtaskReferenceToEpicById() {
        super.taskManagerUpdatesSubtaskReferenceToEpicById();
        saveFileContentCheck = "fileBackedTaskManagerUpdatesSubtaskReferenceToEpicById";
    }

    @Override
    @Test
    void taskManagerUpdatesEpicById() {
        super.taskManagerUpdatesEpicById();
        saveFileContentCheck = "fileBackedTaskManagerUpdatesEpicById";
    }

    @Override
    @Test
    void taskManagerUpdatesEpicSubtaskListAndCalcsStatus() {
        super.taskManagerUpdatesEpicSubtaskListAndCalcsStatus();
        saveFileContentCheck = "fileBackedTaskManagerUpdatesEpicSubtaskListAndCalcsStatus";
    }

    @Override
    @Test
    void taskManagerClearsTasksByTypes() {
        super.taskManagerClearsTasksByTypes();
        saveFileContentCheck = "fileBackedTaskManagerClearsTasksByTypes";
    }

    @Override
    @Test
    void taskManagerDeletesTaskById() {
        super.taskManagerDeletesTaskById();
        saveFileContentCheck = "fileBackedTaskManagerDeletesTaskById";
    }

    @Override
    @Test
    void taskManagerDeletesEpicAndItsSubtasksById() {
        super.taskManagerDeletesEpicAndItsSubtasksById();
        saveFileContentCheck = "fileBackedTaskManagerDeletesEpicAndItsSubtasksById";
    }

    @Override
    @Test
    void taskManagerDeletesSubtaskAndFromEpicById() {
        super.taskManagerDeletesSubtaskAndFromEpicById();
        saveFileContentCheck = "fileBackedTaskManagerDeletesSubtaskAndFromEpicById";
    }

    @Override
    @Test
    void taskManagerReturnsEpicSubtasksById() {
        super.taskManagerReturnsEpicSubtasksById();
        saveFileContentCheck = "fileBackedTaskManagerReturnsEpicSubtasksById";
    }

    @Override
    @Test
    void taskManagerReturnsAddsTaskHistory() {
        super.taskManagerReturnsAddsTaskHistory();
        saveFileContentCheck = "fileBackedTaskManagerReturnsAddsTaskHistory";
    }

    @Override
    @Test
    void TasksInTaskManagerProtectedFromOuterChanges() {
        super.TasksInTaskManagerProtectedFromOuterChanges();
        saveFileContentCheck = "TasksInFileBackedTaskManagerProtectedFromOuterChanges";
    }
}
