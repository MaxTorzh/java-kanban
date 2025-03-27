import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void tasksAreNotEqualIfDifferentIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        assertNotEquals(task1, task2);
    }

    @Test
    void tasksAreEqualIfSameIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2);
    }

    @Test
    void tasksInheritorsAreEqualIfHasSameIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Subtask task2 = new Subtask("Task2", "Desc2", Status.NEW, 1);
        Epic task3 = new Epic("Task3", "Desc3");
        task1.setId(1);
        task2.setId(1);
        task3.setId(1);
        assertEquals(task1, task2);
        assertEquals(task1, task3);
    }
}