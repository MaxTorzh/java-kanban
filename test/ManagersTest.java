import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {
    @BeforeEach
    void resetManagers() { // Восстанавливаем исходное состояние HistoryManager
        Managers.setHistoryManager(new InMemoryHistoryManager());
    } // Присутствует изменение статического поля в Managers (setHistory в InMemoryHistoryManagerTest)

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
