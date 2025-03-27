import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EpicTest {
    @Test
    void canNotAddEpicToItselfAsSubtask() {
        Epic epic = new Epic("Epic", "Epic description");
        epic.setId(1);
        Subtask subtask = new Subtask("Subtask", "Subtask description", Status.NEW, 1);
        subtask.setId(2);
        epic.addSubtask(epic.getId());
        assertEquals(new ArrayList<>(), epic.getSubtaskIds());
    }
}