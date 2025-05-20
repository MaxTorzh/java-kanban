package taskmanager.core.managers;

import taskmanager.core.exceptions.TimeConflictException;
import taskmanager.core.model.*;
import taskmanager.core.util.*;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private final HistoryManager historyManager;
    private boolean isUpdatingStatus = false; // Флаг для блокировки рекурсии
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>( // Автоматически сортирует элементы при добавлении
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder()) // задачи со startTime == null идут в конец
            )
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.idCounter = 1;
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory()); // Делегируем к конструктору выше
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
    public Optional<Task> getTaskById(int id) {
        return Optional.ofNullable(tasks.get(id))
                .map(task -> {
                    historyManager.add(task);
                    return task;
                });
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        return Optional.ofNullable(subtasks.get(id))
                .map(subtask -> {
                    historyManager.add(subtask);
                    return subtask;
                });
    }

    @Override
    public Optional<Epic> getEpicById(int id) {
        return Optional.ofNullable(epics.get(id))
                .map(epic -> {
                    historyManager.add(epic);
                    return epic;
                });
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory()); // Делегирует вызов истории, возвращаем копию списка
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks); // Создание копии для безопасности
    }

    @Override
    public void addTask(Task task) {
        if (task.getStartTime() != null && hasTimeOverlap(task)) { // Добавление проверки по пересечению задач
            throw new TimeConflictException("Задача пересекается по времени с существующей.");
        }
        task.setId(generateId()); // Автоматическая установка уникального идентификатора
        tasks.put(task.getId(), task); // Добавление задачи в мапу
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task); // Добавление задачи в приоритезированный список
        }
    }

    @Override
    public void addSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени");
        }
        subtask.setId(generateId()); // Автоматическая установка уникального идентификатора
        if (subtask.getEpicId() == subtask.getId()) { // Если подзадача является эпиком для себя
            throw new IllegalArgumentException("Подзадача не может быть эпиком для себя");
        }
        subtasks.put(subtask.getId(), subtask); // Добавление подзадачи в мапу
        Epic epic = epics.get(subtask.getEpicId()); // Получение эпика по идентификатору
        if (epic != null) { // Если эпик существует
            epic.addSubtask(subtask.getId()); // Добавление подзадачи в список подзадач у эпика
            updateEpicStatus(subtask.getEpicId()); // Обновление статуса эпика
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());// Автоматическая установка уникального идентификатора
        epics.put(epic.getId(), epic); // Добавление эпика в мапу
    }

    @Override
    public void updateEpicStatus(int epicId) {
        if (isUpdatingStatus) return; // Блокировка повторного вызова
        isUpdatingStatus = true;
        try {
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return;
            }
            List<Subtask> actualSubtasks = getAllSubtasksByEpicId(epicId); // Получение списка актуальных подзадач
            if (actualSubtasks.isEmpty()) { // Если список пустой, то статус = NEW
                epic.setStatus(Status.NEW);
                return;
            }
            boolean isAllDone = areAllSubtasks(actualSubtasks, Status.DONE); // Проверка, все ли подзадачи DONE
            boolean isAllNew = areAllSubtasks(actualSubtasks, Status.NEW); // Проверка, все ли подзадачи NEW

            epic.setStatus(
                    isAllDone ? Status.DONE :
                            isAllNew ? Status.NEW :
                                    Status.IN_PROGRESS
            );
        } finally {
            isUpdatingStatus = false;
        }
    }

    @Override
    public void updateTask(Task task) {
        if (task.getStartTime() != null && hasTimeOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей.");
        }
        prioritizedTasks.remove(task);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtask.getStartTime() != null && hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени");
        }
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
        historyManager.remove(id); // Удаление задачи из истории
        prioritizedTasks.remove(tasks.get(id)); // Удаление задачи из приоритезированного списка
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
        historyManager.remove(id); // Удаление подзадачи из истории
        prioritizedTasks.remove(subtask); // Удаление подзадачи из приоритезированного списка
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
        historyManager.remove(id); // Удаление эпика из истории
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

    /**
     * Проверка пересечения времени (линейная сложность O(n))
     * @param newTask - новая задача
     * @return - true, если пересечение есть, иначе false
     */
    @Override
    public boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .filter(task -> task.getStartTime() != null)
                .anyMatch(existingTask -> existingTask.isOverlapping(newTask));
    }

    private int generateId() { // Так как это внутренний счетчик класса, то метод должен быть приватный
        return idCounter++; // Создание счетчика идентификаторов
    }

    private boolean areAllSubtasks(List<Subtask> subtasks, Status status) {
        return subtasks.stream().allMatch(task -> task.getStatus() == status);
    }

    /**
     * Добавление задач при загрузке из файла
     * Данные уже были добавлены при вызове addTask или addSubtask
     * Дополнительная проверка в этом случае избыточна
     * @param task Задача для добавления
     */
    protected void internalAddTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void internalAddSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        }
    }

    protected void internalAddEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
}
