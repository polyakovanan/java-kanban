package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    ArrayList<Task> getTasksByType(TaskType type);
    boolean removeTasksByType(TaskType type);
    Task getTaskByIdAndType(int id, TaskType type);
    Task getTaskById(int id);
    int createTaskByType(Task task, TaskType type);
    int createTask(Task task);
    boolean updateTaskByIdAndType(Task task, int id, TaskType type);
    boolean updateTaskById(Task task, int id);
    boolean deleteTaskByIdAndType(int id, TaskType type);
    boolean deleteTaskById(int id);
    ArrayList<Subtask> getSubtasksByEpic(int id);
    List<Task> getHistory();
}
