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
}