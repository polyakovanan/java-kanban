package kanban.Tasks;

public class Subtask extends Task{

    private Epic epic;
    public Subtask(String name, String description, Epic epic) {
        super(name, description);
        this.epic = epic;
    }
    public Subtask(int id, String name, String description, Epic epic) {
        super(id, name, description);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    public void setEpic(Epic epic) {
        this.epic = epic;
    }


    @Override
    public String toString() {
        return "Subtask{" +
                "epic=" + (epic != null ? epic.getId() : "null")  +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status=" + status +
                '}';
    }
}
