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
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        task1.setId(1); // id = 1
        task2.setId(2); // id = 2
        assertNotEquals(task1, task2); // Не должны быть равны
    }

    @Test
    void tasksAreEqualIfSameIds() {
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        task1.setId(1); // id = 1
        task2.setId(1); // id = 1
        assertEquals(task1, task2); // Должны быть равны
    }

    @Test
    void tasksInheritorsAreEqualIfHasSameIds() {
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Subtask task2 = new Subtask("T2", "D2", Status.NEW, 1,
                Duration.ofMinutes(30), baseTime);
        Epic task3 = new Epic("T3", "D3");
        task1.setId(1); // id = 1
        task2.setId(1); // id = 1
        task3.setId(1); // id = 1
        assertEquals(task1, task2); // Должны быть равны
        assertEquals(task1, task3); // Должны быть равны
    }

    @Test
    void testTaskDurationAndStartTime() {
        Task task = new Task("T", "D", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        assertNotNull(task.getEndTime());
    }
}