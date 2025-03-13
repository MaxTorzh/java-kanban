public class Main {

    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        Task task1 = new Task("Переезд", "Переехать в новый дом", manager.generateID(), Status.NEW);
        manager.addTask(task1);
        Task task2 = new Task("Поход в магазин", "Купить всю необходимую еду", manager.generateID(), Status.NEW);
        manager.addTask(task2);
        Task task3 = new Task("Ремонт", "Починить санузел", manager.generateID(), Status.NEW);
        manager.addTask(task3);
        Task task4 = new Task("Отдых", "Играть в PS5 весь день", manager.generateID(), Status.NEW);
        manager.addTask(task4);
        System.out.println(manager.getAllTasks());
        System.out.println(" ");
        System.out.println("-".repeat(150));
        System.out.println(" ");
        Epic epic1 = new Epic("Организация праздника", "Организация др", manager.generateID(), Status.NEW);
        manager.addEpic(epic1);
        Subtask subtask1 = new Subtask("Пригласить друзей", "Позвонить всем друзьям", manager.generateID(),Status.NEW, epic1.getId());
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Пригласить аниматоров", "Позвонить аниматорам", manager.generateID(),Status.NEW, epic1.getId());
        manager.addSubtask(subtask2);
        Subtask subtask3 = new Subtask("Накормить всех гостей", "Приготовить еду на 15 человек", manager.generateID(),Status.NEW, epic1.getId());
        manager.addSubtask(subtask3);
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
    }
}
