import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Subtask> subtasks;
    private HashMap<Integer, Epic> epics;

    public InMemoryTaskManager() {
        this.idCounter = 1;
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
    }

    public List<Task> getHistory() {
        return new ArrayList<>(); // Получение истории. Возврат копии списка, для защиты от изменений
    }

    @Override
    public int generateId() {
        return idCounter++; // Создание счетчика идентификаторов
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values()); // Получение всех задач. Возврат копии списка, для защиты от изменений
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values()); // Получение всех подзадач. Возврат копии списка, аналог tasks
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values()); // Получение всех эпиков. Возврат копии списка, аналог tasks
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) { // Перебор всех значений в мапе эпика
            epic.getSubtaskIds().clear(); // Очистка списка подзадач у каждого эпика
        }
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    @Override
    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    @Override
    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    @Override
    public void addTask(Task task) {
        task.setId(generateId()); // Установка уникального идентификатора
        tasks.put(task.getId(), task); // Добавление задачи в мапу
    }

    @Override
    public void addSubtask(Subtask subtask) {
        subtask.setId(generateId()); // Установка уникального идентификатора
        subtasks.put(subtask.getId(), subtask); // Добавление подзадачи в мапу
        Epic epic = epics.get(subtask.getEpicId()); // Получение эпика по идентификатору
        if (epic != null) { // Если эпик существует
            epic.addSubtask(subtask.getId()); // Добавление подзадачи в список подзадач у эпика
            updateEpicStatus(subtask.getEpicId()); // Обновление статуса эпика
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());// Установка уникального идентификатора
        epics.put(epic.getId(), epic); // Добавление эпика в мапу
    }

    @Override
    public void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        List<Subtask> actualSubtasks = getAllSubtasksByEpicId(epicId); // Получение списка актуальных подзадач
        if (actualSubtasks.isEmpty()) { // Если список пустой, то статус = NEW
            epic.setStatus(Status.NEW);
            return;
        }
        boolean isAllDone = true; // Проверка, все ли подзадачи DONE
        boolean isAllNew = true; // Проверка, все ли подзадачи NEW
        for (Subtask subtask : actualSubtasks) { // Перебор всех актуальных подзадач
            if (subtask.getStatus() != Status.DONE) { // Если статус подзадачи не DONE, то isAllDone = false
                isAllDone = false;
            }
            if (subtask.getStatus() != Status.NEW) { // Если статус подзадачи не NEW, то isAllNew = false
                isAllNew = false;
            }
        }
        if (isAllDone) {
            epic.setStatus(Status.DONE); // Если у epic все подзадачи DONE, то статус = DONE
        } else if (isAllNew) {
            epic.setStatus(Status.NEW); // Если у epic все подзадачи NEW, то статус = NEW
        } else {
            epic.setStatus(Status.IN_PROGRESS); // Если у epic подзадачи DONE и NEW, то статус = IN_PROGRESS
        }
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id); // Удаление задачи из мапы
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id); // Создание объекта подзадачи для удобного доступа к методам
        if (subtask != null) { // Проверка существования подзадачи
            int epicId = subtask.getEpicId(); // Получение идентификатора эпика
            Epic epic = epics.get(epicId); // Получение эпика по идентификатору
            if (epic != null) { // Если эпик существует
                if (epic.getSubtaskIds().contains(id)) { // Если подзадача принадлежит эпику
                    epic.getSubtaskIds().remove(Integer.valueOf(id)); // Удаление подзадачи по значению, а не по индексу
                }
                updateEpicStatus(epic.getId()); // Обновление статуса эпика
            }
        }
        subtasks.remove(id); // Удаление подзадачи из мапы
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.get(id); // Получение эпика по идентификатору
        if (epic != null) { // Если эпик существует
            for (Integer subtaskId : new ArrayList<>(epic.getSubtaskIds())) { // Перебор всех id подзадач
                deleteSubtaskById(subtaskId); // Удаление подзадач по id из оригинального списка
            }
        }
        epics.remove(id); // Удаление эпика из мапы
    }

    @Override
    public List<Subtask> getAllSubtasksByEpicId(int epicId) { // Метод для получения всех подзадач по id эпика
        Epic epic = epics.get(epicId); // Получение эпика по id
        if (epic == null) { // Если эпик не существует
            return new ArrayList<>(); // Возврат пустого списка
        }
        List<Subtask> result = new ArrayList<>(); // Создание списка для результата
        for (Integer subtaskId : epic.getSubtaskIds()) { // Перебор всех id подзадач
            Subtask subtask = subtasks.get(subtaskId); // Получение подзадачи по id
            if (subtask != null) { // Если подзадача существует
                result.add(subtask); // Добавление подзадачи в список
            }
        }
        return result; // Возврат списка подзадач
    }
}
