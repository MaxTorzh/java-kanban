package taskmanager.server.handlers;

import com.google.gson.Gson;
import taskmanager.core.model.Epic;
import taskmanager.core.model.Subtask;
import taskmanager.core.util.Status;
import taskmanager.server.utils.HttpTaskServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки эндпоинта /subtasks, реализованного в {@link SubtaskHandler}.
 * Проверяет работу GET, POST, DELETE запросов для подзадач.
 */
public class SubtaskHandlerTest extends BaseHttpHandlerTest {
    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Вспомогательный метод для отправки POST-запроса на создание подзадачи.
     *
     * @param subtask подзадача для отправки
     * @return HTTP-ответ от сервера
     * @throws Exception если произошла ошибка при выполнении запроса
     */
    private HttpResponse<String> createSubtask(Subtask subtask) throws Exception {
        String json = gson.toJson(subtask);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Проверяет, что GET /subtasks возвращает пустой список и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetAllSubtasks_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request,HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    /**
     * Проверяет, что POST /subtasks корректно добавляет новую подзадачу и возвращает статус 201.
     */
    @org.junit.jupiter.api.Test
    void testAddSubtask_valid_returns201AndSavedInManager() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("S", "D", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());

        HttpResponse<String> response = createSubtask(subtask);

        assertEquals(201, response.statusCode());
        assertFalse(taskManager.getAllSubtasks().isEmpty());

        Subtask savedSubtask = taskManager.getAllSubtasks().get(0);

        assertNotNull(savedSubtask);
        assertEquals("S", savedSubtask.getTitle());
    }

    /**
     * Проверка, что POST /subtasks с невалидным JSON возвращает статус 400.
     */
    @org.junit.jupiter.api.Test
    void testAddSubtask_invalidJson_returns400() throws Exception {
        String invalidJson = "{invalid}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Неверный формат JSON"));
    }

    /**
     * Проверка, что GET /subtasks/{id} возвращает существующую подзадачу и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetSubtaskById_validId_returns200() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("S", "D", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("S"));
    }

    /**
     * Проверка, что GET /subtasks/{id} для несуществующей подзадачи возвращает статус 404.
     */
    @org.junit.jupiter.api.Test
    void testGetSubtaskById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Подзадача не найдена"));
    }

    /**
     * Проверка, что DELETE /subtasks/{id} удаляет подзадачу и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteSubtaskById_validId_taskRemoved() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        Subtask subtask = new Subtask("S", "D", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        taskManager.addSubtask(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getSubtaskById(1));
    }

    /**
     * Проверка, что DELETE /subtasks удаляет все подзадачи и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteAllSubtasks_tasksCleared() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        Subtask s1 = new Subtask("S", "D", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now());
        Subtask s2 = new Subtask("S", "D", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), LocalDateTime.now().plusHours(1));
        taskManager.addSubtask(s1);
        taskManager.addSubtask(s2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }
}
