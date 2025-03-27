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
}