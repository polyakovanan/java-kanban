package ru.terralink.kanban.service;

import ru.terralink.kanban.exception.ManagerSaveException;
import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.model.TaskType;
import ru.terralink.kanban.util.TaskUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File saveFile;

    public FileBackedTaskManager(File saveFile) {
        super();
        this.saveFile = saveFile;
    }

    public static FileBackedTaskManager loadFromFile(File saveFile) throws IOException, IllegalArgumentException {
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(saveFile);
        try {
            String fileContent = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
            String[] lines = fileContent.split(System.lineSeparator());
            for (String line : lines) {
                try {
                    Task task = TaskUtils.fromString(line);
                    fileBackedTaskManager.createTask(task);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(String.format("Ошибка в строке '%s': %s", line, e.getMessage()));
                }
            }
        } catch (IOException e) {
            throw new IOException("Ошибка чтения файла: " + e.getMessage());
        }

        return fileBackedTaskManager;
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

    private void save() {
        try (FileWriter fileWriter = new FileWriter(this.saveFile, StandardCharsets.UTF_8)) {
            fileWriter.write("id,type,name,status,description,epic");
            fileWriter.write(System.lineSeparator());
            for (Task task : this.taskStorage.get(TaskType.TASK).values()){
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
            //эпики обязательно должны быть записаны до подзадач, иначе потом будут проблемы с чтением
            for (Task task : this.taskStorage.get(TaskType.EPIC).values()){
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
            for (Task task : this.taskStorage.get(TaskType.SUBTASK).values()){
                fileWriter.write(TaskUtils.toString(task));
                fileWriter.write(System.lineSeparator());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить в файл: " + e.getMessage());
        }
    }
}
