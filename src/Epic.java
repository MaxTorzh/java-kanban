import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; // id подзадач, относящихся к epic
    private TaskManager taskManager; // Для доступа к методам

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>(); // Создание списка id подзадач
    }

    public List<Integer> getSubtaskIds() {
        return new ArrayList<>(subtaskIds); // Возврат копии списка id подзадач
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskManager(TaskManager taskManager) {
        this.taskManager = taskManager;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    private List<Subtask> getActualSubtasks() { // Создание метода для получения актуальных подзадач
        List<Subtask> actualSubtasks = new ArrayList<>(); // Создание списка актуальных подзадач
        for (Integer subtaskId : subtaskIds) { // Перебор всех id подзадач
            Subtask subtask = taskManager.getSubtaskById(subtaskId); // Получение подзадачи по id
            if (subtask != null) { // Если подзадача существует
                actualSubtasks.add(subtask); // Добавление подзадачи в список актуальных
            }
        }
        return actualSubtasks; // Возврат списка актуальных подзадач
    }

    public void updateStatus() {
        List<Subtask> actualSubtasks = getActualSubtasks(); // Получение списка актуальных подзадач
        if (actualSubtasks.isEmpty()) { // Если список пустой, то статус = NEW
            setStatus(Status.NEW);
            return;
        }
        boolean isAllDone = true; // Проверка, все ли подзадачи DONE
        boolean isAllNew = true; // Проверка, все ли подзадачи NEW
        for (Subtask subtask : actualSubtasks) { // Перебор всех актуальных подзадач
            if (subtask.getStatus() != Status.DONE) { // Если статус подзадачи не DONE, то isAllDone = false
                isAllDone = false;
            }
            if (subtask.getStatus() != Status.NEW) { // Если статус подзадачи не NEW, то isAllNew = false
                isAllNew = false;
            }
        }
        if (isAllDone) {
            setStatus(Status.DONE); // Если у epic все подзадачи DONE, то статус = DONE
        } else if (isAllNew) {
            setStatus(Status.NEW); // Если у epic все подзадачи NEW, то статус = NEW
        } else {
            setStatus(Status.IN_PROGRESS); // Если у epic подзадачи DONE и NEW, то статус = IN_PROGRESS
        }
    }

    @Override
    public String toString() {
        List<Integer> actualSubtaskIds = new ArrayList<>(); // Создание списка id актуальных подзадач
        for (Subtask subtask : getActualSubtasks()) { // Перебор всех актуальных подзадач
            actualSubtaskIds.add(subtask.getId()); // Добавление id подзадачи в список
        }
        return "Epic{" +
                ", " + super.toString() +
                "subtasksIds=" + actualSubtaskIds + // Вывод списка id подзадач
                '}';
    }
}
