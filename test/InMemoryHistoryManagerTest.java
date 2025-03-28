import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    @Test
    void historyKeepsPreviousTaskVersions() {
        HistoryManager hm = new InMemoryHistoryManager();
        Managers.setHistoryManager(hm);
        InMemoryTaskManager tm = new InMemoryTaskManager();

        Task initialTask = new Task("Task 1", "Description 1", Status.NEW);
        tm.addTask(initialTask);
        tm.getTaskById(initialTask.getId());
        List<Task> historyList = hm.getHistory();
        assertEquals(1, historyList.size());
        assertEquals(Status.NEW, historyList.get(0).getStatus());

        Task updatedTask = new Task("Task 1", "Description 1", Status.IN_PROGRESS);
        updatedTask.setId(initialTask.getId());

        tm.updateTask(updatedTask);
        tm.getTaskById(updatedTask.getId());

        historyList = hm.getHistory();
        assertEquals(2, historyList.size());
        assertEquals(Status.NEW, historyList.get(0).getStatus());
        assertEquals(Status.IN_PROGRESS, historyList.get(1).getStatus());
    }
}
