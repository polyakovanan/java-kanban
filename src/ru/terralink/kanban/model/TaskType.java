package ru.terralink.kanban.model;

public enum TaskType {
    TASK,
    EPIC,
    SUBTASK,
    OPTIONAL;

    public static TaskType parseTaskType(String type) {
        for (TaskType taskType : values()) {
            if (type.equals(taskType.toString())) {
                return taskType;
            }
        }
        return OPTIONAL;
    }
}
