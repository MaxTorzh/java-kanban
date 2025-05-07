package taskmanager.core.exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException(String message, Throwable e) {
        super(message, e);
    }
}
