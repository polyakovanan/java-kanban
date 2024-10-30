package ru.terralink.kanban.model;

import java.util.HashMap;
import java.util.Map;

/* Добавляем эпику ссылку на список его подзадач. Сохраним их в хэш-мапе,
* аналагично тому, как все задачи хранит TaskManager для того, чтобы унифицировать то,
* как на фронт отдается списки задач
 */

public class Epic extends Task{
    private final Map<Integer, Subtask> subtasks;

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
        calcStatus();
    }

    public void removeSubtask(int id) {
        subtasks.remove(id);
        calcStatus();
    }

    public void clearSubtasks() {
        subtasks.clear();
        calcStatus();
    }

    private void calcStatus() {
        int subtaskCount = subtasks.keySet().size();
        if (subtaskCount == 0) {
            this.status = TaskStatus.NEW;
            return;
        }

        int newCount = 0;
        int doneCount = 0;
        for (Subtask subtask : subtasks.values()) {
            TaskStatus subtaskStatus = subtask.getStatus();
            if (subtaskStatus == TaskStatus.NEW) {
                newCount++;
            } else if (subtaskStatus == TaskStatus.DONE) {
                doneCount++;
            } else {
                this.status = TaskStatus.IN_PROGRESS;
                return;
            }
        }

        if (newCount == subtaskCount) {
            this.status = TaskStatus.NEW;
        } else if (doneCount == subtaskCount) {
            this.status = TaskStatus.DONE;
        } else {
            this.status = TaskStatus.IN_PROGRESS;
        }
    }

    @Override
    public Object clone() {
        Epic epic = new Epic(this.id, this.name, this.description);
        for (Subtask subtask : this.subtasks.values()) {
            epic.addSubtask((Subtask) subtask.clone());
        }
        return epic;
    }

    @Override
    public void setStatus(TaskStatus status) {
        throw new UnsupportedOperationException("Невозможно явно установить статус эпика, так как это расчетное значение");
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", subtasks=" + subtasks.keySet() +
                '}';
    }

    @Override
    public TaskType getType() { return TaskType.EPIC; }
}
