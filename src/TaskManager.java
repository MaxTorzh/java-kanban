import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private int idCounter;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subTasks;

    public TaskManager() {
        this.idCounter = 1; //инициализация счетчика
        this.tasks = new HashMap<>(); //инициализация мапы заданий
        this.epics = new HashMap<>(); // инициализация мапы эпиков
        this.subTasks = new HashMap<>(); //инициализация мапы подзаданий
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
        subTasks.put(subtask.getId(), subtask); // добавление задач в мапу подзадач
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subTasks.values());
    }
}
