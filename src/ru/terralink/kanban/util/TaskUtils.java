package ru.terralink.kanban.util;

import ru.terralink.kanban.model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static ru.terralink.kanban.model.TaskType.*;

public class TaskUtils {
    public static final String TEXT_FILE_HEADER = "id,type,name,status,description,epic,startTime,duration,endTime";
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    public static final int SECONDS_IN_MINUTE = 60;
    private TaskUtils() {

    }

    public static String toString(Task task) {
        return String.format("%s,%s,%s,%s,%s,%s,%s,%s", task.getId(), task.getType(), task.getName(), task.getStatus(),
                task.getDescription(), task.getType() == SUBTASK ? ((Subtask)task).getEpicId() : "",
                task.getStartTime() != null ? task.getStartTime().format(DATE_TIME_FORMATTER) : "",
                task.getDuration() != null ? task.getDuration().getSeconds() / SECONDS_IN_MINUTE : "");
    }

    public static Task fromString(String value) throws IllegalArgumentException {

        String[] elements = value.split(",", -1);
        if (elements.length != 8) {
            throw new IllegalArgumentException("Количество элементов в строке не соответствует модели данных");
        }

        int id;
        try {
            id = Integer.parseInt(elements[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Не удалось прочитать id объекта");
        }

        Optional<TaskType> type = TaskType.parseTaskType(elements[1]);
        if (!type.isPresent()) {
            throw new IllegalArgumentException("Невалидный тип объекта");
        }

        String name = elements[2];

        Optional<TaskStatus> status = TaskStatus.parseTaskStatus(elements[3]);
        if (!status.isPresent()) {
            throw new IllegalArgumentException("Невалидный статус объекта");
        }

        String description = elements[4];

        int epicId = 0;
        if (type.get() == SUBTASK) {
            try {
                epicId = Integer.parseInt(elements[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Не удалось прочитать id эпика объекта");
            }
        }

        LocalDateTime startTime = null;
        if (!elements[6].isBlank()) {
            try {
                startTime = LocalDateTime.parse(elements[6], DATE_TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Не удалось прочитать дату начала объекта");
            }
        }

        Duration duration = null;
        if (!elements[7].isBlank()) {
            try {
                duration = Duration.ofMinutes(Integer.parseInt(elements[7]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Не удалось прочитать продолжительность объекта");
            }
        }

        Task parsedObject = null;
        switch (type.get()) {
            case TASK -> {
                parsedObject = new Task(id, name, description);
            }
            case SUBTASK -> {
                parsedObject = new Subtask(id, name, description, epicId);
            }
            case EPIC -> {
                parsedObject = new Epic(id, name, description);
            }
        }

        parsedObject.setStatus(status.get());
        parsedObject.setStartTime(startTime);
        parsedObject.setDuration(duration);

        return parsedObject;
    }
}
