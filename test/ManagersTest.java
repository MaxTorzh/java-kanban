import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {

    @Test
    void getDefaultHistoryReturnsInitializedHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory(); // Возвращает историю, которая инициализирована
        assertNotNull(historyManager); // Метод не возвращает null
        assertTrue(historyManager.getHistory().isEmpty()); // История пустая, только что созданная
    }

    @Test
    void getDefaultTaskManagerIsInitialized() {
        TaskManager tm = Managers.getDefault(); // Возвращает менеджер задач, который инициализирован
        assertNotNull(tm); // Метод не возвращает null
        assertTrue(tm.getAllTasks().isEmpty()); // Менеджер задач пустой, только что созданный
    }
}
