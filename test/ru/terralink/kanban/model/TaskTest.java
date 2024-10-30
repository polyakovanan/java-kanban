package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

}