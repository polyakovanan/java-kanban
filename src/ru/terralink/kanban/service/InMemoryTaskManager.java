package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    protected int idCounter = 0; //id задачи уникален между всеми существующими задачами независимо от типа
    protected final Map<TaskType, Map<Integer, Task>> taskStorage;
    protected final HistoryManager historyManager;
    protected final TreeSet<Task> prioritizedTasks;

    public InMemoryTaskManager() {
        taskStorage = new HashMap<>();
        Arrays.stream(TaskType.values())
                .forEach(type -> taskStorage.put(type, new HashMap<>()));

       historyManager = Managers.getDefaultHistory();
       prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    }

    //Вернем список с целевым типом задачи

    @Override
    public List<Task> getTasksByType(TaskType type) {
        return new ArrayList<>(taskStorage.get(type).values());
    }

    /*Полностью очистим хэш-мапу с целевым типом задачи.
    * Вернем true, чтобы обозначить успешность процесса для фронта.
    * Сейчас нет сценариев с false, но это может быть заготовкой для обработки
    * ошибок в новой потенциальной логике.*/

    @Override
    public boolean removeTasksByType(TaskType type) {
        Map<Integer, Task> tasks = taskStorage.get(type);
        if (type == TaskType.EPIC) {
            //Если очистили все эпики, то все подзадачи тоже удалились.
            taskStorage.get(TaskType.SUBTASK).clear();
        } else if (type == TaskType.SUBTASK) {
            //Если очистили все подзадачи, то все эпики тоже опустели. Очистим в них ссылки на подзадачи
            taskStorage.get(TaskType.EPIC).values().stream()
                    .map(epic -> (Epic) epic)
                    .forEach(Epic::clearSubtasks);
        }

        tasks.clear();
        return true;
    }

    /*Дадим возможность фронту запросить задачу по ее id и типу,
    * чтобы сократить время поиска по трем коллекциям */

    @Override
    public Task getTaskByIdAndType(int id, TaskType type) {
        Task task = taskStorage.get(type).get(id);
        if (task != null) {
            historyManager.add(task);
            return (Task) task.clone();
        }
        return null;
    }

    /*Или просто по id, если фронту неизвестен тип задачи.
    * Тогда пройдемся по всем типам и вернем задачу как только она
    * где-то нашлась или null, если id еще не завели*/

    @Override
    public Task getTaskById(int id) {
        Optional<Task> taskOptional = Arrays.stream(TaskType.values())
                .map(type -> getTaskByIdAndType(id, type))
                .filter(task -> task != null)
                .findAny();

        if (taskOptional.isPresent()) {
            return (Task) taskOptional.get().clone();
        }

        return null;
    }

    /*Аналогичная история. Даем фронту создать задачу с указанием типа.
    * Если такой id уже есть, то "СОЗДАТЬ" мы его не можем
    * (с точки зрения бизнес-логики - это уже будет обновление)
    * поэтому вернем id созданной задачи. Иначе -1*/

    @Override
    public int createTaskByType(Task task, TaskType type) {
        Map<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                //Доверимся фронту и посчитаем, что эпик создается перед созданием подзадач
                //и следовательно список его подзадач пуст (ничего каскадно создавать не надо)
                Epic epic = (Epic) task;
                epic.setId(++idCounter);
                tasks.put(epic.getId(), (Epic) epic.clone());

                return idCounter;
            }
            case TASK -> {
                task.setId(++idCounter);
                Task clone = (Task) task.clone();
                tasks.put(task.getId(), clone);
                if (!validateTaskDeadlines(clone)) {
                    return -1;
                }
                if (clone.getStartTime() != null) {
                    prioritizedTasks.add(clone);
                }
                return idCounter;
            }
            case SUBTASK -> {
                //для подзадачи находим ее эпик и добавляем в него ссылку на нее
                //(подразумеваем, что нельзя создать одним запросом и подзадачу, и ее эпик)
                Subtask subtask = (Subtask) task;
                Epic targetEpic = (Epic)taskStorage.get(TaskType.EPIC).get(subtask.getEpicId());
                if (targetEpic == null) {
                    return -1;
                }

                subtask.setId(++idCounter);
                Subtask clone = (Subtask) subtask.clone();
                tasks.put(subtask.getId(), clone);
                if (!validateTaskDeadlines(clone)) {
                    return -1;
                }

                if (clone.getStartTime() != null) {
                    prioritizedTasks.add(clone);
                }
                targetEpic.addSubtask(clone);
                return idCounter;
            }
        }

        return -1;
    }

    /*Если фронт по какой-то причине не может отдать тип задачи
    * определим его сами. Почему бы и нет*/

    @Override
    public int createTask(Task task) {
        return createTaskByType(task, task.getType());
    }

    /*Логика аналогична созданию задачи. Если задачи нет,
    * то это не "ОБНОВЛЕНИЕ", а вставка. Поэтому вернем ошибку, если задачи
    * с таким id нет*/

    @Override
    public boolean updateTaskByIdAndType(Task task, int id, TaskType type) {
        Map<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (tasks.containsKey(id)) {
                    final Epic originalEpic = (Epic) tasks.get(id);
                    originalEpic.setName(task.getName());
                    originalEpic.setDescription(task.getDescription());
                    return true;
                }
            }
            case TASK -> {
                if (tasks.containsKey(id)) {
                    Task clone = (Task) task.clone();
                    tasks.put(id, clone);
                    if (!validateTaskDeadlines(clone)) {
                        return false;
                    }
                    if (clone.getStartTime() != null) {
                        prioritizedTasks.add(clone);
                    }
                    return true;
                }
            }
            case SUBTASK -> {
                if (tasks.containsKey(id)) {
                    //проверяем, сменилась ли у подзадачи ссылка на эпик
                    //и если сменилась, то удаляем ее из старого эпика и добавляем в новый
                    //всегда передобавляем новую подзадачу, чтобы стриггерить пересчет статуса эпика
                    Subtask originalSubtask = (Subtask) tasks.get(id);
                    Subtask clone = (Subtask) task.clone();
                    Epic newEpic = (Epic)taskStorage.get(TaskType.EPIC).get(clone.getEpicId());
                    if (originalSubtask.getEpicId() != clone.getEpicId()) {
                        Epic originalEpic = (Epic)taskStorage.get(TaskType.EPIC).get(originalSubtask.getEpicId());
                        originalEpic.removeSubtask(originalSubtask.getId());
                    }
                    newEpic.addSubtask(clone);
                    tasks.put(id, clone);
                    if (!validateTaskDeadlines(clone)) {
                        return false;
                    }
                    if (clone.getStartTime() != null) {
                        prioritizedTasks.add(clone);
                    }
                    return true;
                }
            }
        }

        return false;
    }

    /*Тут тоже можем сами определить тип обновляемой задачи*/

    @Override
    public boolean updateTaskById(Task task, int id) {
        return updateTaskByIdAndType(task, id, task.getType());
    }

    /*И в очередной раз, следуя бизнес-логике, вернем ошибку, если удалять нечего.
    * Хоть этот процесс никак не вредит технической составляющей процесса*/

    @Override
    public boolean deleteTaskByIdAndType(int id, TaskType type) {
        Map<Integer, Task> tasks = taskStorage.get(type);
        switch (type) {
            case EPIC -> {
                if (tasks.containsKey(id)) {
                    //если удаляем эпик, то надо удалить все его подзадачи
                    final Epic epic = (Epic) tasks.remove(id);
                    historyManager.remove(id);
                    Map<Integer, Subtask> epicSubtasks = epic.getSubtasks();
                    Map<Integer, Task> subtasks = taskStorage.get(TaskType.SUBTASK);

                    epicSubtasks.keySet().stream()
                            .forEach(subId -> {
                                prioritizedTasks.remove(subtasks.remove(subId));
                                historyManager.remove(subId);
                            });

                    return true;
                }
            }
            case TASK -> {
                if (tasks.containsKey(id)) {
                    prioritizedTasks.remove(tasks.remove(id));
                    historyManager.remove(id);
                    return true;
                }
            }
            case SUBTASK -> {
                if (tasks.containsKey(id)) {
                    //если подзадачу удалили, надо убрать ссылку на нее из ее эпика
                    final Subtask subtask = (Subtask) tasks.remove(id);
                    prioritizedTasks.remove(subtask);
                    Epic epic = (Epic)taskStorage.get(TaskType.EPIC).get(subtask.getEpicId());
                    epic.removeSubtask(id);
                    historyManager.remove(id);
                    return true;
                }
            }
        }
        return false;
    }

    /*Если не отдали тип задачи - перебираем коллекции по существующим типам*/

    @Override
    public boolean deleteTaskById(int id) {
        return Arrays.stream(TaskType.values())
                .filter(type -> deleteTaskByIdAndType(id, type))
                .findAny()
                .isPresent();
    }

    /*Если есть такой эпик - отдаем его список подзадач*/

    @Override
    public List<Subtask> getSubtasksByEpic(int id) {
        Map<Integer, Task> tasks = taskStorage.get(TaskType.EPIC);
        if (tasks.containsKey(id)) {
            return new ArrayList<>(((Epic)tasks.get(id)).getSubtasks().values());
        }

        return new ArrayList<>();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return prioritizedTasks.stream().collect(Collectors.toList());
    }

    @Override
    public boolean validateTaskDeadlines(Task task) {
        if (task.getStartTime() == null || prioritizedTasks.isEmpty()) {
            return true;
        }

        return !prioritizedTasks.stream()
                .anyMatch(prioritizedTask -> prioritizedTask.checkTimeIntersections(task));
    }
}
