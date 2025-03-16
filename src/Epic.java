import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks;

    public Epic(String title, String description) {
        super(title, description);
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtask.setEpicId(getId()); // Установка идентификатора эпика
        this.subtasks.add(subtask); // Добавление подзадачи в список подзадач
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) { // Если у epic нет подзадач, то статус = NEW
            setStatus(Status.NEW);
            return;
        }
        boolean isAllDone = true; // Проверка, все ли подзадачи DONE
        boolean isAllNew = true; // Проверка, все ли подзадачи NEW
        for (Subtask subtask : subtasks) { // Перебор всех подзадач
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
        return "Epic{" +
                ", " + super.toString() +
                "subtasks=" + subtasks +
                '}';
    }
}
