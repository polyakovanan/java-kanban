package ru.terralink.kanban.service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;

import java.util.List;

public class HistoryTest {

    @Test
    void managersReturnNotNullHistoryManagerByDefault(){
        HistoryManager historyManager = Managers.getDefaultHistory();
        Assertions.assertNotNull(historyManager, "Managers не отдает менеджер истории по умолчанию");
    }

    @Test
    void historyManagerAddsTaskInRightOrder(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(1,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(2,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(2)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerReaddsFirstNodeInRightOrder(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        historyManager.add(task);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(3, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(2,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(1,(tasks.get(2)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerReaddsLastNodeInRightOrder(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        historyManager.add(subtask);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(3, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(1,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(2,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(2)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerReaddsMiddleNodeInRightOrder(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);
        historyManager.add(epic);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(3, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(1,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(2,(tasks.get(2)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerRemovesFirstNode(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(1);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(2, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(2,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerRemovesLastNode(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(3);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(2, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(1,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(2,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }

    @Test
    void historyManagerRemovesMiddleNode(){
        HistoryManager historyManager = Managers.getDefaultHistory();

        Task task = new Task(1, "Задача", "Задача");
        Epic epic = new Epic(2, "Эпик", "Эпик");
        Subtask subtask = new Subtask(3, "Подзадача", "Подзадача", epic);
        historyManager.add(task);
        historyManager.add(epic);
        historyManager.add(subtask);

        historyManager.remove(2);

        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(2, tasks.size(), "Менеджер задач добавляет дубликат задачи");
        Assertions.assertEquals(1,(tasks.get(0)).getId(), "Менеджер задач хранит задачи не в том порядке");
        Assertions.assertEquals(3,(tasks.get(1)).getId(), "Менеджер задач хранит задачи не в том порядке");
    }
}
