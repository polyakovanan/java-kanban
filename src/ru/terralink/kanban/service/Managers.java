package ru.terralink.kanban.service;

import ru.terralink.kanban.model.Task;
import ru.terralink.kanban.util.TaskUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

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
            if (lines.length > 0 && !lines[0].equals(TaskUtils.TEXT_FILE_HEADER)) {
                throw new IllegalArgumentException(String.format("Неверный заголовок файла: " + lines[0]));
            }

            Arrays.stream(lines)
                    .filter(line -> !line.equals(TaskUtils.TEXT_FILE_HEADER))
                    .forEach(line -> {
                        try {
                            Task task = TaskUtils.fromString(line);
                            fileBackedTaskManager.addParsedTask(task);
                            } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException(String.format("Ошибка в строке '%s': %s", line, e.getMessage()));
                            }
                    });

            fileBackedTaskManager.save();
        } catch (IOException e) {
            throw new IOException("Ошибка чтения файла: " + e.getMessage());
        }

        return fileBackedTaskManager;
    }

}
