package taskmanager.core.model;

import org.junit.jupiter.api.Test;
import taskmanager.core.managers.InMemoryTaskManager;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SubtaskTest {
    LocalDateTime baseTime = TestData.BASE_TIME;
    @Test
    void subtaskCanNotBeSelfEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, 1,
                Duration.ofMinutes(30), baseTime);
        subtask.setId(1); // Устанавливаем конкретный id для проверки
        assertThrows(IllegalArgumentException.class, () -> tm.addSubtask(subtask)); // Ожидаем исключение IllegalArgumentException
    } // В методе InMemoryTaskManager.addSubtask() есть проверка
} // Поскольку epicId = 1 и id = 1, условие выполняется → выбрасывается IllegalArgumentException