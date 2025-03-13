import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private int idCounter;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        this.idCounter = 1; //инициализация счетчика
        this.tasks = new HashMap<>(); //инициализация мапы заданий
        this.epics = new HashMap<>(); // инициализация мапы эпиков
        this.subtasks = new HashMap<>(); //инициализация мапы подзаданий
    }

    public int generateID() { // создание счетчика
        return idCounter++;
    }

    public void addTask(Task task) {
        tasks.put(task.getId(), task); // добавление задачи в мапу задач
    }

    public void addEpic(Epic epic) {
        epics.put(epic.getId(), epic); // добавление задач в мапу эпиков
    }

    public void addSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask); // добавление задач в мапу подзадач
        Epic epic = epics.get(subtask.getEpicId()); // проверка на null, чтобы избежать ошибки
        if (epic != null) { // если такого эпика не существует
            epic.addSubtaskId(subtask.getId());
        }
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    public void removeTask(int id) {
        tasks.remove(id);
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void updateSubtask(Subtask subtask) {

    }

    public void removeEpic(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            for (int subtaskId : epic.getSubtasksIds()) {
                subtasks.remove(subtaskId);
            }
        }
        epics.remove(id);
    }

    public void removeSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtasksIds().remove(id);
                epic.updateStatus(this);
            }
        }
        subtasks.remove(id);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void clearEpics() {
        epics.clear();
    }

    public void clearSubtasks() {
        subtasks.clear();
    }
}
