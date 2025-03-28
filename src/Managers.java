public class Managers {
    private static HistoryManager defaultHistory = new InMemoryHistoryManager();

    public static void setHistoryManager(HistoryManager historyManager) {
        defaultHistory = historyManager;
    }
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return defaultHistory;
    }
}
