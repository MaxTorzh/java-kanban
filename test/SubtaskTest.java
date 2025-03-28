import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SubtaskTest {
    @Test
    void subtaskCanNotBeSelfEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, 1);
        subtask.setId(1);
        assertThrows(IllegalArgumentException.class, () -> tm.addSubtask(subtask));
    }
}