public class Main {

    public static void main(String[] args) {
        TaskManager tm = new TaskManager();
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        tm.addTask(task1);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        tm.addTask(task2);
        System.out.println(tm.getAllTasks());

        Epic epic1 = new Epic("Epic 1", "Description 1", Status.NEW);
        tm.addEpic(epic1);
        System.out.println(tm.getAllEpics());
        Subtask sub1 = new Subtask("Subtask 1", "Description 1", Status.NEW,  epic1.getId());
        tm.addSubtask(sub1);
        Subtask sub2 = new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, epic1.getId());
        tm.addSubtask(sub2);
        System.out.println(tm.getAllSubtasks());

        Epic epic2 = new Epic("Epic 2", "Description 2", Status.IN_PROGRESS);
        tm.addEpic(epic2);
        System.out.println(tm.getAllEpics());
        Subtask sub3 = new Subtask("Subtask 3", "Description 3", Status.IN_PROGRESS, epic2.getId());
        tm.addSubtask(sub3);
        System.out.println(tm.getAllSubtasks());

        task1.setStatus(Status.IN_PROGRESS);
        tm.updateTask(task1);
        System.out.println(tm.getAllTasks());

        sub1.setStatus(Status.DONE);
        sub2.setStatus(Status.IN_PROGRESS);
        tm.updateSubtask(sub1);
        tm.updateSubtask(sub2);
        sub3.setStatus(Status.DONE);
        tm.updateSubtask(sub3);
        System.out.println(tm.getAllSubtasks());
        System.out.println(tm.getAllEpics());

        tm.deleteTaskById(1);
        tm.deleteSubtaskById(4);
        tm.deleteEpicById(6);

        System.out.println(tm.getAllTasks());
        System.out.println(tm.getAllSubtasks());
        System.out.println(tm.getAllEpics());
    }
}
