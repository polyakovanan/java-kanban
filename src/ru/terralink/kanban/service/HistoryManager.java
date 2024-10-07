package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;

import java.util.List;

public interface HistoryManager {

    void add(Task task);
    List<Task> getHistory();
    int getMaxSize();
}
