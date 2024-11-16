package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.util.TaskUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Managers {

    private static final String FILE_BACKED_SAVE_FILE_NAME = "saveFile";
    private static final String FILE_BACKED_SAVE_FILE_EXTENTION = ".csv";

    private Managers() {

    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBackedTaskManager(File saveFile) {
        return new FileBackedTaskManager(saveFile);
    }

    public static TaskManager loadFromFile(File saveFile) throws IOException, IllegalArgumentException {
        String saveFileName = FILE_BACKED_SAVE_FILE_NAME + System.currentTimeMillis() + FILE_BACKED_SAVE_FILE_EXTENTION;
        File currentSaveFile = Files.createFile(Path.of(saveFileName)).toFile();
        FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(currentSaveFile);
        try {
            String fileContent = Files.readString(saveFile.toPath(), StandardCharsets.UTF_8);
            String[] lines = fileContent.split(System.lineSeparator());
            for (int i = 0; i < lines.length; i++) {
                if (i == 0) {
                    if (!lines[i].equals(TaskUtils.TEXT_FILE_HEADER)) {
                        throw new IllegalArgumentException(String.format("Неверный заголовок файла: " + lines[i]));
                    }
                } else {
                    try {
                        Task task = TaskUtils.fromString(lines[i]);
                        fileBackedTaskManager.addParsedTask(task);
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException(String.format("Ошибка в строке '%s': %s", lines[i], e.getMessage()));
                    }
                }
            }
            fileBackedTaskManager.save();
        } catch (IOException e) {
            throw new IOException("Ошибка чтения файла: " + e.getMessage());
        }

        return fileBackedTaskManager;
    }
}
