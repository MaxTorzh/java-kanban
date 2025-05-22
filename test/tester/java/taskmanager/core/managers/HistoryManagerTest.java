package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.model.Task;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    private HistoryManager manager = new InMemoryHistoryManager();

    /**
     * Проверяет, что при создании нового менеджера история просмотров пустая.
     */
    @Test
    public void testEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty());
    }

    /**
     * Проверяет, что дубликаты задач не добавляются в историю.
     * Добавление одинаковой задачи дважды должно привести к наличию только одной копии.
     */
    @Test
    public void testDuplicateTasks() {
        Task task = new Task("T", "D");
        manager.add(task);
        manager.add(task);
        assertEquals(1, manager.getHistory().size()); // Дубликаты не добавляются
    }

    /**
     * Проверяет корректное удаление задачи из истории по её ID.
     * Удаление задачи из начала списка должно привести к её отсутствию в истории.
     */
    @Test
    public void testRemoveFromHistory() {
        Task task1 = new Task("T1", "D1");
        Task task2 = new Task("T2", "D2");
        task1.setId(1);
        task2.setId(2);
        manager.add(task1);
        manager.add(task2);
        manager.remove(1); // Удаление из начала
        assertFalse(manager.getHistory().contains(task1));
    }
}
