import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SubtaskTest {
    @Test
    void subtaskCanNotBeSelfEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, 1);
        subtask.setId(1); // Устанавливаем конкретный id для проверки
        assertThrows(IllegalArgumentException.class, () -> tm.addSubtask(subtask)); // Ожидаем исключение IllegalArgumentException
    } // В методе InMemoryTaskManager.addSubtask() есть проверка
} // Поскольку epicId = 1 и id = 1, условие выполняется → выбрасывается IllegalArgumentException