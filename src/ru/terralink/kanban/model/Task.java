package ru.terralink.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/* Базовая реализация задачи. Такая не является абстрактной и экземпляр создать можно.
* Но при этому у нее не будет ни потомков, ни родителей с точки зрения бизнес логики.
* Подобная задача является небольшой самостоятельной единицей
 */

public class Task implements Cloneable {

    protected int id;
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    public Task(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration == null ? "" : duration.toMinutes()) +
                ", startTime=" + (startTime == null ? "" : startTime.toString()) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public Object clone() {
        Task task = new Task(this.id, this.name, this.description);
        task.setStatus(this.status);
        task.setStartTime(this.getStartTime());
        task.setDuration(this.getDuration());
        return task;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null) {
            return null;
        } else if (duration == null) {
            return startTime;
        }

        return startTime.plus(duration);
    }

    public boolean checkTimeIntersections(Task task) {
        if (this.getStartTime() == null || task.getStartTime() == null) {
            return false;
        }
        return this.getStartTime().isEqual(task.getStartTime()) ||
                (task.getStartTime().isAfter(this.getStartTime()) && task.getStartTime().isBefore(this.getEndTime())) ||
                (this.getStartTime().isAfter(task.getStartTime()) && this.getStartTime().isBefore(task.getEndTime()));
    }
}
