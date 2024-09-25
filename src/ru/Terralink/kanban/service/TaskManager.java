package ru.Terralink.kanban.service;

import ru.Terralink.kanban.model.Epic;
import ru.Terralink.kanban.model.Subtask;
import ru.Terralink.kanban.model.Task;
import ru.Terralink.kanban.model.TaskType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TaskManager {

    private int idCounter = 0; //id задачи уникален между всеми существующими задачами независимо от типа
    private final HashMap<TaskType, HashMap<Integer, Task>> taskStorage;

    public TaskManager(){
        taskStorage = new HashMap<>();
        for (TaskType type : TaskType.values())
            taskStorage.put(type, new HashMap<>());
    }

    //Вернем список с целевым типом задачи
    public ArrayList<Task> getTasksByType(TaskType type){
        return new ArrayList<>(taskStorage.get(type).values());
    }

    /*Полностью очистим хэш-мапу с целевым типом задачи.
    * Вернем true, чтобы обозначить успешность процесса для фронта.
    * Сейчас нет сценариев с false, но это может быть заготовкой для обработки
    * ошибок в новой потенциальной логике.*/
    public boolean removeTasksByType(TaskType type){
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        if (type == TaskType.EPIC) {
            //Если очистили все эпики, то все подзадачи тоже удалились.
            taskStorage.get(TaskType.SUBTASK).clear();
        } else if (type == TaskType.SUBTASK) {
            //Если очистили все подзадачи, то все эпики тоже опустели. Очистим в них ссылки на подзадачи
            for (Task task : taskStorage.get(TaskType.EPIC).values()){
                Epic epic = (Epic)task;
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
                    //Доверимся фронту и посчитаем, что эпик создается перед созданием подзадач
                    //и следовательно список его подзадач пуст (ничего каскадно создавать не надо)
                    Epic epic = (Epic) task;
                    epic.setId(++idCounter);
                    tasks.put(epic.getId(), epic);

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
                    Epic targetEpic = (Epic)taskStorage.get(TaskType.EPIC).get(subtask.getEpicId());
                    targetEpic.addSubtask(subtask);
                    return idCounter;
                }
            }
        }

        return -1;
    }

    /*Если фронт по какой-то причине не может отдать тип задачи
    * определим его сами. Почему бы и нет*/
    public int createTask(Task task){
        return createTaskByType(task, task.getType());
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
                    //всегда передобавляем новую подзадачу, чтобы стриггерить пересчет статуса эпика
                    Subtask originalSubtask = (Subtask) tasks.get(id);
                    Subtask newSubtask = (Subtask) task;
                    Epic newEpic = (Epic)taskStorage.get(TaskType.EPIC).get(newSubtask.getEpicId());
                    if (originalSubtask.getEpicId() != newSubtask.getEpicId()){
                        Epic originalEpic = (Epic)taskStorage.get(TaskType.EPIC).get(originalSubtask.getEpicId());
                        originalEpic.removeSubtask(originalSubtask.getId());
                    }
                    newEpic.addSubtask(newSubtask);
                    tasks.put(id, task);
                    return true;
                }
            }
        }

        return false;
    }

    /*Тут тоже можем сами определить тип обновляемой задачи*/
    public boolean updateTaskById(Task task, int id){
        return updateTaskByIdAndType(task, id, task.getType());
    }

    /*И в очередной раз, следуя бизнес-логике, вернем ошибку, если удалять нечего.
    * Хоть этот процесс никак не вредит технической составляющей процесса*/
    public boolean deleteTaskByIdAndType(int id, TaskType type){
        HashMap<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (tasks.containsKey(id)) {
                    //если удаляем эпик, то надо удалить все его подзадачи
                    Epic epic = (Epic) tasks.get(id);
                    HashMap<Integer, Subtask> epicSubtasks = epic.getSubtasks();
                    if (epicSubtasks != null) {
                        HashMap<Integer, Task> subtasks = taskStorage.get(TaskType.SUBTASK);
                        for (Integer subId : epicSubtasks.keySet()){
                            subtasks.remove(subId);
                        }
                    }

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
                    Epic epic = (Epic)taskStorage.get(TaskType.EPIC).get(subtask.getEpicId());
                    epic.removeSubtask(id);
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
    public ArrayList<Subtask> getSubtasksByEpic(int id) {
        HashMap<Integer, Task> tasks = taskStorage.get(TaskType.EPIC);
        if (tasks.containsKey(id)) {
            return new ArrayList<>(((Epic)tasks.get(id)).getSubtasks().values());
        }

        return new ArrayList<>();
    }

    private void updateSubtasksByEpic(Epic originalEpic, Epic newEpic){
        Set<Integer> originalSubtasks = originalEpic.getSubtasks().keySet();
        Set<Integer> newSubtasks = newEpic.getSubtasks().keySet();

        //удаляем у все подзадачи, которых больше нет в списке у эпика
        for(Integer originalId : originalSubtasks){
            if (!newSubtasks.contains(originalId) && taskStorage.get(TaskType.SUBTASK).get(originalId) != null){
                taskStorage.get(TaskType.SUBTASK).remove(originalId);
            }
        }

        //добавляем у всех подзадач, которых теперь появились в списке у эпика ссылку на эпик
        for(Integer newId : newSubtasks){
            if (!originalSubtasks.contains(newId) && taskStorage.get(TaskType.SUBTASK).get(newId) != null){
                Subtask subtask = (Subtask) taskStorage.get(TaskType.SUBTASK).get(newId);
                subtask.setEpicId(newEpic.getId());
            }
        }
    }
}
