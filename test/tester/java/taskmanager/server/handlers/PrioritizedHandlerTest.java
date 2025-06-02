package taskmanager.server.handlers;

import taskmanager.core.model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки эндпоинта /prioritized, реализованного в {@link PrioritizedHandler}.
 * Проверяет работу GET запроса для получения задач, отсортированных по времени начала.
 */
class PrioritizedHandlerTest extends BaseHttpHandlerTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @org.junit.jupiter.api.Test
    void testGetPrioritizedTasks_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @org.junit.jupiter.api.Test
    void testGetPrioritizedTasks_withTasks_returnsSortedByStartTime() throws Exception {
        Task t1 = new Task("T1", "Desc");
        t1.setStartTime(LocalDateTime.now().plusHours(2));
        t1.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(t1);

        Task t2 = new Task("T2", "Desc");
        t2.setStartTime(LocalDateTime.now());
        t2.setDuration(Duration.ofMinutes(30));
        taskManager.addTask(t2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("T2") && response.body().contains("T1"));
    }
}
