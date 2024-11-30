package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubtaskTest {

    @Test
    void SubtasksWithSameIdEqual() {
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = new Subtask(2, "Подзадача 2", "Подзадача 2", epic);
        Assertions.assertEquals(subtask1, subtask2, "Подзадачи не равны");
    }

    @Test
    void SubtaskCloneWorkProperly(){
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask subtask2 = (Subtask) subtask1.clone();
        Assertions.assertEquals(subtask1, subtask2, "Клон подзадачи не равен оригиналу");
        Assertions.assertNotSame(subtask1, subtask2, "Клон подзадачи ссылается на оригинал");
    }

    @Test
    void SubtaskCalculatesEndDateProperly(){
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача 1", 1);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        Assertions.assertEquals(subtask1.getEndTime(), LocalDateTime.of(2024, 1, 1,0,0), "Задача без продолжительности неправильно вычисляет дату конца");
        subtask1.setDuration(Duration.ofMinutes(60));
        Assertions.assertEquals(subtask1.getEndTime(), LocalDateTime.of(2024, 1, 1,1,0), "Задача с заданной продолжительностью неправильно вычисляет дату конца");
    }

    @Test
    void SubtaskChecksIntersectionsCorrectly(){
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask subtask1 = new Subtask(1, "Подзадача 1", "Подзадача 1", 1);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        subtask1.setDuration(Duration.ofMinutes(120));

        Subtask subtask2 = new Subtask(2, "Подзадача 2", "Подзадача 2", 1);
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 1,2,0));
        subtask2.setDuration(Duration.ofMinutes(120));

        Assertions.assertFalse(subtask1.checkTimeIntersections(subtask2), "Подзадача неправильно определила непересекающиеся интервалы");

        Subtask subtask3 = new Subtask(3, "Подзадача 3", "Подзадача 3", 1);
        subtask3.setStartTime(LocalDateTime.of(2024, 1, 1,1,0));
        subtask3.setDuration(Duration.ofMinutes(120));

        Assertions.assertTrue(subtask1.checkTimeIntersections(subtask3), "Подзадача неправильно определила пересекающиеся интервалы");
    }
}
