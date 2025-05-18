package taskmanager.core.managers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;

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
        Task task = new Task("Task1", "Desc", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Subtask subtask = new Subtask("Sub1", "Desc", Status.NEW, 1,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        Epic epic = new Epic("Epic1", "Desc");
        tm.addTask(task);
        tm.addSubtask(subtask);
        tm.addEpic(epic);

        assertEquals(task, tm.getTaskById(task.getId())); // По id должна вернуться та же задача, что и была добавлена
        assertEquals(subtask, tm.getSubtaskById(subtask.getId())); // Здесь тоже
        assertEquals(epic, tm.getEpicById(epic.getId())); // И здесь
    }

    @Test
    void testNoIdConflicts() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        tm.addTask(task1);

        assertEquals(task1, tm.getTaskById(task1.getId())); // По id должна вернуться та же задача, что и была добавлена
    }

    @Test
    void taskUnchangedAfterAdding() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, 2,
                Duration.ofMinutes(30), baseTime);
        tm.addSubtask(sub1); // Добавление подзадачи
        Subtask sub2 = tm.getSubtaskById(sub1.getId()); // Возвращение ссылки на тот же объект, что и был добавлен

        assertEquals(sub1.getTitle(), sub2.getTitle());
        assertEquals(sub1.getDescription(), sub2.getDescription());
        assertEquals(sub1.getStatus(), sub2.getStatus());
        assertEquals(sub1.getEpicId(), sub2.getEpicId());
        assertEquals(sub1.getId(), sub2.getId()); // Все поля сохранились без изменений
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        tm.addTask(task1); // id = 1, счетчик начинается с 1
        tm.addTask(task2); // id = 2
        assertEquals(task1.getId() + 1, task2.getId()); // id второй задачи должен быть на 1 больше первого
    }
}