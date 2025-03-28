import java.util.List;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        tm.addTask(task1);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        tm.addTask(task2);

        Epic epic1 = new Epic("Epic 1", "Description 1");
        tm.addEpic(epic1);
        tm.updateEpicStatus(epic1.getId());
        Subtask sub1 = new Subtask("Subtask 1", "Description 1", Status.NEW,  epic1.getId());
        tm.addSubtask(sub1);
        Subtask sub2 = new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, epic1.getId());
        tm.addSubtask(sub2);

        Epic epic2 = new Epic("Epic 2", "Description 2");
        tm.addEpic(epic2);
        tm.updateEpicStatus(epic2.getId());
        Subtask sub3 = new Subtask("Subtask 3", "Description 3", Status.IN_PROGRESS, epic2.getId());
        tm.addSubtask(sub3);

        task1.setStatus(Status.IN_PROGRESS);
        tm.updateTask(task1);
        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.DONE);
        tm.updateSubtask(sub1);
        tm.updateSubtask(sub2);
        sub3.setStatus(Status.IN_PROGRESS);
        tm.updateSubtask(sub3);

        tm.getTaskById(task1.getId());
        tm.getTaskById(task2.getId());
        tm.getEpicById(epic1.getId());
        tm.getEpicById(epic2.getId());
        tm.getSubtaskById(sub1.getId());
        tm.getSubtaskById(sub2.getId());
        tm.getSubtaskById(sub3.getId());
        tm.getTaskById(task1.getId());
        tm.getTaskById(task1.getId());
        tm.getTaskById(task1.getId());
        tm.getTaskById(task1.getId());

        printAllTasks(tm);
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : manager.getAllSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
