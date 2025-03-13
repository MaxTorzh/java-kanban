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
        if (subtaskIds.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (int subtaskId : subtaskIds) {
            Task subtask = taskManager.getTask(subtaskId);
            if (subtask == null) {
                System.out.println("Подзадача с ID " + subtaskId + " не найдена.");
                allDone = false;
                allNew = false;
                continue;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if  (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
        }
        if (allDone) {
            setStatus(Status.DONE);
        } else if (allNew) {
            setStatus(Status.NEW);
        } else {
            setStatus(Status.IN_PROGRESS);
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

