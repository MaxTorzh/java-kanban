import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {
    @Test
    void getDefaultHistoryReturnsInitializedHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();
        assertNotNull(historyManager);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getDefaultTaskManagerIsInitialized() {
        TaskManager tm = Managers.getDefault();
        assertNotNull(tm);
        assertTrue(tm.getAllTasks().isEmpty());
    }
}
