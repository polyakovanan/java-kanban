package ru.terralink.kanban.exception;

public class ManagerSaveException extends RuntimeException {

    public ManagerSaveException() {
        super();
    }

    public ManagerSaveException(String message) {
        super(message);
    }
}
