package kanban;

import kanban.Tasks.Epic;
import kanban.Tasks.Subtask;
import kanban.Tasks.Task;
import kanban.Tasks.TaskType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TaskManager {

    private static int idCounter = 0; //id задачи уникален между всеми существующими задачами независимо от типа
    private HashMap<TaskType, HashMap<Integer, Task>> taskStorage;

    public TaskManager(){
        taskStorage = new HashMap<>();
        for (TaskType type : TaskType.values())
            taskStorage.put(type, new HashMap<>());
    }

    //Вернем полностью хэш-мапу с целевым типом задачи
    public Map<Integer, Task> getTasksByType(TaskType type){
        return taskStorage.get(type);
    }

    /*Полностью очистим хэш-мапу с целевым типом задачи.
    * Вернем true, чтобы обозначить успешность процесса для фронта.
    * Сейчас нет сценариев с false, но это может быть заготовкой для обработки
    * ошибок в новой потенциальной логике.*/
    public boolean removeTasksByType(TaskType type){
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        if (type == TaskType.EPIC) {
            //Если очистили все эпики, то все подзадачи тоже осиротели. Очистим в них ссылки на эпики
            for (Integer id : tasks.keySet()) {
                Subtask subtask = (Subtask) tasks.get(id);
                subtask.setEpic(null);
            }
        } else if (type == TaskType.SUBTASK) {
            //Если очистили все подзадачи, то все эпики тоже опустели. Очистим в них ссылки на подзадачи
            for (Integer id : tasks.keySet()){
                Epic epic = (Epic) tasks.get(id);
                epic.clearSubtasks();
            }
        }

        tasks.clear();
        return true;
    }

    /*Дадим возможность фронту запросить задачу по ее id и типу,
    * чтобы сократить время поиска по трем коллекциям */
    public Task getTaskByIdAndType(int id, TaskType type){
        return taskStorage.get(type).get(id);
    }

    /*Или просто по id, если фронту неизвестен тип задачи.
    * Тогда пройдемся по всем типам и вернем задачу как только она
    * где-то нашлась или null, если id еще не завели*/
    public Task getTaskById(int id){
        for (TaskType type : TaskType.values()) {
            Task task = getTaskByIdAndType(id, type);
            if (task != null){
                return task;
            }
        }
        return null;
    }

    /*Аналогичная история. Даем фронту создать задачу с указанием типа.
    * Если такой id уже есть, то "СОЗДАТЬ" мы его не можем
    * (с точки зрения бизнес-логики - это уже будет обновление)
    * поэтому вернем id созданной задачи. Иначе -1*/
    public int createTaskByType(Task task, TaskType type){
        int id = idCounter + 1;
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (!tasks.containsKey(id)) {
                    Epic epic = (Epic) task;
                    epic.setId(++idCounter);
                    tasks.put(epic.getId(), epic);
                    //для эпика также смотрим список его подзадач и обновляем в них ссылку на эпик
                    //(подразумеваем, что нельзя создать одним запросом и эпик, и его подзадачи)
                    //а еще сюда как будто транзакцию хочется, на случай если что-то пойдет не так в процессе
                    createSubtasksByEpic(epic);

                    //пересчитываем статус эпика
                    epic.calcStatus();
                    return idCounter;
                }
            }
            case TASK -> {
                if (!tasks.containsKey(id)) {
                    task.setId(++idCounter);
                    tasks.put(task.getId(), task);
                    return idCounter;
                }
            }
            case SUBTASK -> {
                if (!tasks.containsKey(id)) {
                    task.setId(++idCounter);
                    tasks.put(task.getId(), task);
                    //для подзадачи находим ее эпик и добавляем в него ссылку на нее
                    //(подразумеваем, что нельзя создать одним запросом и подзадачу, и ее эпик)
                    Subtask subtask = (Subtask) task;
                    subtask.getEpic().addSubtask(subtask);
                    //пересчитываем статус эпика
                    subtask.getEpic().calcStatus();
                    return idCounter;
                }
            }
        }

        return -1;
    }

    /*Если фронт по какой-то причине не может отдать тип задачи
    * определим его сами. Почему бы и нет*/
    public int createTask(Task task){
        if (task instanceof Epic) {
            return createTaskByType(task, TaskType.EPIC);
        } else if (task instanceof Subtask) {
            return createTaskByType(task, TaskType.SUBTASK);
        } else {
            return createTaskByType(task, TaskType.TASK);
        }
    }

    /*Логика аналогична созданию задачи. Если задачи нет,
    * то это не "ОБНОВЛЕНИЕ", а вставка. Поэтому вернем ошибку, если задачи
    * с таким id нет*/
    public boolean updateTaskByIdAndType(Task task, int id, TaskType type){
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (tasks.containsKey(id)) {
                    //проверяем, сменился ли у эпика список подзадач
                    Epic originalEpic = (Epic) tasks.get(id);
                    Epic newEpic = (Epic) task;
                    updateSubtasksByEpic(originalEpic, newEpic);
                    tasks.put(id, newEpic);

                    //пересчитываем статус эпика
                    newEpic.calcStatus();
                    return true;
                }
            }
            case TASK -> {
                if (tasks.containsKey(id)) {
                    tasks.put(id, task);
                    return true;
                }
            }
            case SUBTASK -> {
                if (tasks.containsKey(id)) {
                    //проверяем, сменилась ли у подзадачи ссылка на эпик
                    //и если сменилась, то удаляем ее из старого эпика и добавляем в новый
                    Subtask originalSubtask = (Subtask) tasks.get(id);
                    Subtask newSubtask = (Subtask) task;
                    if (originalSubtask.getEpic().getId() != newSubtask.getEpic().getId()){
                        originalSubtask.getEpic().removeSubtask(originalSubtask.getId());
                        newSubtask.getEpic().addSubtask(newSubtask);
                    }

                    //пересчитываем статус эпика
                    newSubtask.getEpic().calcStatus();
                    tasks.put(id, task);
                    return true;
                }
            }
        }

        return false;
    }

    /*Тут тоже можем сами определить тип обновляемой задачи*/
    public boolean updateTaskById(Task task, int id){
        if (task instanceof Epic) {
            return updateTaskByIdAndType(task, id, TaskType.EPIC);
        } else if (task instanceof Subtask) {
            return updateTaskByIdAndType(task, id, TaskType.SUBTASK);
        } else {
            return updateTaskByIdAndType(task, id, TaskType.TASK);
        }
    }

    /*И в очередной раз, следуя бизнес-логике, вернем ошибку, если удалять нечего.
    * Хоть этот процесс никак не вредит технической составляющей процесса*/
    public boolean deleteTaskByIdAndType(int id, TaskType type){
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (tasks.containsKey(id)) {
                    //если удаляем эпик, то надо удалить ссылку на него из всех его подзадач
                    Epic epic = (Epic) tasks.get(id);
                    HashMap<Integer, Subtask> subtasks = epic.getSubtasks();
                    if (subtasks != null) {
                        for (Integer subId : subtasks.keySet()){
                            subtasks.get(subId).setEpic(null);
                        }
                    }

                    epic.calcStatus();
                    tasks.remove(id);
                    return true;
                }
            }
            case TASK -> {
                if (tasks.containsKey(id)) {
                    tasks.remove(id);
                    return true;
                }
            }
            case SUBTASK -> {
                if (tasks.containsKey(id)) {
                    //если подзадачу удалили, надо убрать ссылку на нее из ее эпика
                    Subtask subtask = (Subtask) tasks.get(id);
                    subtask.getEpic().removeSubtask(id);
                    //пересчитываем статус эпика
                    subtask.getEpic().calcStatus();
                    tasks.remove(id);
                    return true;
                }
            }
        }
        return false;
    }

    /*Если не отдали тип задачи - перебираем коллекции по существующим типам*/
    public boolean deleteTaskById(int id){
        for(TaskType type : TaskType.values()) {
            if (deleteTaskByIdAndType(id, type))
                return true;
        }

        return false;
    }

    /*Если есть такой эпик - отдаем его список подзадач*/
    public Map<Integer, Subtask> getSubtasksByEpic(int id) {
        HashMap<Integer, Task> tasks = taskStorage.get(TaskType.EPIC);
        if (tasks.containsKey(id)) {
            return ((Epic) tasks.get(id)).getSubtasks();
        }
        return null;
    }

    private void createSubtasksByEpic(Epic epic){
        if(epic.getSubtasks() != null){
            for (Integer subId : epic.getSubtasks().keySet()){
                Subtask subtask = (Subtask) taskStorage.get(TaskType.SUBTASK).get(subId);
                if(subtask != null){
                    subtask.setEpic(epic);
                }
            }
        }
    }
    private void updateSubtasksByEpic(Epic originalEpic, Epic newEpic){
        Set<Integer> originalSubtasks = originalEpic.getSubtasks().keySet();
        Set<Integer> newSubtasks = newEpic.getSubtasks().keySet();

        //удаляем у всех подзадач, которых больше нет в списке у эпика ссылку на эпик
        for(Integer originalId : originalSubtasks){
            if (!newSubtasks.contains(originalId) && taskStorage.get(TaskType.SUBTASK).get(originalId) != null){
                Subtask subtask = (Subtask) taskStorage.get(TaskType.SUBTASK).get(originalId);
                subtask.setEpic(null);
            }
        }

        //добавляем у всех подзадач, которых теперь появились в списке у эпика ссылку на эпик
        for(Integer newId : newSubtasks){
            if (!originalSubtasks.contains(newId) && taskStorage.get(TaskType.SUBTASK).get(newId) != null){
                Subtask subtask = (Subtask) taskStorage.get(TaskType.SUBTASK).get(newId);
                subtask.setEpic(newEpic);
            }
        }
    }
}
