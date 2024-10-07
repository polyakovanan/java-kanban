package ru.terralink.kanban.model;

public class Subtask extends Task{

    private int epicId;
    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epicId = epic.getId();
    }
    public Subtask(int id, String name, String description, Epic epic) {
        super(id, name, description);
        this.epicId = epic.getId();
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }


    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId  +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public TaskType getType() { return TaskType.SUBTASK; }
}