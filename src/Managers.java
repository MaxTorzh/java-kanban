public class Managers {
    private static HistoryManager defaultHistory = new InMemoryHistoryManager(); // Создается история задач по умолчанию

    public static TaskManager getDefault() {
        return new InMemoryTaskManager(); // Метод для получения по умолчанию менеджера задач
    }

    public static void setHistoryManager(HistoryManager historyManager) {
        defaultHistory = historyManager; // Метод для установки истории задач
    }

    public static HistoryManager getDefaultHistory() {
        return defaultHistory; // Метод для получения истории задач по умолчанию
    }
}
