package kanban.Tasks;

import java.util.HashMap;

/* Добавляем эпику ссылку на список его подзадач. Сохраним их в хэш-мапе,
* аналагично тому, как все задачи хранит TaskManager для того, чтобы унифицировать то,
* как на фронт отдается списки задач
 */

public class Epic extends Task{
    private HashMap<Integer, Subtask> subtasks;

    public Epic(int id, String name, String description) {
        super(id, name, description);
        subtasks = new HashMap<>();
    }

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask){
        subtasks.put(subtask.getId(), subtask);
    }

    public void removeSubtask(int id){
        subtasks.remove(id);
    }

    public void calcStatus(){
        int subtaskCount = subtasks.keySet().size();
        if(subtaskCount == 0){
            this.setStatus(TaskStatus.NEW);
            return;
        }

        int newCount = 0;
        int doneCount = 0;
        for (Integer subId : subtasks.keySet()){
            if (subtasks.get(subId).getStatus() != TaskStatus.NEW){
                newCount++;
            }
            if (subtasks.get(subId).getStatus() != TaskStatus.DONE){
                doneCount++;
            }
        }

        if(newCount == subtaskCount){
            this.setStatus(TaskStatus.NEW);
        } else if (doneCount == subtaskCount) {
            this.setStatus(TaskStatus.DONE);
        } else {
            this.setStatus(TaskStatus.IN_PROGRESS);
        }
    }
}
