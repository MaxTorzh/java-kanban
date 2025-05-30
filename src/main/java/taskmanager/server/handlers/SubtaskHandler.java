package taskmanager.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.core.managers.TaskManager;
import taskmanager.core.model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Обработчик HTTP-запросов для работы с подзадачами (Subtask).
 * Поддерживает операции GET, POST, DELETE по эндпоинту /subtasks.
 */
public class SubtaskHandler extends BaseHttpHandler {
    private final Gson gson;

    public SubtaskHandler(TaskManager taskManager, Gson gson) {
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
     * - GET /subtasks - возвращает список всех подзадач
     * - GET /subtasks/{id} - возвращает подзадачу по ID
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/subtasks")) {
            sendText(exchange, gson.toJson(taskManager.getAllSubtasks()), 200);
        } else {
            try {
                int id = extractIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange, "Подзадача не найдена.");
                } else {
                    sendText(exchange, gson.toJson(subtask), 200);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID подзадачи должен быть числом");
            }
        }
    }

    /**
     * Обработка POST-запросов:
     * - Создание новой подзадачи (если ID=0 или не существует)
     * - Обновление существующей подзадачи
     * Проверка валидности данных и пересечения по времени.
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Subtask subtask = gson.fromJson(json, Subtask.class);
            if (subtask.getTitle() == null || subtask.getDescription() == null) {
                sendBadRequest(exchange, "Поля title и description должны быть инициализированы");
                return;
            }
            if (subtask.getEpicId() == 0 || taskManager.getEpicById(subtask.getEpicId()) == null) {
                sendBadRequest(exchange, "Не указан или не существует эпик для подзадачи");
                return;
            }
            if (taskManager.hasTimeOverlap(subtask)) {
                sendHasInteractions(exchange);
                return;
            }
            if (subtask.getId() == 0 || taskManager.getSubtaskById(subtask.getId()) == null) {
                taskManager.addSubtask(subtask);
                sendCreated(exchange);
            } else {
                taskManager.updateSubtask(subtask);
                sendSuccess(exchange, "Подзадача обновлена.");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    /**
     * Обрабатывает DELETE-запросы:
     * - DELETE /subtasks/{id} - удаляет подзадачу по ID
     * - DELETE /subtasks - удаляет все подзадачи
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/subtask")) {
            taskManager.deleteAllSubtasks();
            sendSuccess(exchange, "Все подзадачи удалены.");
        } else {
            try {
                int id = extractIdFromPath(path);
                Subtask subtask = taskManager.getSubtaskById(id);
                if (subtask == null) {
                    sendNotFound(exchange, "Подзадача не найдена.");
                } else {
                    taskManager.deleteSubtaskById(id);
                    sendSuccess(exchange, "Подзадача удалена.");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID подзадачи должен быть числом.");
            }
        }
    }
}
