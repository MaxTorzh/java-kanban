package taskmanager.core.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.core.exceptions.ManagerSaveException;


import java.io.File;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Тестовый класс для проверки реализации FileBackedTaskManager.
 * Проверка корректности работы с файлом: загрузка, сохранение и обработка ошибок.
 */
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    /**
     * Проверка, что при попытке загрузить данные из несуществующего или недоступного файла
     * выбрасывается исключение ManagerSaveException.
     */
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {
        // Создание временного файла
        tempFile = File.createTempFile("task-manager-test", ".csv");
        tempFile.deleteOnExit(); // Удаление после завершения теста
        // Инициализация менеджера
        manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
    }

    @Test
    public void testFileOperations() {
        assertThrows(ManagerSaveException.class, () -> {
            File invalidFile = new File("invalid_path.csv");
            FileBackedTaskManager.loadFromFile(invalidFile);
        });
    }
}
