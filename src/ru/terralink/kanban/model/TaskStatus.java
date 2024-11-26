package ru.terralink.kanban.model;

import java.util.Arrays;
import java.util.Optional;

public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE;

    public static Optional<TaskStatus> parseTaskStatus(String status) {
        return Arrays.stream(values()).filter(taskStatus -> status.equals(taskStatus.toString())).findAny();
    }
}
