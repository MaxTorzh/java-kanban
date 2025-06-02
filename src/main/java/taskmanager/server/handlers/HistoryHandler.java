package taskmanager.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.core.managers.TaskManager;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для получения истории просмотров задач.
 * Поддерживает только GET /history.
 */
public class HistoryHandler extends BaseHttpHandler {
    private final Gson gson;

    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager);
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            if (!exchange.getRequestMethod().equals("GET")) {
                sendText(exchange, "{\"error\":\"Метод не поддерживается\"}", 405);
                return;
            }
            sendText(exchange, gson.toJson(taskManager.getHistory()), 200);
        } catch (Exception e) {
            sendServerError(exchange, e);
        }
    }
}
