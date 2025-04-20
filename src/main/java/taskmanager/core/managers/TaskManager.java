package taskmanager.core.managers;

import taskmanager.core.model.*;
import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();
    List<Subtask> getAllSubtasks();
    List<Epic> getAllEpics();

    void deleteAllTasks();
    void deleteAllSubtasks();
    void deleteAllEpics();

    Task getTaskById(int id);
    Subtask getSubtaskById(int id);
    Epic getEpicById(int id);

    List<Task> getHistory();

    void addTask(Task task);
    void addSubtask(Subtask subtask);
    void addEpic(Epic epic);

    void updateEpicStatus(int epicId);
    void updateTask(Task task);
    void updateSubtask(Subtask subtask);
    void updateEpic(Epic epic);

    void deleteTaskById(int id);
    void deleteSubtaskById(int id);
    void deleteEpicById(int id);

    List<Subtask> getAllSubtasksByEpicId(int epicId);
}
