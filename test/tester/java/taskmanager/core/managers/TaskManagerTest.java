package taskmanager.core.managers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import taskmanager.core.exceptions.TimeConflictException;
import taskmanager.core.model.*;
import taskmanager.core.util.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Абстрактный обобщенный класс для тестирования различных реализаций TaskManager.
 * @param <T> — конкретная реализация TaskManager, например InMemoryTaskManager или FileBackedTaskManager
 */
public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    LocalDateTime baseTime = TestData.BASE_TIME;

    /**
     * Проверка корректности расчета статуса эпика в зависимости от статуса подзадач.
     */
    @Test
    public void testEpicStatusCalculation() {
        Epic epic = new Epic("E", "D"); // Создание объекта
        manager.addEpic(epic); // Добавление в менеджер задач

        // a. Все подзадачи NEW
        Subtask subtask1 = new Subtask("S1", "D1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime);
        manager.addSubtask(subtask1); // Добавление подзадачи со статусом NEW
        assertEquals(Status.NEW, epic.getStatus()); // Проверка, что эпик также равен NEW

        // b. Все подзадачи DONE
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1); // Обновление статуса подзадачи на DONE
        assertEquals(Status.DONE, epic.getStatus()); // Проверка, что эпик также равен DONE

        // c. NEW и DONE
        Subtask subtask2 = new Subtask("S2", "D2", Status.NEW, epic.getId(),
                Duration.ofMinutes(60), baseTime.plusHours(1));
        manager.addSubtask(subtask2); // Добавление второй подзадачи с отличающимся статусом
        assertEquals(Status.IN_PROGRESS, epic.getStatus()); // Проверка, что эпик равен IN_PROGRESS

        // d. Подзадачи IN_PROGRESS
        subtask2.setStatus(Status.IN_PROGRESS); // Изменение статуса второй задачи
        manager.updateSubtask(subtask2); // Добавление в менеджер
        assertEquals(Status.IN_PROGRESS, epic.getStatus()); // Проверка, что эпик остается со статусом IN_PROGRESS
    }

    /**
     * Проверка, что при попытке добавить задачу с пересекающимся временем выполнения
     * будет выброшено исключение TimeConflictException.
     */
    @Test
    public void testTimeOverlap() {
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofHours(1), baseTime); // Создание задачи с определенным временем
        manager.addTask(task1); // Добавление первой задачи
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofHours(2), baseTime.plusMinutes(30)); // Пересечение с первой задачей на 30 минут
        assertThrows(TimeConflictException.class, () -> manager.addTask(task2)); // Исключение, если есть пересечение
    }

    /**
     * Проверка, что deleteAllTasks() корректно очищает список всех задач.
     */
    @Test
    void testDeleteAllTasks() {
        Task task1 = new Task("T1", "D1");
        Task task2 = new Task("T2", "D2");
        manager.addTask(task1);
        manager.addTask(task2);
        Assertions.assertDoesNotThrow(() -> manager.deleteAllTasks(),
                "Удаление всех задач не должно вызывать исключений");
        assertTrue(manager.getAllTasks().isEmpty());
    }

    /**
     * Проверка правильности удаления задачи из середины истории просмотров.
     */
    @Test
    void testRemoveFromMiddle() {
        HistoryManager hm = new InMemoryHistoryManager();
        Task t1 = new Task("T1", "D1"); t1.setId(1);
        Task t2 = new Task("T2", "D2"); t2.setId(2);
        Task t3 = new Task("T3", "D3"); t3.setId(3);
        hm.add(t1);
        hm.add(t2);
        hm.add(t3);
        Assertions.assertDoesNotThrow(() -> hm.remove(2),
                "Удаление задачи из истории не должно вызывать исключений");
        List<Task> history = hm.getHistory();
        assertEquals(List.of(t1, t3), history);
    }

    /**
     * Проверка, что хэш-код задачи зависит только от её содержимого, а не от ссылки на объект.
     */
    @Test
    void testHashCodeConsistency() {
        Task task1 = new Task("T", "D"); // Создается первая задача
        task1.setId(1);
        Task task2 = new Task("T", "D"); // Создается вторая задача с тем же содержимым
        task2.setId(1);
        assertEquals(task1.hashCode(), task2.hashCode()); // Хэш-код должен быть одинаковым
    }
}
