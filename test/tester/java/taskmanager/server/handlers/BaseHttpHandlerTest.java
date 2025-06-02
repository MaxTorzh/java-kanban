package taskmanager.server.handlers;

import org.junit.jupiter.api.*;
import java.net.http.*;
import java.io.IOException;
import com.google.gson.Gson;
import taskmanager.core.managers.InMemoryTaskManager;
import taskmanager.core.managers.TaskManager;
import taskmanager.server.utils.HttpTaskServer;

public abstract class BaseHttpHandlerTest {
    protected static final int PORT = 8080;
    protected TaskManager taskManager;
    protected HttpTaskServer server;
    protected HttpClient client;
    protected Gson gson;

    @BeforeEach
    void setUp() throws IOException {
        taskManager = new InMemoryTaskManager(); // Чистый менеджер перед каждым тестом
        server = new HttpTaskServer(taskManager);
        server.start();
        client = HttpClient.newHttpClient();
        gson = new Gson();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }
}
