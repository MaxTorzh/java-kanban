package taskmanager.core.model;

import org.junit.jupiter.api.Test;
import taskmanager.core.managers.InMemoryTaskManager;
import taskmanager.core.util.Status;
import taskmanager.core.util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    LocalDateTime baseTime = TestData.BASE_TIME;
    @Test
    void canNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1); // Создается эпик с ID 1
        epic.addSubtask(1); // Попытка добавить ID эпика в список подзадач
        assertEquals(0, epic.getSubtaskIds().size()); // epic.getSubtaskIds().size должен быть 0
    }

    @Test
    void canNotAddDuplicateSubtask() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Desc");
        tm.addEpic(epic); // Создается эпик и добавляется в менеджер
        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, 1, Duration.ofMinutes(30), baseTime);
        tm.addSubtask(sub); // Создается подзадача и добавляется в менеджер
        epic.addSubtask(1);
        epic.addSubtask(1); // Попытка добавить дубликат ID подзадачи
        assertEquals(1, epic.getSubtaskIds().size()); // epic.getSubtaskIds().size должен быть 1
    }

    @Test
    void subtaskRemovedFromEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Desc");
        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime);
        tm.addEpic(epic); // Создается эпик и добавляется в менеджер
        tm.addSubtask(sub); // Создается подзадача и добавляется в менеджер
        tm.deleteSubtaskById(sub.getId()); // Удаление подзадачи
        assertEquals(0, epic.getSubtaskIds().size()); // epic.getSubtaskIds().size должен быть 0
    }

    @Test
    void testEpicTimeCalculation() {
        Epic epic = new Epic("Epic", "Desc");
        Subtask subtask1 = new Subtask("Sub1", "Desc", Status.DONE, epic.getId(),
                Duration.ofMinutes(30), baseTime); // Начало в 18:00 → конец 18:30
        Subtask subtask2 = new Subtask("Sub2", "Desc", Status.NEW, epic.getId(),
                Duration.ofMinutes(60), baseTime.plusHours(1)); // Начало в 19:00 → конец в 20:00

        epic.updateTimeParameters(List.of(subtask1, subtask2));

        assertEquals(subtask1.getStartTime(), epic.getStartTime()); // Ожидает в: 18:00
        assertEquals(subtask2.getEndTime(), epic.getEndTime());     // Ожидает в: 20:00
    }
}