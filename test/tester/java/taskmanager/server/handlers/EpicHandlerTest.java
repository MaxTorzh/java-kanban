package taskmanager.server.handlers;

import com.google.gson.Gson;
import taskmanager.core.model.Epic;
import taskmanager.server.utils.HttpTaskServer;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для проверки эндпоинта /epics, реализованного в {@link EpicHandler}.
 * Проверяет работу GET, POST, DELETE запросов для эпиков.
 */
class EpicHandlerTest extends BaseHttpHandlerTest {

    private final Gson gson = HttpTaskServer.getGson();
    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Вспомогательный метод для отправки POST-запроса на создание эпика.
     *
     * @param epic эпик для отправки
     * @return HTTP-ответ от сервера
     * @throws Exception если произошла ошибка при выполнении запроса
     */
    private HttpResponse<String> createEpic(Epic epic) throws Exception {
        String json = gson.toJson(epic);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .header("Content-Type", "application/json")
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Проверяет, что GET /epics возвращает пустой список и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetAllEpics_emptyList_returns200() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals("[]", response.body());
    }

    /**
     * Проверяет, что POST /epics корректно добавляет новый эпик и возвращает статус 201.
     */
    @org.junit.jupiter.api.Test
    void testAddEpic_valid_returns201AndSavedInManager() throws Exception {
        Epic epic = new Epic("E", "D");

        HttpResponse<String> response = createEpic(epic);

        assertEquals(201, response.statusCode());

        assertFalse(taskManager.getAllEpics().isEmpty());
        Epic savedEpic = taskManager.getAllEpics().get(0);
        assertNotNull(savedEpic);
        assertEquals("E", savedEpic.getTitle());
    }

    /**
     * Проверка, что POST /epics с невалидным JSON возвращает статус 400.
     */
    @org.junit.jupiter.api.Test
    void testAddEpic_invalidJson_returns400() throws Exception {
        String invalidJson = "{invalid}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Неверный формат JSON"));
    }

    /**
     * Проверка, что GET /epics/{id} возвращает существующий эпик и статус 200.
     */
    @org.junit.jupiter.api.Test
    void testGetEpicById_validId_returns200() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("E"));
    }

    /**
     * Проверка, что GET /epics/{id} для несуществующего эпика возвращает статус 404.
     */
    @org.junit.jupiter.api.Test
    void testGetEpicById_notFound_returns404() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        assertTrue(response.body().contains("Эпик не найден"));
    }

    /**
     * Проверка, что DELETE /epics/{id} удаляет эпик и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteEpicById_validId_taskRemoved() throws Exception {
        Epic epic = new Epic("E", "D");
        taskManager.addEpic(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(taskManager.getEpicById(1));
    }

    /**
     * Проверка, что DELETE /epics удаляет все эпики и возвращает статус 200.
     */
    @org.junit.jupiter.api.Test
    void testDeleteAllEpics_tasksCleared() throws Exception {
        Epic e1 = new Epic("E1", "D1");
        Epic e2 = new Epic("E2", "D2");
        taskManager.addEpic(e1);
        taskManager.addEpic(e2);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/epics"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty());
    }
}
