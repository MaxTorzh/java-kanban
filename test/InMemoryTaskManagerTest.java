import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    @Test
    void addAllTaskTypesAndFindsById() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task = new Task("Task1", "Desc", Status.NEW);
        Subtask subtask = new Subtask("Sub1", "Desc", Status.NEW, 1);
        Epic epic = new Epic("Epic1", "Desc");
        tm.addTask(task);
        tm.addSubtask(subtask);
        tm.addEpic(epic);

        assertEquals(task, tm.getTaskById(task.getId()));
        assertEquals(subtask, tm.getSubtaskById(subtask.getId()));
        assertEquals(epic, tm.getEpicById(epic.getId()));
    }

    @Test
    void testNoIdConflicts() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        tm.addTask(task1);

        assertEquals(task1, tm.getTaskById(task1.getId()));
    }

    @Test
    void taskUnchangedAfterAdding() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask sub1 = new Subtask("Sub1", "Desc1", Status.NEW, 1);
        sub1.setId(777);
        tm.addSubtask(sub1);
        Subtask sub2 = tm.getSubtaskById(sub1.getId());

        assertEquals(sub1.getTitle(), sub2.getTitle());
        assertEquals(sub1.getDescription(), sub2.getDescription());
        assertEquals(sub1.getStatus(), sub2.getStatus());
        assertEquals(sub1.getEpicId(), sub2.getEpicId());
        assertEquals(sub1.getId(), sub2.getId());
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        tm.addTask(task1);
        tm.addTask(task2);
        assertEquals(task1.getId() + 1, task2.getId());
    }
}