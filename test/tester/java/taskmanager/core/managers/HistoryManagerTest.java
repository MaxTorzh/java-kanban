package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.model.Task;

import static org.junit.jupiter.api.Assertions.*;

public class HistoryManagerTest {
    private HistoryManager manager = new InMemoryHistoryManager();

    @Test
    public void testEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    public void testDuplicateTasks() {
        Task task = new Task("Task", "Desc");
        manager.add(task);
        manager.add(task);
        assertEquals(1, manager.getHistory().size()); // Дубликаты не добавляются
    }

    @Test
    public void testRemoveFromHistory() {
        Task task1 = new Task("Task1", "Desc1");
        Task task2 = new Task("Task2", "Desc2");
        task1.setId(1);
        task2.setId(2);
        manager.add(task1);
        manager.add(task2);
        manager.remove(1); // Удаление из начала
        assertFalse(manager.getHistory().contains(task1));
    }
}
