import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    @Test
    void canNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);
        epic.addSubtask(1); // Попытка добавить ID эпика в список подзадач
        assertEquals(0, epic.getSubtaskIds().size());
    }

    @Test
    void canNotAddDuplicateSubtask() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Desc");
        tm.addEpic(epic);
        Subtask sub = new Subtask("Sub", "Desc", Status.NEW, 1);
        tm.addSubtask(sub);
        epic.addSubtask(1);
        epic.addSubtask(1); // Попытка добавить дубликат ID подзадачи
        assertEquals(1, epic.getSubtaskIds().size());
    }
}