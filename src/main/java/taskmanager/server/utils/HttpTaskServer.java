package taskmanager.server.utils;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import org.junit.platform.commons.logging.LoggerFactory;
import taskmanager.core.managers.TaskManager;
import taskmanager.core.util.Managers;
import taskmanager.server.handlers.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * HTTP-сервер для управления задачами (Task, Subtask, Epic).
 * Предоставляет REST API для работы с менеджером задач через HTTP-запросы.
 * Основные эндпоинты:
 * /tasks - работа с задачами
 * /subtasks - работа с подзадачами
 * /epics - работа с эпиками
 * /history - история просмотров задач
 * /prioritized - список задач по приоритету
 * Сервер автоматически добавляет shutdown hook для корректного завершения работы.
 */
public class HttpTaskServer {
    private static final Logger log = Logger.getLogger(HttpTaskServer.class.getName());
    private final int port;
    private final TaskManager taskManager;
    private Gson gson;
    private HttpServer server;

    /**
     * Создание сервера на стандартном порту 8080.
     * @param taskManager менеджер задач для обработки запросов
     * @throws IOException если произошла ошибка при создании сервера
     */
    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this(taskManager, 8080);
    }

    /**
     * Создает сервер с указанным портом.
     * @param taskManager менеджер задач для обработки запросов
     * @param port порт для прослушивания
     * @throws IOException если произошла ошибка при создании сервера
     */
    public HttpTaskServer(TaskManager taskManager, int port) throws IOException {
        this.taskManager = taskManager;
        this.port = port;
        this.gson = new Gson();
        Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    }

    /**
     * Запуск HTTP-сервера.
     * Создание HTTP-сервера, регистрация обработчиков и начало прослушивания указанного порта.
     * Используется пул из 10 потоков для обработки запросов.
     * @throws IOException если сервер не может быть запущен
     */
    public void start() throws IOException {
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            registerHandlers();
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            log.info("Сервер запущен на порту:" + " {" + port + "}");
        }
    }

    /**
     * Регистрация обработчика для всех эндпоинтов.
     */
    private void registerHandlers() {
        server.createContext("/tasks", new TaskHandler(taskManager, gson));
        server.createContext("/subtasks", new SubtaskHandler(taskManager, gson));
        server.createContext("/epics", new EpicHandler(taskManager, gson));
        server.createContext("/history", new HistoryHandler(taskManager, gson));
        server.createContext("/prioritized", new PrioritizedHandler(taskManager, gson));
    }

    /**
     * Остановка сервера.
     * Корректно завершает работу сервера, освобождая все ресурсы.
     * Метод вызывается автоматически при завершении работы JVM через shutdown hook.
     */
    public void stop() {
        if (server != null) {
            server.stop(0);
            log.info("Сервер остановлен");
            server = null;
        }
    }

    /**
     * Возвращение экземпляра Gson для сериализации/десериализации JSON.
     *
     * @return экземпляр Gson
     */
    public static Gson getGson() {
        return new Gson();
    }

    public static void main(String[] args) throws IOException {
        TaskManager taskManager = Managers.getDefault();
        HttpTaskServer server = new HttpTaskServer(taskManager);
        server.start();
    }
}
