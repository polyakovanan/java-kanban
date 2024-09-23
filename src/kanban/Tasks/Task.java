package kanban.Tasks;

import java.util.Objects;

/* Базовая реализация задачи. Такая не является абстрактной и экземпляр создать можно.
* Но при этому у нее не будет ни потомков, ни родителей с точки зрения бизнес логики.
* Подобная задача является небольшой самостоятельной единицей
 */

public class Task {

    private int id;
    private String name;
    private String description;
    private TaskStatus status;

    public Task(int id, String name, String description){
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = TaskStatus.NEW;
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

    public int getId() {
        return id;
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
}