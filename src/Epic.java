import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Integer> subtaskIds; // id подзадач, относящихся к epic

    public Epic(String title, String description) {
        super(title, description);
        this.subtaskIds = new ArrayList<>(); // Создание списка id подзадач
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds; // Возврат оригинала списка id подзадач
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        subtaskIds.add(subtaskId);
    }

    @Override
    public String toString() {
        return "Epic{" +
                ", " + super.toString() +
                "subtasksIds=" + subtaskIds + // Вывод списка id подзадач
                '}';
    }
}
