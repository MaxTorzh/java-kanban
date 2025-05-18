package taskmanager.core.managers;

import org.junit.jupiter.api.Test;
import taskmanager.core.exceptions.TimeConflictException;
import taskmanager.core.model.*;
import taskmanager.core.util.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    LocalDateTime baseTime = TestData.BASE_TIME;

    @Test
    public void testEpicStatusCalculation() {
        Epic epic = new Epic("Epic", "Description");
        manager.addEpic(epic);

        // a. Все подзадачи NEW
        Subtask subtask1 = new Subtask("Sub1", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime);
        manager.addSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus());

        // b. Все подзадачи DONE
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus());

        // c. NEW и DONE
        Subtask subtask2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(60), baseTime.plusHours(1));
        manager.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        // d. Подзадачи IN_PROGRESS
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void testTimeOverlap() {
        Task task1 = new Task("Task1", "Desc", Status.NEW,
                Duration.ofHours(1), baseTime);
        Task task2 = new Task("Task2", "Desc", Status.NEW,
                Duration.ofHours(2), baseTime.plusMinutes(30));

        manager.addTask(task1);
        assertThrows(TimeConflictException.class, () -> manager.addTask(task2));
    }
}
