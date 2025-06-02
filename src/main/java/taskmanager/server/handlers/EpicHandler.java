package taskmanager.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.core.managers.TaskManager;
import taskmanager.core.model.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для работы с эпиками (Epic).
 * Поддерживает операции GET, POST, DELETE по эндпоинту /epics.
 */
public class EpicHandler extends BaseHttpHandler {
    private final Gson gson;

    public EpicHandler(TaskManager taskManager, Gson gson) {
        super(taskManager);
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            switch (exchange.getRequestMethod()) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> sendText(exchange, "{\"error\":\"Метод не поддерживается\"}", 405);
            }
        } catch (Exception e) {
            sendServerError(exchange, e);
        }
    }

    /**
     * Обработка GET-запросов:
     * - GET /epics - возвращает список всех эпиков
     * - GET /epics/{id} - возвращает эпик по ID
     * - GET /epics/{id}/subtasks - возвращает подзадачи эпика
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/epics")) {
            sendText(exchange, gson.toJson(taskManager.getAllEpics()), 200);
        } else if (path.matches("/epics/\\d+")) {
            handleGetEpicById(exchange, path);
        } else if (path.matches("/epics/\\d+/subtasks")) {
            handleGetEpicSubtasks(exchange, path);
        } else {
            sendNotFound(exchange, "Неверный путь запроса");
        }
    }

    private void handleGetEpicById(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            Epic epic = taskManager.getEpicById(id);
            if (epic == null) {
                sendNotFound(exchange, "Эпик не найден.");
            } else {
                sendText(exchange, gson.toJson(epic), 200);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "ID эпика должен быть числом.");
        }
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, String path) throws IOException {
        try {
            int id = extractIdFromPath(path);
            sendText(exchange, gson.toJson(taskManager.getEpicById(id)), 200);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, "ID эпика должен быть числом");
        }
    }

    /**
     * Обработка POST-запросов:
     * - Создает новый эпик (если ID=0 или не существует)
     * - Обновляет существующий эпик
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Epic epic = gson.fromJson(json, Epic.class);
            if (epic.getTitle() == null || epic.getDescription() == null) {
                sendBadRequest(exchange, "Поля title и description должны быть инициализированы");
                return;
            }
            if (epic.getId() == 0 || taskManager.getEpicById(epic.getId()) == null) {
                taskManager.addEpic(epic);
                sendCreated(exchange);
            } else {
                taskManager.updateEpic(epic);
                sendSuccess(exchange, "Эпик обновлен");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    /**
     * Обработка DELETE-запросов:
     * - DELETE /epics/{id} - удаляет эпик по ID
     * - DELETE /epics - удаляет все эпики
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/epics")) {
            taskManager.deleteAllEpics();
            sendSuccess(exchange, "Все эпики удалены");
        } else {
            try {
                int id = extractIdFromPath(path);
                Epic epic = taskManager.getEpicById(id);
                if (epic == null) {
                    sendNotFound(exchange, "Эпик не найден");
                } else {
                    taskManager.deleteEpicById(id);
                    sendSuccess(exchange, "Эпик удален");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID эпика должен быть числом");
            }
        }
    }
}
