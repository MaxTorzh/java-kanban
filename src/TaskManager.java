import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {
    private int idCounter;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Subtask> subtasks;
    private HashMap<Integer, Epic> epics;

    public TaskManager() {
        this.idCounter = 1;
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public int generateId() {
        return idCounter++; // Создание счетчика идентификаторов
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values()); // Получение всех задач. Возврат копии списка, для защиты от изменений
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values()); // Получение всех подзадач. Возврат копии списка, аналог tasks
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values()); // Получение всех эпиков. Возврат копии списка, аналог tasks
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public void addTask(Task task) {
        task.setId(generateId()); // Установка уникального идентификатора
        tasks.put(task.getId(), task); // Добавление задачи в мапу
    }

    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId()); // Установка уникального идентификатора
        subtasks.put(subtask.getId(), subtask); // Добавление подзадачи в мапу
        Epic epic = epics.get(subtask.getEpicId()); // Получение эпика по идентификатору
        if (epic != null) { // Если эпик существует
            epic.addSubtask(subtask); // Добавление подзадачи в список подзадач у эпика
            epic.updateStatus(); // Обновление статуса эпика
        }
    }

    public void addEpic(Epic epic) {
        epic.setId(generateId()); // Установка уникального идентификатора
        epics.put(epic.getId(), epic); // Добавление эпика в мапу
    }

    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.updateStatus();
        }
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    public void deleteTaskById(int id) {
        tasks.remove(id); // Удаление задачи из мапы
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id); // Создание объекта подзадачи для удобного доступа к методам
        if (subtask != null) { // Проверка существования подзадачи
            int epicId = subtask.getEpicId(); // Получение идентификатора эпика
            Epic epic = epics.get(epicId); // Получение эпика по идентификатору
            if (epic != null) { // Если эпик существует
                epic.getSubtasks().remove(subtask); // Удаление подзадачи из списка подзадач у эпика
                epic.updateStatus(); // Обновление статуса эпика
            }
        }
        subtasks.remove(id); // Удаление подзадачи из мапы
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id); // Получение эпика по идентификатору
        if (epic != null) { // Если эпик существует
            List<Subtask> subtaskToDelete = new ArrayList<>(getAllSubtasksByEpicId(id)); // Получение всех подзадач эпика
            for (Subtask subtask : subtaskToDelete) {
                deleteSubtaskById(subtask.getId()); // Удаление подзадачи
            }
        }
        epics.remove(id); // Удаление эпика из мапы
    }

    public List<Subtask> getAllSubtasksByEpicId(int epicId) {
        return epics.get(epicId).getSubtasks(); // Получение всех подзадач эпика
    }
}
