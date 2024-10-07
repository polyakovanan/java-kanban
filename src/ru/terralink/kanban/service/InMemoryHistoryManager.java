package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final ArrayList<Task> taskHistory;
    private int maxSize;
    public InMemoryHistoryManager(int size){
        this.maxSize = size;
        taskHistory = new ArrayList<>();
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
        return taskHistory;
    }

    public int getMaxSize() {
        return maxSize;
    }
}
