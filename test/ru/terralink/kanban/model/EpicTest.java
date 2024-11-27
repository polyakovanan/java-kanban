package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class EpicTest {

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

    @Test
    void EpicCloneWorkProperly(){
        Epic epic1 = new Epic(1, "Эпик 1", "Эпик 1");
        Epic epic2 = (Epic) epic1.clone();
        Assertions.assertEquals(epic1, epic2, "Клон эпика не равен оригиналу");
        Assertions.assertNotSame(epic1, epic2, "Клон эпика ссылается на оригинал");

        Map<Integer, Subtask> subtasks1 = epic1.getSubtasks();
        Map<Integer, Subtask> subtasks2 = epic2.getSubtasks();

        for (Integer id : subtasks1.keySet()){
            Assertions.assertEquals(subtasks1.get(id), subtasks2.get(id), "Клон подзадачи эпика не равняется оригиналу");
            Assertions.assertNotSame(subtasks1.get(id), subtasks2.get(id), "Клон подзадачи эпика ссылается на оригинал");
        }
    }

    @Test
    void EpicCalculatesDatesProperly() {
        Epic epic = new Epic(1, "Эпик 1", "Эпик 1");

        Subtask subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = new Subtask(3, "Подзадача 2", "Подзадача 2", epic);

        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        Assertions.assertNull(epic.getStartTime(), "Эпик с новыми подзадачами без времени начала имеет время начала");
        Assertions.assertNull(epic.getDuration(), "Эпик с новыми подзадачами без продолжительности имеет продолжительность");
        Assertions.assertNull(epic.getEndTime(), "Эпик с новыми подзадачами без времени конца имеет время конца");

        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));

        epic = new Epic(1, "Эпик 1", "Эпик 1");
        epic.addSubtask(subtask1);
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 1,0,0), epic.getStartTime(), "Эпик не высчитал время начала по подзадаче");
        Assertions.assertNull(epic.getDuration(),  "Эпик с подзадачами без продолжительности имеет продолжительность");

        subtask1.setDuration(Duration.ofMinutes(60));
        epic = new Epic(1, "Эпик 1", "Эпик 1");
        epic.addSubtask(subtask1);
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 1,0,0), epic.getStartTime(), "Эпик неправильно высчитал время начала по подзадаче");
        Assertions.assertEquals(Duration.ofMinutes(60), epic.getDuration(),  "Эпик неправильно посчитал свою продолжительность по подзадаче");
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 1,1,0), epic.getEndTime(),  "Эпик неправильно посчитал время конца по подзадаче");

        epic = new Epic(1, "Эпик 1", "Эпик 1");
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 2,0,0));
        subtask2.setDuration(Duration.ofMinutes(60));
        epic.addSubtask(subtask1);
        epic.addSubtask(subtask2);
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 1,0,0), epic.getStartTime(), "Эпик неправильно высчитал время начала по нескольким подзадачам");
        Assertions.assertEquals(Duration.ofMinutes(120), epic.getDuration(),  "Эпик неправильно посчитал свою продолжительность по нескольким подзадачам");
        Assertions.assertEquals(LocalDateTime.of(2024, 1, 2,1,0), epic.getEndTime(),  "Эпик неправильно посчитал время конца по нескольким подзадачам");

    }

    @Test
    void EpicChecksIntersectionsCorrectly(){
        Epic epic1 = new Epic(1,"Эпик 1", "Эпик 1");
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача 1", 1);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        subtask1.setDuration(Duration.ofMinutes(120));
        epic1.addSubtask(subtask1);

        Epic epic2 = new Epic(1,"Эпик 2", "Эпик 2");
        Subtask subtask2 = new Subtask(2, "Подзадача 2", "Подзадача 2", 2);
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1,2,0));
        subtask2.setDuration(Duration.ofMinutes(120));
        epic2.addSubtask(subtask2);

        Assertions.assertFalse(epic1.checkTimeIntersections(epic2), "Эпик неправильно определил непересекающиеся интервалы");

        Epic epic3 = new Epic(1,"Эпик 3", "Эпик 3");
        Subtask subtask3 = new Subtask(3, "Подзадача 3", "Подзадача 3", 3);
        subtask3.setStartTime(LocalDateTime.of(2024, 1, 1,1,0));
        subtask3.setDuration(Duration.ofMinutes(120));
        epic3.addSubtask(subtask3);

        Assertions.assertTrue(epic1.checkTimeIntersections(epic3), "Эпик неправильно определил пересекающиеся интервалы");
    }
}
