import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    @Test
    void historyKeepsPreviousTaskVersions() {
        HistoryManager hm = new InMemoryHistoryManager();
        InMemoryTaskManager tm = new InMemoryTaskManager(hm); // Связываем историю с менеджером

        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        task1.setId(1);
        tm.addTask(task1);
        tm.getTaskById(1); // Получаем задачу и добавляем в историю

        Task task2 = new Task("Task2", "Desc2", Status.IN_PROGRESS);
        task2.setId(1);
        tm.updateTask(task2);
        tm.getTaskById(1);

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size());
        assertEquals(Status.IN_PROGRESS, history.get(0).getStatus());
    }

    @Test
    void testOrderOfTasks() {
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.IN_PROGRESS);
        task2.setId(2);

        hm.add(task1);
        hm.add(task2);

        List<Task> history = hm.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveTask() {
        HistoryManager hm = new InMemoryHistoryManager();
        Task task = new Task("Task", "Desc", Status.NEW);
        hm.add(task);
        hm.remove(task.getId());
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testEmptyHistory() {
        HistoryManager hm = new InMemoryHistoryManager();
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testNoDuplicatesSameId() {
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setId(1);
        Task task2 = new Task("Task2", "Desc", Status.IN_PROGRESS);
        task2.setId(1); // Тот же id

        hm.add(task1);
        hm.add(task2); // Удаляет task1 и добавляет task2

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size()); // Только task2
        assertEquals(task2, history.get(0));
    }
}
