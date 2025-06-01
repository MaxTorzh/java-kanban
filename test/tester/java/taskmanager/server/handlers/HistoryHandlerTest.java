package taskmanager.server.handlers;

import taskmanager.core.model.Task;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки эндпоинта /history, реализованного в {@link HistoryHandler}.
 * Проверяет работу GET запроса для получения истории просмотров задач.
 */
class HistoryHandlerTest extends BaseHttpHandlerTest {

    private final HttpClient client = HttpClient.newHttpClient();

    @org.junit.jupiter.api.Test
    void testGetHistory_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    @org.junit.jupiter.api.Test
    void testGetHistory_withViewedTasks_returnsHistory() throws Exception {
        Task task = new Task("History Task", "Desc");
        taskManager.addTask(task);
        taskManager.getTaskById(task.getId()); // добавляем в историю

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/history"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("History Task"));
    }
}
