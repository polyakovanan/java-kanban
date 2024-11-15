package ru.terralink.kanban.model;

public enum TaskStatus {
    NEW,
    IN_PROGRESS,
    DONE,
    OPTIONAL;

    public static TaskStatus parseTaskStatus(String status) {
        for (TaskStatus taskStatus : values()) {
            if (status.equals(taskStatus.toString())) {
                return taskStatus;
            }
        }
        return OPTIONAL;
    }
}
