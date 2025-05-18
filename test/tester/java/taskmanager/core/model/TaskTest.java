package taskmanager.core.model;

import org.junit.jupiter.api.Test;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    LocalDateTime baseTime = TestData.BASE_TIME;
    @Test
    void tasksAreNotEqualIfDifferentIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        task1.setId(1); // id = 1
        task2.setId(2); // id = 2
        assertNotEquals(task1, task2); // Не должны быть равны
    }

    @Test
    void tasksAreEqualIfSameIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("Task2", "Desc2", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        task1.setId(1); // id = 1
        task2.setId(1); // id = 1
        assertEquals(task1, task2); // Должны быть равны
    }

    @Test
    void tasksInheritorsAreEqualIfHasSameIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Subtask task2 = new Subtask("Task2", "Desc2", Status.NEW, 1,
                Duration.ofMinutes(30), baseTime);
        Epic task3 = new Epic("Task3", "Desc3");
        task1.setId(1); // id = 1
        task2.setId(1); // id = 1
        task3.setId(1); // id = 1
        assertEquals(task1, task2); // Должны быть равны
        assertEquals(task1, task3); // Должны быть равны
    }

    @Test
    void testTaskDurationAndStartTime() {
        Task task = new Task("Test", "Desc", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        assertNotNull(task.getEndTime());
    }
}