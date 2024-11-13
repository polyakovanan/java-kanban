package ru.terralink.kanban.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.model.*;

import java.util.List;

public class InMemoryTaskManagerTest {

    @Test
    void managersReturnNotNullTaskManagerByDefault(){
        TaskManager taskManager = Managers.getDefault();
        Assertions.assertNotNull(taskManager, "Managers не отдает менеджер задач по умолчанию");
    }

    @Test
    void taskManagerCreatesTasksAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Задача 1", "Задача 1");
        Task task2 = new Task("Задача 2", "Задача 2");

        int id = taskManager.createTaskByType(task1, TaskType.TASK);
        Assertions.assertEquals(1, id, "Менеджер задач создал неверный id задачи");

        id = taskManager.createTaskByType(task2, TaskType.TASK);
        Assertions.assertEquals(2, id, "Менеджер задач создал неверный id задачи");

        List<Task> tasks = taskManager.getTasksByType(TaskType.TASK);
        Assertions.assertEquals(2, tasks.size(), "Менеджер задач не добавил задачи в список");
    }

    @Test
    void taskManagerCreatesEpicsAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getDefault();

        Epic epic1 = new Epic("Эпик 1", "Эпик 1");
        Epic epic2 = new Epic("Эпик 2", "Эпик 2");

        int id = taskManager.createTaskByType(epic1, TaskType.EPIC);
        Assertions.assertEquals(1, id, "Менеджер задач создал неверный id эпика");

        id = taskManager.createTaskByType(epic2, TaskType.EPIC);
        Assertions.assertEquals(2, id, "Менеджер задач создал неверный id эпика");

        List<Task> tasks = taskManager.getTasksByType(TaskType.EPIC);
        Assertions.assertEquals(2, tasks.size(), "Менеджер задач не добавил эпики в список");
    }

    @Test
    void taskManagerCreatesSubtasksAndAssignsIncrementalIds(){
        TaskManager taskManager = Managers.getDefault();
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
    }

    @Test
    void taskManagerDoesNotCreateSubtaskWithAbsentEpic(){
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        taskManager.removeTasksByType(TaskType.EPIC);
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.EPIC).size(), "Менеджер задач не очищает список эпиков");

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        int id = taskManager.createTask(subtask);

        Assertions.assertEquals(-1, id, "Менеджер задач не вернул ошибку при создании подзадачи с несуществующим эпиком");
        Assertions.assertEquals(0, taskManager.getTasksByType(TaskType.SUBTASK).size(), "Менеджер задач создает подзадачу с несуществующим эпиком");

    }

    @Test
    void taskManagerDeterminesCreatedType() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.TASK).size(), "Менеджер задач не закидывает задачу в нужный тип");
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.EPIC).size(), "Менеджер задач не закидывает эпик в нужный тип");
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);
        Assertions.assertEquals(1, taskManager.getTasksByType(TaskType.SUBTASK).size(), "Менеджер задач не закидывает подзадачу в нужный тип");
    }

    @Test
    void taskManagerGetsTaskById() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);
        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        Assertions.assertEquals("Задача", taskManager.getTaskById(task.getId()).getName(), "Менеджер задач не может найти задачу по id");
        Assertions.assertEquals("Эпик", taskManager.getTaskById(epic.getId()).getName(), "Менеджер задач не может найти эпик по id");
        Assertions.assertEquals("Подзадача", taskManager.getTaskById(subtask.getId()).getName(), "Менеджер задач не может найти подзадачу по id");

    }

    @Test
    void taskManagerUpdatesTaskById() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);

        task.setName("Бабача");

        taskManager.updateTaskById(task, task.getId());

        Task returnTask = taskManager.getTaskById(task.getId());

        Assertions.assertEquals("Бабача", returnTask.getName(), "Менеджер задач не может обновить задачу по id");
    }

    @Test
    void taskManagerUpdatesSubtaskReferenceToEpicById() {
        TaskManager taskManager = Managers.getDefault();
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
    }

    @Test
    void taskManagerUpdatesEpicById() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        epic.setName("Кекик");

        taskManager.updateTaskById(epic, epic.getId());
        Assertions.assertEquals("Кекик", epic.getName(), "Менеджер задач не может обновить эпик по id");
    }

    @Test
    void taskManagerUpdatesEpicSubtaskListAndCalcsStatus() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        subtask.setStatus(TaskStatus.IN_PROGRESS);

        taskManager.updateTaskByIdAndType(subtask, subtask.getId(),TaskType.SUBTASK);

        Epic returnEpic = (Epic) taskManager.getTaskById(epic.getId());

        Assertions.assertEquals(1, returnEpic.getSubtasks().size(), "Менеджер задач не может добавить подзадачу в эпик");
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, returnEpic.getStatus(), "Менеджер задач не может пересчитать статус эпика после добавления подзадачи");
    }

    @Test
    void taskManagerClearsTasksByTypes() {
        TaskManager taskManager = Managers.getDefault();
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

    }

    @Test
    void taskManagerDeletesTaskById() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("Задача", "Задача");
        taskManager.createTask(task);
        taskManager.deleteTaskById(task.getId());
        Assertions.assertNull(taskManager.getTaskById(task.getId()), "Менеджер задач не удаляет задачу по id");
    }

    @Test
    void taskManagerDeletesEpicAndItsSubtasksById() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        taskManager.deleteTaskById(epic.getId());
        Assertions.assertNull(taskManager.getTaskById(epic.getId()), "Менеджер задач не удаляет эпик по id");
        Assertions.assertNull(taskManager.getTaskById(subtask.getId()), "Менеджер задач не удаляет подзадачи удаленного эпика");
    }

    @Test
    void taskManagerDeletesSubtaskAndFromEpicById() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);

        taskManager.deleteTaskById(subtask.getId());
        Assertions.assertNull(taskManager.getTaskById(subtask.getId()), "Менеджер задач не удаляет подзадачу по id");

        Epic returnEpic = (Epic) taskManager.getTaskByIdAndType(epic.getId(), TaskType.EPIC);

        Assertions.assertEquals(0, returnEpic.getSubtasks().size(), "Менеджер задач не удаляет удаленные подзадачи из эпика");
    }

    @Test
    void taskManagerReturnsEpicSubtasksById() {
        TaskManager taskManager = Managers.getDefault();
        Epic epic = new Epic("Эпик", "Эпик");
        taskManager.createTask(epic);

        Subtask subtask = new Subtask("Подзадача", "Подзадача", epic);
        taskManager.createTask(subtask);
        List<Subtask> subtasks = taskManager.getSubtasksByEpic(epic.getId());

        Assertions.assertNotEquals(0, subtasks.size(), "Менеджер задач вернул пустой список подзадач эпика");
        Assertions.assertEquals(subtask, subtasks.get(0), "Менеджер задач вернул неверный список подзадач эпика");
    }

    @Test
    void taskManagerReturnsAddsTaskHistory() {
        TaskManager taskManager = Managers.getDefault();
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
    }

    @Test
    void TasksInTaskManagerProtectedFromOuterChanges(){
        TaskManager taskManager = Managers.getDefault();
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

    }
}
