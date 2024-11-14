package ru.terralink.kanban.util;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.terralink.kanban.model.*;

public class TaskUtilsTest {
    String stringifyCheck;
    @Test
    void taskUtilsStringifiesTask() {
        Task task = new Task(1, "Задача 1", "Задача 1");
        String taskStringify = TaskUtils.toString(task);
        Assertions.assertEquals(taskStringify, "1,TASK,Задача 1,NEW,Задача 1,", "TaskUtils некорректно преобразует задачу в строку");
    }

    @Test
    void taskUtilsStringifiesEpic() {
        Epic epic = new Epic(1, "Эпик 1", "Эпик 1");
        String taskStringify = TaskUtils.toString(epic);
        Assertions.assertEquals(taskStringify, "1,EPIC,Эпик 1,NEW,Эпик 1,", "TaskUtils некорректно преобразует эпик в строку");
    }

    @Test
    void taskUtilsStringifiesSubtask() {
        Subtask subtask = new Subtask(1, "Подзадача 1", "Подзадача 1", 2);
        String taskStringify = TaskUtils.toString(subtask);
        Assertions.assertEquals(taskStringify, "1,SUBTASK,Подзадача 1,NEW,Подзадача 1,2", "TaskUtils некорректно преобразует подзадачу в строку");
    }

    @Test
    void taskUtilsParseCorrectTask() {
        Task task = TaskUtils.fromString("1,TASK,Задача 1,NEW,Описание 1,");
        Assertions.assertEquals(task.getId(), 1, "TaskUtils неверно парсит id задачи из строки");
        Assertions.assertEquals(task.getName(), "Задача 1", "TaskUtils неверно парсит имя задачи из строки");
        Assertions.assertEquals(task.getDescription(), "Описание 1", "TaskUtils неверно парсит описание задачи из строки");
        Assertions.assertEquals(task.getStatus(), TaskStatus.NEW, "TaskUtils неверно парсит статус задачи из строки");
        Assertions.assertEquals(task.getType(), TaskType.TASK, "TaskUtils неверно парсит тип задачи из строки");
    }

    @Test
    void taskUtilsParseCorrectEpic() {
        Epic epic = (Epic) TaskUtils.fromString("1,EPIC,Эпик 1,NEW,Описание 1,");
        Assertions.assertEquals(epic.getId(), 1, "TaskUtils неверно парсит id эпика из строки");
        Assertions.assertEquals(epic.getName(), "Эпик 1", "TaskUtils неверно парсит имя эпика из строки");
        Assertions.assertEquals(epic.getDescription(), "Описание 1", "TaskUtils неверно парсит описание эпика из строки");
        Assertions.assertEquals(epic.getStatus(), TaskStatus.NEW, "TaskUtils неверно парсит статус эпика из строки");
        Assertions.assertEquals(epic.getType(), TaskType.TASK, "TaskUtils неверно парсит тип эпика из строки");
    }

    @Test
    void taskUtilsParseCorrectSubtask() {
        Subtask subtask = (Subtask) TaskUtils.fromString("1,SUBTASK,Подзадача 1,NEW,Описание 1,2");
        Assertions.assertEquals(subtask.getId(), 1, "TaskUtils неверно парсит id подзадачи из строки");
        Assertions.assertEquals(subtask.getName(), "Подзадача 1", "TaskUtils неверно парсит имя подзадачи из строки");
        Assertions.assertEquals(subtask.getDescription(), "Описание 1", "TaskUtils неверно парсит описание подзадачи из строки");
        Assertions.assertEquals(subtask.getStatus(), TaskStatus.NEW, "TaskUtils неверно парсит статус подзадачи из строки");
        Assertions.assertEquals(subtask.getType(), TaskType.TASK, "TaskUtils неверно парсит тип подзадачи из строки");
        Assertions.assertEquals(subtask.getEpicId(), TaskType.TASK, "TaskUtils неверно парсит родительский подзадачи из строки");
    }

    @Test
    void taskUtilsThrowExceptionsForMalformedTaskData() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskUtils.fromString("asdfg"), "TaskUtils не выбрасывает исключение при парсинге некорректной строки");
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskUtils.fromString("Один,SUBTASK,Подзадача 1,NEW,Описание 1,2"), "TaskUtils не выбрасывает исключение при парсинге некорректного id");
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskUtils.fromString("1,YOLO,Подзадача 1,NEW,Описание 1,2"), "TaskUtils не выбрасывает исключение при парсинге некорректного типа задачи");
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskUtils.fromString("1,SUBTASK,Подзадача 1,OLD,Описание 1,2"), "TaskUtils не выбрасывает исключение при парсинге некорректного статуса задачи");
        Assertions.assertThrows(IllegalArgumentException.class, () -> TaskUtils.fromString("1,SUBTASK,Подзадача 1,NEW,Описание 1,Два"), "TaskUtils не выбрасывает исключение при парсинге некорректного id родительского эпика");
    }
}
