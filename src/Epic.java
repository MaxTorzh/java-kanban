import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private List<Subtask> subtasks;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        this.subtasks = new ArrayList<>();
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Subtask> subtasks) {
        this.subtasks = subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtask.setEpicId(getId());
        this.subtasks.add(subtask);
    }

    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
            // Если у epic нет подзадач, то статус = NEW
        }
        boolean isAllDone = true;
        boolean isAllNew = true;
        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.DONE) {
                isAllDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                isAllNew = false;
            }
        }
        if (isAllDone) {
            setStatus(Status.DONE);
            // Если у epic все подзадачи DONE, то статус = DONE
        } else if (isAllNew) {
            setStatus(Status.NEW);
            // Если у epic все подзадачи NEW, то статус = NEW
        } else {
            setStatus(Status.IN_PROGRESS);
            // Если у epic подзадачи DONE и NEW, то статус = IN_PROGRESS
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
