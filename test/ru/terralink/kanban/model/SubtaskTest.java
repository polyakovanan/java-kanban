package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
}
