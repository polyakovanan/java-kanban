package kanban;

import kanban.Tasks.Epic;
import kanban.Tasks.Subtask;
import kanban.Tasks.Task;
import kanban.Tasks.TaskType;
import java.util.HashMap;
import java.util.Set;

public class TaskManager {

    private static int idCounter = 0; //id задачи уникален между всеми существующими задачами независимо от типа
    private HashMap<Integer, Task> taskStorage; //три хэш-мапы под каждый тип задач. Не придумал пока другого решения,
    private HashMap<Integer, Task> subtaskStorage; //которое не затруднит последующую работу по id задачи.
    private HashMap<Integer, Task> epicStorage; //возможно, упускаю какое-то изящное решение с вложенными коллекциям

    public TaskManager(){
        taskStorage = new HashMap<>();
        subtaskStorage = new HashMap<>();
        epicStorage = new HashMap<>();
    }

    //Вернем полностью хэш-мапу с целевым типом задачи
    public HashMap<Integer, Task> getTasksByType(TaskType type){
        switch (type) {
            case EPIC -> {
                return epicStorage;
            }
            case TASK -> {
                return taskStorage;
            }
            case SUBTASK -> {
                return subtaskStorage;
            }
        }
        return null;
    }

    /*Полностью очистим хэш-мапу с целевым типом задачи.
    * Вернем true, чтобы обозначить успешность процесса для фронта.
    * Сейчас нет сценариев с false, но это может быть заготовкой для обработки
    * ошибок в новой потенциальной логике.*/
    public boolean removeTasksByType(TaskType type){
        switch (type) {
            case EPIC -> {
                epicStorage.clear();
            }
            case TASK -> {
                taskStorage.clear();
            }
            case SUBTASK -> {
                subtaskStorage.clear();
            }
        }

        return true;
    }

    /*Дадим возможножность фронту запросить задачу по ее id и типу,
    * чтобы сократить время поиска по трем коллекциям */
    public Task getTaskByIdAndType(int id, TaskType type){
        switch (type) {
            case EPIC -> {
                return epicStorage.get(id);
            }
            case TASK -> {
                return taskStorage.get(id);
            }
            case SUBTASK -> {
                return subtaskStorage.get(id);
            }
        }
        return null;
    }

    /*Или просто по id, если фронту неизвестен тип задачи.
    * Тогда пройдемся по всем типам и вернем задачу как только она
    * где-то нашлась или null, если id еще не завели*/
    public Task getTaskById(int id){
        Task task = null;
        for (TaskType type : TaskType.values()) {
            task = getTaskByIdAndType(id, type);
            if (task != null){
                return task;
            }
        }
        return task;
    }

    /*Аналогичная история. Даем фронту создать задачу с указанием типа.
    * Если такой id уже есть, то "СОЗДАТЬ" мы его его не можем
    * (с точки зрения бизнес-логики - это уже будет обновление)
    * поэтому вернем false. Иначе true*/
    public boolean createTaskByType(Task task, TaskType type){
        int id = idCounter + 1;
        switch (type) {
            case EPIC -> {
                if (!epicStorage.containsKey(id)) {
                    Epic epic = (Epic) task;
                    epicStorage.put(++idCounter, epic);
                    //для эпика также смотрим список его подзадач и обновляем в них ссылку на эпик
                    //(подразумеваем, что нельзя создать одним запросом и эпик, и его подзадачи)
                    //а еще сюда как будто транзакцию хочется, на случай если что-то пойдет не так в процессе
                    if(epic.getSubtasks() != null){
                        for (Integer subId : epic.getSubtasks().keySet()){
                            Subtask subtask = (Subtask) subtaskStorage.get(subId);
                            if(subtask != null){
                                subtask.setEpic(epic);
                            }
                        }
                    }
                    //пересчитываем статус эпика
                    epic.calcStatus();
                    return true;
                }
            }
            case TASK -> {
                if (!taskStorage.containsKey(id)) {
                    taskStorage.put(++idCounter, task);
                    return true;
                }
            }
            case SUBTASK -> {
                if (!subtaskStorage.containsKey(id)) {
                    subtaskStorage.put(++idCounter, task);
                    //для подзадачи находим ее эпик и добавляем в него ссылку на нее
                    //(подразумеваем, что нельзя создать одним запросом и подзадачу, и ее эпик)
                    Subtask subtask = (Subtask) task;
                    subtask.getEpic().addSubtask(subtask);
                    //пересчитываем статус эпика
                    subtask.getEpic().calcStatus();
                    return true;
                }
            }
        }

        return false;
    }

