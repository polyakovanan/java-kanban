package ru.terralink.kanban.util;

import ru.terralink.kanban.model.*;

public class TaskUtils {
    public static final String TEXT_FILE_HEADER = "id,type,name,status,description,epic";

    private TaskUtils() {

    }

    public static String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,%s", task.getId(), task.getType(), task.getName(), task.getStatus(),
                task.getDescription(), task.getType() == TaskType.SUBTASK ? ((Subtask)task).getEpicId() : "");
    }

    public static Task fromString(String value) throws IllegalArgumentException {
        String[] elements = value.split(",");
        if (elements.length < 5 || elements.length > 6) {
            throw new IllegalArgumentException("Количество элементов в строке не соответствует модели данных");
        }

        int id;
        try {
            id = Integer.parseInt(elements[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Не удалось прочитать id объекта");
        }

        TaskType type = TaskType.parseTaskType(elements[1]);
        if (type == TaskType.OPTIONAL) {
            throw new IllegalArgumentException("Невалидный тип объекта");
        }

        String name = elements[2];

        TaskStatus status = TaskStatus.parseTaskStatus(elements[3]);
        if (status == TaskStatus.OPTIONAL) {
            throw new IllegalArgumentException("Невалидный статус объекта");
        }

        String description = elements[4];

        int epicId = 0;
        if (type == TaskType.SUBTASK) {
            try {
                epicId = Integer.parseInt(elements[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Не удалось прочитать id эпика объекта");
            }
        }

        Task parsedObject = null;
        switch (type) {
            case TASK -> {
                parsedObject = new Task(id, name, description);
                parsedObject.setStatus(status);
            }
            case SUBTASK -> {
                parsedObject = new Subtask(id, name, description, epicId);
                parsedObject.setStatus(status);
            }
            case EPIC -> {
                parsedObject = new Epic(id, name, description);
                parsedObject.setStatus(status);
            }
        }

        return parsedObject;
    }
}
