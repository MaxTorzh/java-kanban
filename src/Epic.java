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

    public void updateStatus(TaskManager taskManager) {
        List<Subtask> subtasks = taskManager.getSubtaskByEpicId(this.getId());
        if (subtaskIds.isEmpty()) {
            this.setStatus(Status.NEW);
        } else {
            boolean allDone = true;
            boolean anyInProgress = false;
            for (Subtask subtask : subtasks) {
                if (subtask.getStatus() == Status.NEW) {
                    anyInProgress = true;
                } else if (subtask.getStatus() != Status.DONE) {
                    allDone = false;
                    anyInProgress = true;
                }
            }
            if (allDone) {
                this.setStatus(Status.DONE);
            } else if (anyInProgress) {
                this.setStatus(Status.IN_PROGRESS);
            } else {
                this.setStatus(Status.NEW);
            }
        }
    }

    @Override
    public String toString() {
        return "Epic{" +
                "subtaskIds=" + subtaskIds +
                ", " + super.toString() +
                '}';
    }
}

