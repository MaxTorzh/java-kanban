package taskmanager.server.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.core.managers.TaskManager;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для получения отсортированного списка задач по приоритету.
 * Поддерживает только GET /prioritized.
 */
public class PrioritizedHandler extends BaseHttpHandler {
    private final Gson gson;

    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
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
            sendText(exchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
        } catch (Exception e) {
            sendServerError(exchange, e);
        }
    }
}
