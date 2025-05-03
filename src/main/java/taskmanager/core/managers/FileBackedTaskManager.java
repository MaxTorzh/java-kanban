package taskmanager.core.managers;

import taskmanager.core.exceptions.ManagerSaveException;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;
import taskmanager.core.util.TaskType;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static taskmanager.core.util.Status.NEW;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String filePath;

    public FileBackedTaskManager(String filePath) {
        super(); // Вызов конструктора родителя
        this.filePath = filePath; // Сохранение пути к файлу, где будут храниться данные
        loadFromFile(); // Загрузка данных из файла в память при инициализации
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        // Создание нового экземпляра, используя путь к файлу
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.loadFromFile(); // Восстановление данных из файла
        return manager; // Возврат готового менеджера с восстановленными данными
    }

    private void loadFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean headerPassed = false;

            while ((line = reader.readLine()) != null) {
                System.out.println("Читаю строку: " + line); // Логирование строки
                if (line.trim().isEmpty()) { // Пропуск пустых строк
                    continue;
                }
                if (!headerPassed) {
                    headerPassed = true; // Пропускаем первую строку (заголовок)
                    continue;
                }

                Task task = fromString(line);
                if (task instanceof Epic) {
                    addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    addSubtask((Subtask) task);
                } else {
                    addTask(task);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    private Task fromString(String value) {
        String[] fields = parseCsvLine(value); // Метод для корректного разбора CSV
        if (fields.length < 5) { // Проверка на минимальное количество полей
            throw new IllegalArgumentException("Некорректная строка " + value);
        }

        int id;
        try {
            id = Integer.parseInt(fields[0]); // Преобразование ID в число
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный ID: " + fields[0]);
        }

        TaskType type;
        try {
            type = TaskType.valueOf(fields[1]); // Преобразование типа задачи
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный тип задачи: " + fields[1]);
        }

        String title = fields[2]; // Извлечение третьего поля: название задачи
        Status status = NEW; // Установка значения по умолчанию
        if (!fields[3].isEmpty()) {
            try {
                status = Status.valueOf(fields[3]);
            } catch (IllegalArgumentException e) {
                System.out.println("Некорректный статус: " + fields[3] + ". Установлено значение по умолчанию: NEW");
            }
        }

        String description = fields.length > 4 ? fields[4] : "";
        int epicId = -1; // Значение по умолчанию
        if (fields.length > 5 && !fields[5].isEmpty()) { // Проверка, что поле epicId существует
            try {
                epicId = Integer.parseInt(fields[5]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный epicId: " + fields[5]);
            }
        }

        switch (type) {
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

    /* Переопределенные методы:
    - Вызов задачи родительского класса(super...), чтобы добавить задачу в коллекцию
    - Вызов метода save(), чтобы сохранить изменений в файл.
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

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getAllTasks()) {
                String taskString = toString(task);
                System.out.println("Записываю задачу: " + taskString); // Логирование
                writer.write(taskString + "\n");
            }
            for (Epic epic : getAllEpics()) {
                String epicString = toString(epic);
                System.out.println("Записываю эпик: " + epicString); // Логирование
                writer.write(epicString + "\n");
            }
            for (Subtask subtask : getAllSubtasks()) {
                String subtaskString = toString(subtask);
                System.out.println("Записываю подзадачу: " + subtaskString); // Логирование
                writer.write(subtaskString + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл", e);
        }
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes; // Переключаем режим кавычек
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0); // Очищаем буфер
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString().trim()); // Добавляем последнее поле

        // Проверяем, что строка содержит минимум 5 полей
        if (fields.size() < 5) {
            throw new IllegalArgumentException("Некорректная строка: " + line);
        }

        return fields.toArray(new String[0]);
    }

    private String toString(Task task) {
        TaskType type = task instanceof Epic ? TaskType.EPIC :
                task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK;

        String baseFormat = String.format("%d,%s,%s,%s,\"%s\"",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus() != null ? task.getStatus() : NEW,
                task.getDescription() != null ? task.getDescription() : "");

        if (task instanceof Subtask) {
            Subtask subtask = (Subtask) task;
            return baseFormat + "," + subtask.getEpicId(); // Для подзадач добавляем epicId
        }

        return baseFormat + ","; // Для задач и эпиков добавляем запятую в конце
    }
}