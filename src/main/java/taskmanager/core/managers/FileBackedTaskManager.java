package taskmanager.core.managers;

import taskmanager.core.exceptions.ManagerSaveException;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TaskType;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

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
     * Статический метод для загрузки менеджера из существующего файла
     * @param file - Файл с сохраненными данными
     * @return - Новый экземпляр FileBackedTaskManager с загруженными данными
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.loadFromFile(); // Дополнительная загрузка (на случай если файл изменился после создания)
        return manager;
    }

    /**
     * Метод загрузки данных из файла в память
     * Обрабатывает возможные ошибки чтения и дубликаты ID
     */
    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return; // Если файл не существует - нет данных для загрузки

        Set<Integer> usedIds = new HashSet<>(); // Хранилище для уже использованных ID

         try {
            String content = Files.readString(file.toPath()); // Чтение всего содержимого файла
            String[] lines = content.split("\\R"); // Разделение на строки с универсальным разделителем
            boolean headerPassed = false; // Флаг для пропуска заголовка

            for (String line : lines) {
                if (line.trim().isEmpty()) continue; // Пропуск пустых строк
                if (!headerPassed) {
                    headerPassed = true; // Пропуск первой строки (заголовка)
                    continue;
                }

                // Проверка, что строка начинается с цифры (ID)
                if (!line.matches("^\\d+.*")) continue;

                try {
                    Task task = fromString(line); // Преобразование строки в задачу
                    if (usedIds.contains(task.getId())) { // Проверка на дубликат ID
                        System.out.println("Пропускаю дубликат ID: " + task.getId());
                        continue;
                    }
                    usedIds.add(task.getId()); // Запоминание использованного ID

                    if (task instanceof Epic) { // Добавление задачи в соответствующую коллекцию
                        addEpic((Epic) task);
                    } else if (task instanceof Subtask) {
                        addSubtask((Subtask) task);
                    } else {
                        addTask(task);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Некорректная строка: " + line + ". Пропуск.");
                }
            }
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
        if (fields.length < 5) { // Проверка на минимальное количество полей
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

        String description = fields.length > 4 ? fields[4] : "";
        int epicId = -1; // Обработка epicId для подзадач
        if (type == TaskType.SUBTASK) {
            if (fields.length < 6 || fields[5].isEmpty()) {
                throw new IllegalArgumentException("Отсутствует epicId для подзадачи");
            }
            try {
                epicId = Integer.parseInt(fields[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный epicId: " + fields[5]);
            }
        }

        switch (type) { // Создание соответствующего типа задач
            case TASK:
                Task task = new Task(title, description);
                task.setId(id); // Установка ID
                task.setStatus(status); // Установка статуса
                return task;
            case EPIC:
                Epic epic = new Epic(title, description);
                epic.setId(id); // Установка ID
                epic.setStatus(status); // Установка статуса
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(title, description, status, epicId);
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

        for (char c : line.toCharArray()) { // Поочередная обработка каждого символа строки
            if (c == '"') {
                inQuotes = !inQuotes; // Переключение режима кавычек
            } else if (c == ',' && !inQuotes) { // Запятая вне кавычек - разделитель полей
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
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
     * Метод для сохранения текущего состояния в файл
     * Сохраняет все задачи, эпики и подзадачи в формате CSV
     */
    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,epic\n"); // Запись заголовка CSV
            for (Epic epic : getAllEpics()) { // Сохранение всех эпиков
                writer.write(toString(epic) + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) { // Сохранение всех подзадач
                writer.write(toString(subtask) + "\n");
            }
            for (Task task : getAllTasks()) { // Сохранение всех задач
                writer.write(toString(task) + "\n");
            }
            writer.flush(); // Гарантируется запись на диск
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл", e);
        }
    }

    /**
     * Преобразование задачи в строку CSV
     * @param task Задача для преобразования
     * @return Строка в формате CSV
     */
    private String toString(Task task) {
        // Определение типа задачи
        TaskType type = task instanceof Epic ? TaskType.EPIC :
                task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK;
        // Базовый формат для всех типов задач
        String baseFormat = String.format("%d,%s,%s,%s,\"%s\"",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus() != null ? task.getStatus() : NEW,
                task.getDescription() != null ? task.getDescription() : "");
        // Для подзадач добавляется ID эпика
        return baseFormat + "," + (task instanceof Subtask ? ((Subtask) task).getEpicId() : "");
    }

    /**
     * Тестовый метод для проверки функционала сохранения и восстановления данных
     */
    public static void main(String[] args) {
        String filePath = "tasks.csv"; // Путь к тестовому файлу

        // 1. Создание первого менеджера и добавление задачи
        FileBackedTaskManager manager1 = new FileBackedTaskManager(filePath);

        // Создание и добавление эпика
        Epic epic1 = new Epic("Epic 1", "Description 1");
        manager1.addEpic(epic1);

        // Создание и добавление подзадачи, привязанной к эпику
        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.IN_PROGRESS, epic1.getId());
        manager1.addSubtask(subtask1);

        // Создание и добавление обычной задачи
        Task task1 = new Task("Task 1", "Description 1");
        manager1.addTask(task1);

        // 2. Создание второго менеджера, загружая данные из того же файла
        FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(new File(filePath));

        // 3. Проверка корректности восстановленных данных
        System.out.println("\n=== Проверка восстановленных данных ===");

        // Проверка обычной задачи
        Task restoredTask = manager2.getTaskById(task1.getId());
        System.out.println("Задача восстановлена: " + (restoredTask != null ? "ДА" : "НЕТ"));

        // Проверка эпика
        Epic restoredEpic = manager2.getEpicById(epic1.getId());
        System.out.println("Эпик восстановлен: " + (restoredEpic != null ? "ДА" : "НЕТ"));

        // Проверка подзадачи
        Subtask restoredSubtask = manager2.getSubtaskById(subtask1.getId());
        System.out.println("Подзадача восстановлена: " + (restoredSubtask != null ? "ДА" : "НЕТ"));

        // Проверка связи подзадачи с эпиком
        if (restoredSubtask != null) {
            System.out.println("ID эпика у подзадачи совпадает: " +
                    (restoredSubtask.getEpicId() == epic1.getId() ? "ДА" : "НЕТ"));
        }
    }
}