package ru.terralink.kanban.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SubtaskTest {

    @Test
    void SubtasksWithSameIdEqual() {
        Epic epic = new Epic(1,"Эпик", "Эпик");
        Subtask Subtask1 = new Subtask(2, "Подзадача 1", "Подзадача 1", epic);
        Subtask Subtask2 = new Subtask(2, "Подзадача 2", "Подзадача 2", epic);
        Assertions.assertEquals(Subtask1, Subtask2, "Подзадачи не равны");
    }
}
