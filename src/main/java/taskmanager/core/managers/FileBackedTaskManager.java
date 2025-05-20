package taskmanager.core.managers;

import taskmanager.core.exceptions.ManagerSaveException;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

import static taskmanager.core.util.Status.NEW;

/**
 * Реализация менеджера задач с сохранением данных в файл.
 * Расширение InMemoryTaskManager, добавляет функционал сохранения состояния в CSV-файл.
 */
public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String filePath; // Путь к файлу для сохранения данных

    /**
     * Конструктор для создания нового менеджера задач с указанием пути к файлу
     * @param filePath - Путь к файлу для сохранения/загрузки данных
     */
    public FileBackedTaskManager(String filePath) {
        super(); // Инициализация базового класса с пустыми коллекциями
        this.filePath = filePath; // Сохранение пути к файлу
        loadFromFile(); // Загрузка данных из файла в память при инициализации
    }

    /**
     * Статический метод для создания нового экземпляра FileBackedTaskManager
     * с предварительной загрузкой данных из указанного файла.
     *
     * @param file файл с данными
     * @return новый экземпляр FileBackedTaskManager
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.exists() || !file.canRead()) {
            throw new ManagerSaveException("Не удалось открыть файл для чтения: " + file.getAbsolutePath());
        }
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.loadFromFile(); // Дополнительная загрузка (на случай если файл изменился после создания)
        return manager;
    }

    /**
     * Переопределенные методы для автоматического сохранения при изменении данных
     * Вызывают super-метод для изменения коллекции и save() для сохранения в файл
     */
    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    /**
     * Обертка вокруг метода fromString
     * @param line - строка с данными
     * @return - объект Task или null
     */
    private Optional<Task> safeFromString(String line) {
        try { // Оборачивание вызова fromString в try-catch
            return Optional.of(fromString(line)); // При успехе возвращается Optional.of(task)
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректная строка: " + line + ". Пропуск.");
            return Optional.empty(); // При ошибке возвращается Optional.empty() с логированием ошибки
        }
    }
    /**
     * Приватный метод для загрузки задач из файла в текущий менеджер.
     * Если файл не существует или пуст — ничего не происходит.
     */
    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return;
        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\\R");

            List<Task> tasks = Arrays.stream(lines) // Создание потока из массива строк файла
                    .skip(1) // Пропуск первой строки (заголовок CSV)
                    .filter(line -> !line.trim().isEmpty()) // Фильтрация пустых строк
                    .filter(line -> line.matches("^\\d+.*")) // Фильтрация строк, которые начинаются с ID
                    .map(this::safeFromString) // Преобразование строк (Optional.of(task)) или Optional.empty()
                    .flatMap(Optional::stream) // Оборачивается в Stream<Task> или Stream.empty(). Зависит от Optional
                    .toList(); // Сбор всех полученных задач в список
            tasks.forEach(task -> { // Добавление каждой задачи в соответствующие коллекции
                if (task instanceof Epic epic) internalAddEpic(epic);
                else if (task instanceof Subtask subtask) internalAddSubtask(subtask);
                else internalAddTask(task);
            });

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    /**
     * Преобразование строки CSV в объект задачи
     * @param value - Строка с данными задачи
     * @return - Объект задачи (Task, Epic или Subtask)
     */
    private Task fromString(String value) {
        String[] fields = parseCsvLine(value); // Метод для корректного разбора CSV
        if (fields.length < 7) { // Проверка на минимальное количество полей
            throw new IllegalArgumentException("Некорректная строка " + value);
        }

        int id; // Парсинг ID
        try {
            id = Integer.parseInt(fields[0]); // Первое поле: преобразование ID в число
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный ID: " + fields[0]);
        }

        TaskType type; //Определение типа задачи
        try {
            type = TaskType.valueOf(fields[1]); // Второе поле: преобразование типа задачи
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный тип задачи: " + fields[1]);
        }

        String title = fields[2]; // Третье поле: название задачи
        Status status = NEW; // Значение по умолчанию
        if (!fields[3].isEmpty()) {
            try {
                status = Status.valueOf(fields[3]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Некорректный статус: " + fields[3]);
            }
        }

        // Обработка экранирования кавычек
        String description = fields[4].isEmpty() ? "" : fields[4].replace("\"\"", "\"");

        // Парсинг времени и продолжительности
        LocalDateTime startTime = null;
        if (!fields[5].isEmpty()) { // Парсится только если поле не пустое
            try {
                startTime = LocalDateTime.parse(fields[5], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Некорректный формат времени: " + fields[5]);
            }
        }

        Duration duration = null;
        if (!fields[6].isEmpty()) { // Парсится только если поле не пустое
            try {
                duration = Duration.ofMinutes(Long.parseLong(fields[6]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректная продолжительность: " + fields[6]);
            }
        }

        int epicId = -1; // Обработка epicId для подзадач
        if (type == TaskType.SUBTASK) {
            if (fields.length < 8 || fields[7].isEmpty()) {
                throw new IllegalArgumentException("Отсутствует epicId для подзадачи");
            }
            try {
                epicId = Integer.parseInt(fields[7]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный epicId: " + fields[7]);
            }
        }

        switch (type) { // Создание соответствующего типа задач
            case TASK:
                Task task = new Task(title, description, status, duration, startTime);
                task.setId(id); // Установка ID
                task.setStatus(status); // Установка статуса
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id); // Установка ID
                epic.setStatus(status); // Установка статуса
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, status, epicId, duration, startTime);
                subtask.setId(id); // Установка ID
                subtask.setStatus(status); // Установка статуса
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Метод для корректного разбора CSV-строки с учетом кавычек
     * @param line - Строка CSV для разбора
     * @return - Массив значений, извлеченных из строки
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false; // Флаг для отслеживания кавычек
        boolean isEscaped = false;

        for (char c : line.toCharArray()) { // Поочередная обработка каждого символа строки
            if (c == '"') {
                if (isEscaped) {
                    currentField.append(c);
                } else {
                    inQuotes = !inQuotes; // Переключение режима кавычек
                }
            } else if (c == ',' && !inQuotes) { // Запятая вне кавычек - разделитель полей
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else if (c == '\\' && inQuotes) {
                isEscaped = true;
            } else {
                currentField.append(c); // Добавление символа к текущему полю
            }
        }
        fields.add(currentField.toString().trim()); // Добавление последнего поля

        // Добавление пустых значений для недостающих полей
        while (fields.size() < 6) {
            fields.add("");
        }

        return fields.toArray(new String[0]);
    }
    /**
     * Метод для сохранения текущего состояния в файл
     * Сохраняет все задачи, эпики и подзадачи в формате CSV
     */
    private void save() {
        Set<Integer> writtenIds = new HashSet<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,start_time,duration,epic\n");

            writeTasks(writer, writtenIds, getAllTasks(), this::toString);
            writeTasks(writer, writtenIds, getAllEpics(), this::toString);
            writeTasks(writer, writtenIds, getAllSubtasks(), this::toString);

            writer.flush();
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл", e);
        }
    }

    /**
     * Универсальный метод для записи задач в файл
     * @param writer - куда будут записаны строки
     * @param writtenIds - множество ID для проверки дубликатов
     * @param tasks - множество задач любого типа
     * @param toStringFunction - интерфейс, принимающий объект типа T и возвращающий строку (toString(task)).
     * @param <T> - тип задачи
     */
    private <T extends Task> void writeTasks(
            BufferedWriter writer,
            Set<Integer> writtenIds,
            List<T> tasks, Function<T, String> toStringFunction) {

        tasks.stream() // Создание потока из списка задач
                .filter(task -> writtenIds.add(task.getId())) // Сортировка дубликатов по ID
                .map(toStringFunction) // Преобразование задачи в строку CSV
                .forEach(line -> { // Запись каждой строки в файл
                    try { // Поиск возможных исключения и оборачивание их в RuntimeException
                        writer.write(line + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Преобразование задачи в строку CSV
     * @param task Задача для преобразования
     * @return Строка в формате CSV
     */
    private String toString(Task task) {
        String description = task.getDescription() != null
                ? task.getDescription().replace("\"", "\"\"") // Экранирование кавычек
                : "";
        // Определение типа задачи
        TaskType type = task instanceof Epic ? TaskType.EPIC :
                task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK;
        // Форматирование startTime и duration
        String startTimeString = task.getStartTime() != null
                ? task.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // Преобразование в строку стандартного формата
                : "";
        String durationString = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes()) // Сохранение продолжительности в минутах
                : "";
        // Базовый формат для всех типов задач
        String baseFormat = String.format("%d,%s,%s,%s,\"%s\", %s, %s", // Форматирование всех полей в строку CSV
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus() != null ? task.getStatus() : NEW,
                description,
                startTimeString,
                durationString);
        // Для подзадач добавляется ID эпика
        return baseFormat + (task instanceof Subtask ? "," + ((Subtask) task).getEpicId() : "");
    }

    /**
     * Основной метод для тестирования работы FileBackedTaskManager.
     * Создаёт задачи, сохраняет их в файл и проверяет корректность загрузки.
     */
    public static void main(String[] args) {
        try {
            // 1. Создание временного файла
            File tempFile = File.createTempFile("scenario-task", ".csv");
            System.out.println("Тестовый файл создан: " + tempFile.getAbsolutePath());

            // 2. Создание первого менеджера и добавление данных
            FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            // Добавление задачи с временными параметрами
            Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                    Duration.ofMinutes(30),
                    LocalDateTime.of(2023, 1, 1, 10, 0));
            manager1.addTask(task1);

            // Добавление эпика
            Epic epic1 = new Epic("Epic 1", "Description 1");
            manager1.addEpic(epic1);

            // Добавление подзадачи с привязкой к эпику и временными параметрами
            Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW,
                    epic1.getId(),
                    Duration.ofMinutes(60),
                    LocalDateTime.of(2023, 1, 1, 11, 0));
            manager1.addSubtask(subtask1);

            // 3. Создание второго менеджера из того же файла
            FileBackedTaskManager manager2 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            // 4. Проверка соответствия данных
            System.out.println("\nПроверка данных в новом менеджере:");

            // Проверка задач
            assert manager2.getAllTasks().size() == 1 : "Задачи не совпадают";
            Task loadedTask = manager2.getTaskById(task1.getId()).orElse(null);
            assert loadedTask != null : "Задача 1 не найдена";
            assert loadedTask.getTitle().equals("Task 1") : "Неверное название задачи";
            assert loadedTask.getDuration().equals(Duration.ofMinutes(30)) : "Неверная продолжительность задачи";
            assert loadedTask.getStartTime().equals(LocalDateTime.of(2023, 1, 1, 10, 0)) : "Неверное время начала задачи";

            // Проверка эпиков
            assert manager2.getAllEpics().size() == 1 : "Эпики не совпадают";
            Epic loadedEpic = manager2.getEpicById(epic1.getId()).orElse(null);
            assert loadedEpic != null && loadedEpic.getTitle().equals("Epic 1") : "Эпик 1 не найден";

            // Проверка расчета времени эпика
            assert loadedEpic.getStartTime().equals(subtask1.getStartTime()) : "Неверное время начала эпика";
            assert loadedEpic.getDuration().equals(subtask1.getDuration()) : "Неверная продолжительность эпика";
            assert loadedEpic.getEndTime().equals(subtask1.getEndTime()) : "Неверное время окончания эпика";

            // Проверка подзадач
            assert manager2.getAllSubtasks().size() == 1 : "Подзадачи не совпадают";
            Subtask loadedSubtask = manager2.getSubtaskById(subtask1.getId()).orElse(null);
            assert loadedSubtask != null && loadedSubtask.getTitle().equals("Subtask 1") : "Подзадача 1 не найдена";
            assert loadedSubtask.getEpicId() == epic1.getId() : "Неверная привязка к эпику";
            assert loadedSubtask.getDuration().equals(Duration.ofMinutes(60)) : "Неверная продолжительность подзадачи";
            assert loadedSubtask.getStartTime().equals(LocalDateTime.of(2023, 1, 1, 11, 0)) : "Неверное время начала подзадачи";

            System.out.println("Все проверки пройдены успешно!");

            // Удаление временного файла
            tempFile.deleteOnExit();

        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }
}