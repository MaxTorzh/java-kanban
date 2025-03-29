public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Метод для получения по умолчанию менеджера задач
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager(); // Метод для получения истории задач по умолчанию
    }
}
