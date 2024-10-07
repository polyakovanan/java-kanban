package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskStatus;

class TaskTest {

    @Test
    void TasksWithSameIdEqual() {
        Task task1 = new Task(1, "Задача 1", "Задача 1");
        Task task2 = new Task(1, "Задача 2", "Задача 2");
        Assertions.assertEquals(task1, task2, "Задачи не равны");
    }

    @Test
    void SubtasksWithSameIdEqual() {
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask Subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask Subtask2 = new Subtask(2, "Подзадача 2", "Подзадача 2", epic);
        Assertions.assertEquals(Subtask1, Subtask2, "Подзадачи не равны");
    }

    @Test
    void EpicsWithSameIdEqual() {
        Epic epic1 = new Epic(1, "Эпик 1", "Эпик 1");
        Epic epic2 = new Epic(1, "Эпик 2", "Эпик 2");
        Assertions.assertEquals(epic1, epic2, "Эпики не равны");
    }

    @Test
    void EpicWorkWithSubtaskListCorrectly() {
        Epic epic = new Epic(1, "Эпик 1", "Эпик 1");
        Subtask subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = new Subtask(3, "Подзадача 2", "Подзадача 2", epic);

        Assertions.assertEquals(0, epic.getSubtasks().values().size(), "Не пустой список подзадач у нового эпика");

        epic.addSubtask(subtask1);
        Assertions.assertNotNull(epic.getSubtasks().get(2), "Подзадача не добавилась в эпик");

        epic.addSubtask(subtask2);
        epic.removeSubtask(2);
        Assertions.assertNull(epic.getSubtasks().get(2), "Подзадача не удалилась из эпика");
        Assertions.assertNotNull(epic.getSubtasks().get(3), "Список подзадач эпика пуст после удаления отдельного значения");

        epic.clearSubtasks();
        Assertions.assertNull(epic.getSubtasks().get(3), "Список подзадач эпика не очистился");
    }

    @Test
    void EpicCalcsStatusCorrectly() {
        Epic epic = new Epic(1, "Эпик 1", "Эпик 1");
        Assertions.assertEquals(TaskStatus.NEW, epic.getStatus(), "Новый эпик не в статусе NEW");

        Subtask subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = new Subtask(3, "Подзадача 2", "Подзадача 2", epic);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        Assertions.assertEquals(TaskStatus.NEW, epic.getStatus(), "Эпик с новыми подзадачами не в статусе NEW");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        epic.addSubtask(subtask1);
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Эпик с подзадачей IN_PROGRESS не в статусе IN_PROGRESS");

        subtask1.setStatus(TaskStatus.DONE);
        epic.addSubtask(subtask1);
        Assertions.assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "Эпик у которого не все подзадачи в DONE не в статусе IN_PROGRESS");

        subtask2.setStatus(TaskStatus.DONE);
        epic.addSubtask(subtask2);
        Assertions.assertEquals(TaskStatus.DONE, epic.getStatus(), "Эпик у которого все подзадачи в DONE не в статусе DONE");

    }
}