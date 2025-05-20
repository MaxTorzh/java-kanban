package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.exceptions.ManagerSaveException;


import java.io.File;
import static org.junit.jupiter.api.Assertions.assertThrows;


/**
 * Тестовый класс для проверки реализации FileBackedTaskManager.
 * Проверка корректности работы с файлом: загрузка, сохранение и обработка ошибок.
 */
public class FileBackedTaskManagerTest {
    /**
     * Проверка, что при попытке загрузить данные из несуществующего или недоступного файла
     * выбрасывается исключение ManagerSaveException.
     */
    @Test
    public void testFileOperations() {
        assertThrows(ManagerSaveException.class, () -> {
            File invalidFile = new File("invalid_path.csv");
            FileBackedTaskManager.loadFromFile(invalidFile);
        });
    }
}
