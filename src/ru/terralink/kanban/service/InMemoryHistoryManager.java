package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> taskHistory;
    private int maxSize;
    public InMemoryHistoryManager(int size){
        this.maxSize = size;
        taskHistory = new LinkedList<>();
    }
    @Override
    public void add(Task task) {
        if (taskHistory.size() == maxSize){
            taskHistory.remove(0);
        }
        taskHistory.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return new LinkedList<>(taskHistory);
    }

    public int getMaxSize() {
        return maxSize;
    }
}
