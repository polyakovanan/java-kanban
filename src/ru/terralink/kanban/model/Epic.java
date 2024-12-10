package ru.terralink.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/* Добавляем эпику ссылку на список его подзадач. Сохраним их в хэш-мапе,
* аналагично тому, как все задачи хранит TaskManager для того, чтобы унифицировать то,
* как на фронт отдается списки задач
 */

public class Epic extends Task {
    private final Map<Integer, Subtask> subtasks;
    private LocalDateTime endTime;

    public Epic(String name, String description) {
        super(name, description);
        subtasks = new HashMap<>();
    }

    public Epic(int id, String name, String description) {
        super(id, name, description);
        subtasks = new HashMap<>();
    }

    public Map<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        calculateStatus();
        calculateDates();
    }

    public void removeSubtask(int id) {
        subtasks.remove(id);
        calculateStatus();
        calculateDates();
    }

    public void clearSubtasks() {
        subtasks.clear();
        calculateStatus();
        calculateDates();
    }

    private void calculateStatus() {
        int subtaskCount = subtasks.keySet().size();
        if (subtaskCount == 0) {
            this.status = TaskStatus.NEW;
            return;
        }

        if (subtasks.values().stream().anyMatch(subtask -> subtask.getStatus() == TaskStatus.IN_PROGRESS)) {
            this.status = TaskStatus.IN_PROGRESS;
            return;
        }

        long newCount = subtasks.values().stream()
                .filter(subtask -> subtask.getStatus() == TaskStatus.NEW).count();

        long doneCount = subtasks.values().stream()
                .filter(subtask -> subtask.getStatus() == TaskStatus.DONE).count();

        if (newCount == subtaskCount) {
            this.status = TaskStatus.NEW;
        } else if (doneCount == subtaskCount) {
            this.status = TaskStatus.DONE;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    private void calculateDates() {
        Optional<LocalDateTime> opStartTime = subtasks.values().stream()
                .filter(subtask -> subtask.getStartTime() != null)
                .map(subtask -> subtask.getStartTime())
                .min(Comparator.naturalOrder());

        this.startTime = opStartTime.isPresent() ? opStartTime.get() : null;

        Optional<LocalDateTime> opEndTime = subtasks.values().stream()
                .filter(subtask -> subtask.getEndTime() != null)
                .map(subtask -> subtask.getEndTime())
                .max(Comparator.naturalOrder());

        this.endTime = opEndTime.isPresent() ? opEndTime.get() : null;

        Duration calcDuration = subtasks.values().stream()
                .filter(subtask -> subtask.getDuration() != null)
                .map(subtask -> subtask.getDuration())
                .reduce(Duration.ZERO, Duration::plus);

        this.duration = calcDuration.toMinutes() == 0 ? null : calcDuration;
    }

    @Override
    public Object clone() {
        Epic epic = new Epic(this.id, this.name, this.description);
        if (subtasks != null) {
            this.subtasks.values().stream()
                    .map(subtask -> subtask.clone())
                    .forEach(subtask -> epic.addSubtask((Subtask) subtask));
        }
        return epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;

    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration == null ? "" : duration.toMinutes()) +
                ", startTime=" + (startTime == null ? "" : startTime.toString()) +
                ", endTime=" + (endTime == null ? "" : endTime.toString()) +
                ", subtasks=" + subtasks +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }
}
