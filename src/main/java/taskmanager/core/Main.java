package taskmanager.core;

import taskmanager.core.managers.*;
import taskmanager.core.model.*;
import taskmanager.core.util.Status;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Основной класс приложения для тестирования функциональности менеджера задач.
 * Содержит тесты для проверки работы с файлами и различных операций с задачами.
 */
public class Main {
    public static void main(String[] args) {
        try {

            /**
             * Тест 1: Проверка работы с пустым файлом
             * - Создание временного пустого файла
             * - Проверка, что загруженные коллекции задач пусты
             */
            File emptyFile = File.createTempFile("empty-task", ".csv");
            System.out.println("Пустой файл создан: " + emptyFile.getAbsolutePath());

            FileBackedTaskManager emptyManager = new FileBackedTaskManager(emptyFile.getAbsolutePath());

            assert emptyManager.getAllTasks().isEmpty();
            assert emptyManager.getAllEpics().isEmpty();
            assert emptyManager.getAllSubtasks().isEmpty();

            System.out.println("Проверка пустого файла успешна.");
            emptyFile.deleteOnExit();

            /**
             * Тест 2: Сохранение и загрузка нескольких задач
             * - Создание временного файла с тестовыми данными
             * - Проверка корректности чтения данных из файла
             * - Проверка дублированной загрузки из того же файла
             */
            File tempFile = File.createTempFile("temp-task", ".csv");
            System.out.println("Временный файл создан: " + tempFile.getAbsolutePath());

            // Запись тестовых данных в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("id,type,name,status,description,start_time,duration,epic\n");
                writer.write("1,TASK,Task1,NEW,Description1,2025-04-05T10:00,30,\n");
                writer.write("2,TASK,Task2,IN_PROGRESS,Description2,2025-04-05T11:00,60,\n");
                writer.write("3,EPIC,Epic1,IN_PROGRESS,Description1,,,\n");
                writer.write("4,SUBTASK,Subtask1,NEW,Description1,2025-04-05T12:00,60,3\n");
                writer.write("5,SUBTASK,Subtask2,DONE,Description2,2025-04-05T13:10,60,3\n");
            } catch (IOException e) {
                System.out.println("Ошибка при записи во временный файл: " + e.getMessage());
                return;
            }

            // Загрузка первого менеджера
            FileBackedTaskManager fm1 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            // Вывод содержимого файла для проверки
            System.out.println("Содержимое файла:");
            System.out.println(Files.readString(tempFile.toPath()));

            // Вывод состояния менеджера
            System.out.println("Состояние первого менеджера:");
            printAllTasks(fm1);

            // Загрузка второго менеджера из того же файла
            FileBackedTaskManager fm2 = FileBackedTaskManager.loadFromFile(tempFile);

            System.out.println("Состояние второго менеджера после загрузки из файла:");
            printAllTasks(fm2);

            /**
             * Тест 3: Проверка сохранения изменений
             * - Создание файла для тестирования обновления данных
             * - Добавление и последующее обновление задачи
             * - Проверка корректности сохраненных изменений
             */
            File updateFile = File.createTempFile("update-task", ".csv");
            System.out.println("Файл для обновления создан: " + updateFile.getAbsolutePath());

            FileBackedTaskManager updateManager = new FileBackedTaskManager(updateFile.getAbsolutePath());

            Task task = new Task("TaskToUpdate", "Old Description");
            updateManager.addTask(task);

            // Обновление задачи
            task.setDescription("New Description");
            task.setStatus(Status.DONE);
            updateManager.updateTask(task);

            // Загрузка из файла
            FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(updateFile);

            Task loadedTask = loadedManager.getTaskById(task.getId()).orElse(null);
            assert loadedTask != null;
            assert loadedTask.getDescription().equals("New Description");
            assert loadedTask.getStatus() == Status.DONE;

            System.out.println("Проверка сохранения изменений успешна.");
            updateFile.deleteOnExit();

            // Удаление временного файла после завершения
            tempFile.deleteOnExit();

        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }

    /**
     * Вспомогательный метод для вывода всех задач из менеджера
     *
     * @param manager - менеджер задач, данные которого нужно вывести
     */
    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("-".repeat(115));
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getAllSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("-".repeat(115));
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
        System.out.println("-".repeat(115));
    }
}
