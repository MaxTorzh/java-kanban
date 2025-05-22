package taskmanager.core.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    LocalDateTime baseTime = TestData.BASE_TIME;
    @BeforeEach
    void createManager() {
        manager = new InMemoryTaskManager();
    }
    @Test
    void addAllTaskTypesAndFindsById() {
        InMemoryTaskManager tm = new InMemoryTaskManager();

        // Создаём задачи
        Task task = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Epic epic = new Epic("E1", "D1");
        Subtask subtask = new Subtask("S1", "D1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime.plusHours(1));

        // Добавляем задачи
        tm.addTask(task);
        tm.addEpic(epic);
        tm.addSubtask(subtask);

        // Проверяем задачу
        Task loadedTask = tm.getTaskById(task.getId());
        assertNotNull(loadedTask, "Задача не должна быть null");
        assertEquals(task, loadedTask);

        // Проверяем подзадачу
        Subtask loadedSubtask = tm.getSubtaskById(subtask.getId());
        assertNotNull(loadedSubtask, "Подзадача не должна быть null");
        assertEquals(subtask, loadedSubtask);

        // Проверяем эпик
        Epic loadedEpic = tm.getEpicById(epic.getId());
        assertNotNull(loadedEpic, "Эпик не должен быть null");
        assertEquals(epic, loadedEpic);
    }

    @Test
    void testNoIdConflicts() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        tm.addTask(task1);

        Task loaded = tm.getTaskById(task1.getId());
        assertNotNull(loaded, "Загруженная задача не должна быть null");

        assertEquals(task1.getTitle(), loaded.getTitle());
        assertEquals(task1.getDescription(), loaded.getDescription());
        assertEquals(task1.getStatus(), loaded.getStatus());
        assertEquals(task1.getId(), loaded.getId());
        assertEquals(task1.getDuration(), loaded.getDuration());
        assertEquals(task1.getStartTime(), loaded.getStartTime());
    }

    @Test
    void taskUnchangedAfterAdding() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask sub1 = new Subtask("S1", "D1", Status.NEW, 2,
                Duration.ofMinutes(30), baseTime);
        tm.addSubtask(sub1); // Добавление подзадачи
        Subtask sub2 = tm.getSubtaskById(sub1.getId()); // Возвращение ссылки на тот же объект, что и был добавлен

        assert sub2 != null;
        assertEquals(sub1.getTitle(), sub2.getTitle());
        assertEquals(sub1.getDescription(), sub2.getDescription());
        assertEquals(sub1.getStatus(), sub2.getStatus());
        assertEquals(sub1.getEpicId(), sub2.getEpicId());
        assertEquals(sub1.getId(), sub2.getId()); // Все поля сохранились без изменений
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        tm.addTask(task1); // id = 1, счетчик начинается с 1
        tm.addTask(task2); // id = 2
        assertEquals(task1.getId() + 1, task2.getId()); // id второй задачи должен быть на 1 больше первого
    }

    @Test
    void testGetPrioritizedTasks_SortsByStartTime() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task t1 = new Task("T1", "D1", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(2));
        Task t2 = new Task("T2", "D2", Status.NEW, Duration.ofMinutes(30), baseTime);
        Task t3 = new Task("T3", "D3", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(1));
        Task t4 = new Task("T4", "D4", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(3));
        tm.addTask(t2);
        tm.addTask(t3);
        tm.addTask(t1);
        tm.addTask(t4);
        List<Task> prioritized = tm.getPrioritizedTasks();
        assertEquals(Arrays.asList(t2, t3, t1, t4), prioritized);
    }
}