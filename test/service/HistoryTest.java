package service;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.service.HistoryManager;
import ru.terralink.kanban.service.Managers;

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
    void historyManagerRemovesFirstAndAddsLastOnMaxSizeExceeded() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        for (int i = 1; i <= historyManager.getMaxSize() + 1; i++) {
            Task task = new Task(i, "Задача " + i, "Задача " + i);
            historyManager.add(task);
        }
        List<Task> tasks = historyManager.getHistory();

        Assertions.assertEquals(2,(tasks.get(0)).getId(), "Менеджер задач не удаляет первый элемент при переполнении");
        Assertions.assertEquals(11,(tasks.get(historyManager.getMaxSize() - 1)).getId(), "Менеджер задач не добавляет последний элемент при переполнении");
    }
}
