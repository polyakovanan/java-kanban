package ru.terralink.kanban.model;

import java.util.Arrays;
import java.util.Optional;

public enum TaskType {
    TASK,
    EPIC,
    SUBTASK;

    public static Optional<TaskType> parseTaskType(String type) {
        return Arrays.stream(values()).filter(taskType -> type.equals(taskType.toString())).findAny();
    }
}
