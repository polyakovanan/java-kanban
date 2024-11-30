package ru.terralink.kanban.model;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epicId = epic.getId();
    }

    public Subtask(int id, String name, String description, Epic epic) {
        super(id, name, description);
        this.epicId = epic.getId();
    }

    public Subtask(int id, String name, String description, int epicId) {
        super(id, name, description);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public Object clone() {
        Subtask subtask = new Subtask(this.id, this.name, this.description, this.epicId);
        subtask.setStatus(this.getStatus());
        subtask.setStartTime(this.getStartTime());
        subtask.setDuration(this.getDuration());
        return subtask;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                ", duration=" + (duration == null ? "" : duration.toMinutes()) +
                ", startTime=" + (startTime == null ? "" : startTime.toString()) +
                ", epicId=" + epicId +
                '}';
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
