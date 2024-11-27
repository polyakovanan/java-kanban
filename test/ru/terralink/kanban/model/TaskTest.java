package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

class TaskTest {

    @Test
    void TasksWithSameIdEqual() {
        Task task1 = new Task(1, "Задача 1", "Задача 1");
        Task task2 = new Task(1, "Задача 2", "Задача 2");
        Assertions.assertEquals(task1, task2, "Задачи не равны");
    }

    @Test
    void TaskCloneWorkProperly(){
        Task task1 = new Task(1, "Задача 1", "Задача 1");
        Task task2 = (Task) task1.clone();
        Assertions.assertEquals(task1, task2, "Клон задачи не равен оригиналу");
        Assertions.assertNotSame(task1, task2, "Клон задачи ссылается на оригинал");
    }

    @Test
    void TaskCalculatesEndDateProperly(){
        Task task1 = new Task(1, "Задача 1", "Задача 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        Assertions.assertEquals(task1.getEndTime(), LocalDateTime.of(2024, 1, 1,0,0), "Задача без продолжительности неправильно вычисляет дату конца");
        task1.setDuration(Duration.ofMinutes(60));
        Assertions.assertEquals(task1.getEndTime(), LocalDateTime.of(2024, 1, 1,1,0), "Задача с заданной продолжительностью неправильно вычисляет дату конца");
    }

    @Test
    void TaskChecksIntersectionsCorrectly(){
        Task task1 = new Task(1, "Задача 1", "Задача 1");
        task1.setStartTime(LocalDateTime.of(2024, 1, 1,0,0));
        task1.setDuration(Duration.ofMinutes(120));

        Task task2 = new Task(2, "Задача 2", "Задача 2");
        task2.setStartTime(LocalDateTime.of(2024, 1, 1,2,0));
        task2.setDuration(Duration.ofMinutes(120));

        Assertions.assertFalse(task1.checkTimeIntersections(task2), "Задача неправильно определила непересекающиеся интервалы");

        Task task3 = new Task(3, "Задача 3", "Задача 3");
        task3.setStartTime(LocalDateTime.of(2024, 1, 1,1,0));
        task3.setDuration(Duration.ofMinutes(120));

        Assertions.assertTrue(task1.checkTimeIntersections(task3), "Задача неправильно определила пересекающиеся интервалы");
    }

}