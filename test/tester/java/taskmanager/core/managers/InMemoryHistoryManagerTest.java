package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {
    LocalDateTime baseTime = TestData.BASE_TIME;
    @Test
    void historyKeepsPreviousTaskVersions() { // Тест на сохранение предыдущей версии
        HistoryManager hm = new InMemoryHistoryManager();
        InMemoryTaskManager tm = new InMemoryTaskManager(hm); // Связываем историю с менеджером

        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        task1.setId(1);
        tm.addTask(task1);
        tm.getTaskById(1); // Получаем задачу и добавляем в историю

        Task task2 = new Task("T2", "D2", Status.IN_PROGRESS,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        task2.setId(1);
        tm.updateTask(task2);
        tm.getTaskById(1);

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size());
        assertEquals(Status.IN_PROGRESS, history.get(0).getStatus());
    }

    @Test
    void testOrderOfTasks() { // Тест на порядок в истории
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        task1.setId(1);
        Task task2 = new Task("T2", "D2", Status.IN_PROGRESS,
                Duration.ofMinutes(30), baseTime);
        task2.setId(2);

        hm.add(task1);
        hm.add(task2);

        List<Task> history = hm.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveTask() { // Тест на удаление
        HistoryManager hm = new InMemoryHistoryManager();
        Task task = new Task("T", "D", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        hm.add(task);
        hm.remove(task.getId());
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testEmptyHistory() { // Тест на пустую историю
        HistoryManager hm = new InMemoryHistoryManager();
        assertEquals(0, hm.getHistory().size());
    }

    @Test
    void testNoDuplicatesSameId() { // Тест на отсутствие дубликатов
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        task1.setId(1);
        Task task2 = new Task("T2", "D2", Status.IN_PROGRESS,
                Duration.ofMinutes(30), baseTime);
        task2.setId(1); // Тот же id

        hm.add(task1);
        hm.add(task2); // Удаляет task1 и добавляет task2

        List<Task> history = hm.getHistory();
        assertEquals(1, history.size()); // Только task2
        assertEquals(task2, history.get(0));
    }

    @Test
    void lastViewIsAddedToTheEnd() { // Тест на добавление в конец истории
        HistoryManager hm = new InMemoryHistoryManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofMinutes(30), baseTime);

        task1.setId(1);
        task2.setId(2);

        hm.add(task1); // Добавляем первый просмотр
        hm.add(task2); // Добавляем второй просмотр

        List<Task> history = hm.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(1)); // Последний должен быть в конце
    }
}
