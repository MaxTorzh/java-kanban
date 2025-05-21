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
    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(
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
        subtasks.clear(); // Очистка подзадач
        epics.values().forEach(epic -> epic.setSubtaskIds(new ArrayList<>())); // Очистка списка подзадач у эпиков
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
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
        if (hasTimeOverlap(task)) { // Добавление проверки по пересечению задач
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
        if (hasTimeOverlap(subtask)) {
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
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
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
        if (hasTimeOverlap(task)) {
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
        Subtask old = subtasks.get(subtask.getId());
        if (old != null && !old.equals(subtask)) {
            if (hasTimeOverlap(subtask)) { // Проверка только если задача реально изменилась
                throw new TimeConflictException("Подзадача пересекается по времени");
            }
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
        prioritizedTasks.remove(subtask);
        if (subtask.getStartTime() !=null) {
            prioritizedTasks.add(subtask);
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
        Optional.ofNullable(epics.get(id)) // Если эпик существует
                .map(Epic::getSubtaskIds) // Получение списка id подзадач
                .ifPresent(ids -> ids.forEach(this::deleteSubtaskById)); //  Удаление подзадач по этому ID
        epics.remove(id); // Удаление эпика из мапы
        historyManager.remove(id); // Удаление эпика из истории
    }

    @Override
    public List<Subtask> getAllSubtasksByEpicId(int epicId) { // Метод для получения всех подзадач по id эпика
        return Optional.ofNullable(epics.get(epicId)) // Если эпик существует
                .map(Epic::getSubtaskIds) // Получение списка id подзадач
                .stream() // Преобразование в поток, чтобы можно было применять методы map и filter
                .flatMap(List::stream) // Преобразование в поток, для каждого id подзадачи получение подзадачи
                .map(subtasks::get) // Получение подзадачи по id
                .filter(Objects::nonNull) // Фильтрация null значений
                .toList(); // Преобразование в список
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
