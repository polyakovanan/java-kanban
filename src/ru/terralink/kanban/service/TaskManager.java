package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;

import java.util.List;
import java.util.List;

public interface TaskManager {

    List<Task> getTasksByType(TaskType type);

    boolean removeTasksByType(TaskType type);

    Task getTaskByIdAndType(int id, TaskType type);

    Task getTaskById(int id);

    int createTaskByType(Task task, TaskType type);

    int createTask(Task task);

    boolean updateTaskByIdAndType(Task task, int id, TaskType type);

    boolean updateTaskById(Task task, int id);

    boolean deleteTaskByIdAndType(int id, TaskType type);

    boolean deleteTaskById(int id);

    List<Subtask> getSubtasksByEpic(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    boolean validateTaskDeadlines(Task task);
}
