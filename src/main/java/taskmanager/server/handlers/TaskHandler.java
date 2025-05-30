package taskmanager.server.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.core.managers.TaskManager;
import taskmanager.core.model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {
    private final Gson gson;

    public TaskHandler(TaskManager taskManager, Gson gson) {
        super(taskManager);
        this.gson = gson;
    }

    /**
     * Обрабатывает входящий HTTP-запрос.
     * Роутит запросы в зависимости от метода (GET/POST/DELETE).
     *
     * @param exchange объект HTTP-обмена, содержащий запрос и ответ
     * @throws IOException если произошла ошибка ввода-вывода
     */
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
     * Обрабатывает GET-запросы:
     * - GET /tasks - возвращает список всех задач
     * - GET /tasks/{id} - возвращает задачу по ID
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/task")) {
            sendText(exchange, gson.toJson(taskManager.getAllTasks()), 200);
        } else {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange, "Задача не найдена.");
                } else {
                    sendText(exchange, gson.toJson(task), 200);
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID задачи должен быть числом.");
            }
        }
    }

    /**
     * Обрабатывает POST-запросы:
     * - Создает новую задачу (если ID=0 или не существует)
     * - Обновляет существующую задачу
     * Проверяет валидность данных и пересечение по времени.
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handlePost(HttpExchange exchange) throws IOException {
        String json = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        try {
            Task task = gson.fromJson(json, Task.class);
            if (task.getTitle() == null || task.getDescription() == null) {
                sendBadRequest(exchange, "Поля title и description должны быть инициализированы.");
                return;
            }
            if (task.isOverlapping(task)) return;
            if (task.getId() == 0 || taskManager.getTaskById(task.getId()) == null) {
                taskManager.addTask(task);
                sendCreated(exchange);
            } else {
                taskManager.updateTask(task);
                sendSuccess(exchange, "Задача обновлена.");
            }
        } catch (JsonSyntaxException e) {
            sendBadRequest(exchange, "Неверный формат JSON");
        }
    }

    /**
     * Обрабатывает DELETE-запросы:
     * - DELETE /tasks/{id} - удаляет задачу по ID
     * - DELETE /tasks - удаляет все задачи
     *
     * @param exchange объект HTTP-обмена
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void handleDelete(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/task")) {
            taskManager.deleteAllTasks();
            sendSuccess(exchange, "Все задачи удалены.");
        } else {
            try {
                int id = extractIdFromPath(path);
                Task task = taskManager.getTaskById(id);
                if (task == null) {
                    sendNotFound(exchange, "Задача не найдена");
                } else {
                    taskManager.deleteTaskById(id);
                    sendSuccess(exchange, "Задача удалена");
                }
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, "ID задачи должен быть числом");
            }
        }
    }

    /**
     * Извлекает ID задачи из пути запроса.
     *
     * @param path путь запроса (например "/tasks/42")
     * @return числовой ID задачи
     * @throws NumberFormatException если ID не является числом
     */
    private int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }
}
