package taskmanager.server.handlers;

import com.google.gson.Gson;
import taskmanager.core.model.Task;
import taskmanager.server.utils.HttpTaskServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки эндпоинта /tasks, реализованного в {@link TaskHandler}.
 * Проверяет работу GET, POST, DELETE запросов для задач.
 */
class TaskHandlerTest extends BaseHttpHandlerTest {

    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Вспомогательный метод для отправки POST-запроса на создание задачи.
     *
     * @param task задача для отправки
     * @return HTTP-ответ от сервера
     * @throws Exception если произошла ошибка при выполнении запроса
     */
    private HttpResponse<String> createTask(Task task) throws Exception {
        String json = gson.toJson(task);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Проверка, что GET /tasks возвращает пустой список и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetAllTasks_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    /**
     * Проверка, что POST /tasks корректно добавляет новую задачу и возвращает статус 201.
     */
    @org.junit.jupiter.api.Test
    void testAddTask_valid_returns200AndSavedInManager() throws Exception {
        Task task = new Task("T", "D");
        task.setStatus(taskmanager.core.util.Status.NEW);
        task.setDuration(Duration.ofMinutes(30));
        task.setStartTime(LocalDateTime.now());

        HttpResponse<String> response = createTask(task);

        assertEquals(201, response.statusCode());

        assertFalse(taskManager.getAllTasks().isEmpty());
        Task savedTask = taskManager.getAllTasks().get(0);
        assertNotNull(savedTask);
        assertEquals("T", savedTask.getTitle());
    }

    /**
     * Проверка, что POST /tasks с невалидным JSON возвращает статус 400.
     */
    @org.junit.jupiter.api.Test
    void testAddTask_invalidJson_returns400() throws Exception {
        String invalidJson = "{invalid}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Неверный формат JSON"));
    }

    /**
     * Проверка, что GET /tasks/{id} возвращает существующую задачу и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetTaskById_validId_returns200() throws Exception {
        Task task = new Task("T", "D");
        task.setId(1);
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("T"));
    }

    /**
     * Проверка, что GET /tasks/{id} для несуществующей задачи возвращает статус 404.
     */
    @org.junit.jupiter.api.Test
    void testGetTaskById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Задача не найдена"));
    }

    /**
     * Проверка, что DELETE /tasks/{id} удаляет задачу и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteTaskById_validId_taskRemoved() throws Exception {
        Task task = new Task("T", "D");
        task.setId(1);
        taskManager.addTask(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/1"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getTaskById(1));
    }

    /**
     * Проверка, что DELETE /tasks удаляет все задачи и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteAllTasks_tasksCleared() throws Exception {
        Task task1 = new Task("T1", "D1");
        Task task2 = new Task("T2", "D2");
        taskManager.addTask(task1);
        taskManager.addTask(task2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
}
