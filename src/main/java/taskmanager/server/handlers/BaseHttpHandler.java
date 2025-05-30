package taskmanager.server.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.core.managers.TaskManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Абстрактный базовый класс для обработчиков HTTP-запросов.
 * Предоставляет общие методы для отправки ответов и доступ к {@link TaskManager}.
 * <p>
 * Наследники должны реализовать свою логику обработки запросов в методе {@link HttpHandler#handle}.
 */
public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;

    /**
     * Создается обработчик с указанным менеджером задач.
     * @param taskManager Менеджер задач для операций с данными.
     */
    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    /**
     * Отправляет текстовый ответ в формате JSON.
     * @param exchange Объект HTTP-обмена.
     * @param text Текст ответа.
     * @param statusCode HTTP-статус код.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendText(HttpExchange exchange, String text, int statusCode) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    /**
     * Отправляет ответ 400 (Bad Request) с сообщением об ошибке.
     * @param exchange Объект HTTP-обмена.
     * @param message Сообщение об ошибке.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendBadRequest(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\":\"" + escapeJson(message) + "\"}", 400);
    }

    /**
     * Отправляет ответ 404 (Not Found) с сообщением об ошибке.
     * @param exchange Объект HTTP-обмена.
     * @param message Сообщение об ошибке.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendNotFound(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"error\":\"" + escapeJson(message) + "\"}", 404);
    }

    /**
     * Отправляет ответ 406 (Not Acceptable) при пересечении задач по времени.
     * @param exchange Объект HTTP-обмена.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendHasInteractions(HttpExchange exchange) throws IOException {
        sendText(exchange, "{\"error\":\"Задачи пересекаются по времени\"}", 406);
    }

    /**
     * Отправляет ответ 500 (Internal Server Error) с сообщением об исключении.
     * @param exchange Объект HTTP-обмена.
     * @param e Исключение, вызвавшее ошибку.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendServerError(HttpExchange exchange, Exception e) throws IOException {
        sendText(exchange, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}", 500);
    }

    /**
     * Отправляет успешный ответ 200 (OK) с сообщением.
     * @param exchange Объект HTTP-обмена.
     * @param message Информационное сообщение.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendSuccess(HttpExchange exchange, String message) throws IOException {
        sendText(exchange, "{\"message\":\"" + escapeJson(message) + "\"}", 200);
    }

    /**
     * Отправляет ответ 201 (Created) без тела.
     * @param exchange Объект HTTP-обмена.
     * @throws IOException Если произошла ошибка ввода-вывода.
     */
    protected void sendCreated(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(201, -1);
        exchange.close();
    }

    /**
     * Извлекает ID задачи из пути запроса.
     *
     * @param path путь запроса (например "/tasks/1")
     * @return числовой ID задачи
     */
    protected int extractIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[2]);
    }

    /**
     * Экранирует спецсимволы в строке для безопасного включения в JSON.
     * @param input Исходная строка.
     * @return Экранированная строка.
     */
    private String escapeJson(String input) {
        return input.replace("\"", "\\\"")
                .replace("\n", "\\n");
    }
}