    /*Если фронт по какой-то причине не может отдать тип задачи
    * определим его сами. Почему бы и нет*/
    public boolean createTask(Task task){
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
        switch (type) {
            case EPIC -> {
                if (epicStorage.containsKey(id)) {
                    //проверяем, сменился ли у эпика список подзадач
                    Epic originalEpic = (Epic) epicStorage.get(id);
                    Epic newEpic = (Epic) task;
                    Set<Integer> originalSubtasks = originalEpic.getSubtasks().keySet();
                    Set<Integer> newSubtasks = newEpic.getSubtasks().keySet();

                    //удаляем у всех подзадач, которых больше нет в списке у эпика ссылку на эпик
                    for(Integer originalId : originalSubtasks){
                        if (!newSubtasks.contains(originalId) && subtaskStorage.get(originalId) != null){
                            Subtask subtask = (Subtask) subtaskStorage.get(originalId);
                            subtask.setEpic(null);
                        }
                    }

                    //добавляем у всех подзадач, которых теперь появились в списке у эпика ссылку на эпик
                    for(Integer newId : newSubtasks){
                        if (!originalSubtasks.contains(newId) && subtaskStorage.get(newId) != null){
                            Subtask subtask = (Subtask) subtaskStorage.get(newId);
                            subtask.setEpic(newEpic);
                        }
                    }
                    epicStorage.put(id, newEpic);

                    //пересчитываем статус эпика
                    newEpic.calcStatus();
                    return true;
                }
            }
            case TASK -> {
                if (taskStorage.containsKey(id)) {
                    taskStorage.put(id, task);
                    return true;
                }
            }
            case SUBTASK -> {
                if (subtaskStorage.containsKey(id)) {
                    //проверяем, сменилась ли у подзадачи ссылка на эпик
                    //и если сменилась, то удаляем ее из старого эпика и добавляем в новый
                    Subtask originalSubtask = (Subtask) subtaskStorage.get(id);
                    Subtask newSubtask = (Subtask) task;
                    if (originalSubtask.getEpic().getId() != newSubtask.getEpic().getId()){
                        originalSubtask.getEpic().removeSubtask(originalSubtask.getId());
                        newSubtask.getEpic().addSubtask(newSubtask);
                        //пересчитываем статус эпика
                        newSubtask.getEpic().calcStatus();
                    }

                    subtaskStorage.put(id, task);
                    return true;
                }
            }
        }

        return false;
    }

    /*Тут тоже тожем сами определить тип обновляемой задачи*/
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
        switch (type) {
            case EPIC -> {
                if (epicStorage.containsKey(id)) {
                    //если удаляем эпик, то надо удалить ссылку на него из всех его подзадач
                    Epic epic = (Epic) epicStorage.get(id);
                    HashMap<Integer, Subtask> subtasks = epic.getSubtasks();
                    if (subtasks != null) {
                        for (Integer subId : subtasks.keySet()){
                            subtasks.get(subId).setEpic(null);
                        }
                    }
                    epicStorage.remove(id);
                    return true;
                }
            }
            case TASK -> {
                if (taskStorage.containsKey(id)) {
                    taskStorage.remove(id);
                    return true;
                }
            }
            case SUBTASK -> {
                if (subtaskStorage.containsKey(id)) {
                    //если подзадачу удалили, надо убрать ссылку на нее из ее эпика
                    Subtask subtask = (Subtask) subtaskStorage.get(id);
                    subtask.getEpic().removeSubtask(id);
                    //пересчитываем статус эпика
                    subtask.getEpic().calcStatus();
                    subtaskStorage.remove(id);
                    return true;
                }
            }
        }
        return false;
    }

    /*Если не отдали тип задачи - перебираем коллекции по существующим типам*/
    public boolean deleteTaskById(int id){
        boolean isDeleted = false;
        for(TaskType type : TaskType.values()) {
            isDeleted = deleteTaskByIdAndType(id, type);
            if (isDeleted)
                return isDeleted;
        }

        return isDeleted;
    }

    /*Если есть такой эпик - отдаем его список подзадач*/
    public HashMap<Integer, Subtask> getSubtasksByEpic(int id) {
        if (epicStorage.containsKey(id)) {
            return ((Epic) epicStorage.get(id)).getSubtasks();
        }
        return null;
    }
}
