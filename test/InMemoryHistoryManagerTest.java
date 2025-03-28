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
        tm.getTaskById(initialTask.getId()); // Получаем задачу и добавляем в историю
        List<Task> historyList = hm.getHistory(); // В истории будет 1 задача
        assertEquals(1, historyList.size()); // size должен быть 1
        assertEquals(Status.NEW, historyList.get(0).getStatus()); // Status должен быть NEW

        Task updatedTask = new Task("Task 1", "Description 1", Status.IN_PROGRESS);
        updatedTask.setId(initialTask.getId()); // Устанавливаем новый id
        tm.updateTask(updatedTask); // Обновляем задачу
        tm.getTaskById(updatedTask.getId()); // Получаем задачу и добавляем в историю
        historyList = hm.getHistory(); // В истории будет 2 задачи
        assertEquals(2, historyList.size()); // size должен быть 2
        assertEquals(Status.NEW, historyList.get(0).getStatus()); // Status первой задачи должен быть NEW
        assertEquals(Status.IN_PROGRESS, historyList.get(1).getStatus()); // Status второй задачи должен быть IN_PROGRESS
    }
}
