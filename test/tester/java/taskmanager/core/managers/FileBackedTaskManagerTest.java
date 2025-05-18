package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.exceptions.ManagerSaveException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest {
    @Test
    public void testFileOperations() {
        // Проверяем, что невалидный файл вызывает исключение
        assertThrows(ManagerSaveException.class, () -> {
            File invalidFile = new File("invalid_path.csv");
            FileBackedTaskManager.loadFromFile(invalidFile);
        });
    }
}
