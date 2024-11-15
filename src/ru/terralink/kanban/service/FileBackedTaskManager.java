package ru.terralink.kanban.service;

import ru.terralink.kanban.exception.ManagerSaveException;
import ru.terralink.kanban.model.Epic;
import ru.terralink.kanban.model.Subtask;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.util.TaskUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public File getSaveFile() {
        return saveFile;
    }

    public FileBackedTaskManager(File saveFile) {
        super();
        this.saveFile = saveFile;
    }

    @Override
    public boolean removeTasksByType(TaskType type) {
        boolean result = super.removeTasksByType(type);
        if (result) {
            save();
        }
        return result;
    }

    @Override
    public int createTaskByType(Task task, TaskType type) {
        int id = super.createTaskByType(task, type);
        if (id > -1) {
            save();
        }
        return id;
    }

    @Override
    public int createTask(Task task) {
        return createTaskByType(task, task.getType());
    }

    @Override
    public boolean updateTaskByIdAndType(Task task, int id, TaskType type) {
        boolean result = super.updateTaskByIdAndType(task, id, type);
        if (result) {
            save();
        }
        return result;
    }

    @Override
    public boolean updateTaskById(Task task, int id) {
        return updateTaskByIdAndType(task, id, task.getType());
    }


    @Override
    public boolean deleteTaskByIdAndType(int id, TaskType type) {
        boolean result = super.deleteTaskByIdAndType(id, type);
        if (result) {
            save();
        }
        return result;
    }

    @Override
    public boolean deleteTaskById(int id) {
        for (TaskType type : TaskType.values()) {
            if (deleteTaskByIdAndType(id, type))
                return true;
        }

        return false;
    }

    public void addParsedTask(Task task) {
        taskStorage.get(task.getType()).put(task.getId(), task);
        if (task.getType() == TaskType.SUBTASK) {
            Subtask subtask = (Subtask) task;
            Epic epic = (Epic) taskStorage.get(TaskType.EPIC).get(subtask.getEpicId());
            if (epic != null) {
                epic.addSubtask(subtask);
            }
        }
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(this.saveFile, StandardCharsets.UTF_8)) {
            fileWriter.write(TaskUtils.TEXT_FILE_HEADER);
            fileWriter.write(System.lineSeparator());
            for (Task task : this.taskStorage.get(TaskType.TASK).values()) {
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
            //эпики обязательно должны быть записаны до подзадач, иначе потом будут проблемы с чтением
            for (Task task : this.taskStorage.get(TaskType.EPIC).values()) {
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
            for (Task task : this.taskStorage.get(TaskType.SUBTASK).values()) {
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить в файл: " + e.getMessage());
        }
    }
}
