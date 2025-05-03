package taskmanager.core;

import taskmanager.core.managers.*;
import taskmanager.core.model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static taskmanager.core.util.Status.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Создание временного файла для тестирования
            File tempFile = File.createTempFile("temp-task", ".csv");
            System.out.println("Временный файл создан: " + tempFile.getAbsolutePath());
            // Запись тестовых данных в файл
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
                writer.write("id,type,name,status,description,epic\n");
                writer.write("1,TASK,Task1,NEW,Description1,\n");
                writer.write("2,TASK,Task2,IN_PROGRESS,Description2,\n");
                writer.write("3,EPIC,Epic1,NEW,Description1,\n");
                writer.write("4,SUBTASK,Subtask1,NEW,Description1,3\n");
                writer.write("5,SUBTASK,Subtask2,DONE,Description2,3\n");
            } catch (IOException e) {
                System.out.println("Ошибка при записи во временный файл: " + e.getMessage());
                return; //  Прерываем выполнение программы, если не удалось записать данные
            }
            // Создание первого менеджера и добавление задач
            FileBackedTaskManager fm1 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            // Вывод состояния первого менеджера
            System.out.println("Состояние первого менеджера:");
            printAllTasks(fm1);

            // Создание второго менеджера из того же файла
            FileBackedTaskManager fm2 = FileBackedTaskManager.loadFromFile(tempFile);

            // Вывод состояния второго менеджера
            System.out.println("Состояние второго менеджера после загрузки из файла:");
            printAllTasks(fm2);

            // Удаление временного файла после завершения
            tempFile.deleteOnExit();
        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }

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
    }
}
