import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtaskIds; //список идентификаторов подзадач

    public Epic(String title, String description, int id, Status status) {
        super(title, description, id, status);
        this.subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtasksIds() {
        return subtaskIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtaskIds = subtasksIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
    }
}

