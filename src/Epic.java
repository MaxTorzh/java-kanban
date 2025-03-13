import java.util.List;

public class Epic extends Task {

    private List<Integer> subtasksIds;

    public Epic(String title, String description, int id, Status status, List<Integer> subtasksIds) {
        super(title, description, id, status);
        this.subtasksIds = subtasksIds;
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtasksIds.add(subtaskId);
    }
}

