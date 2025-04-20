package taskmanager.core.managers;

import taskmanager.core.model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private static class Node {
        Task task; // Хранимая задача
        Node prev; // Ссылка на предыдущий узел
        Node next; // Ссылка на следующий узел

        Node(Task task) {
            this.task = task;
        }
    }

    private final Map<Integer, Node> nodeMap = new LinkedHashMap<>(); // Хранит id задачи -> узел
    private Node head; // Первый элемент списка (изначально null)
    private Node tail; // Последний элемент списка (изначально null)

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        remove(task.getId()); // Удаление существующей задачи, если она есть
        Node newNode = new Node(task); // Создание нового узла
        linkLast(newNode);
        nodeMap.put(task.getId(), newNode); // Добавление нового узла в конец списка
    }

    @Override
    public void remove(int id) {
        Node node = nodeMap.get(id); // Поиск узел по id
        if (node != null) {
            removeNode(node); // Удаление узла из списка
            nodeMap.remove(id); // Удаление записи из мапы
        }
    }

    @Override
    public List<Task> getHistory() {
        return getTask(); // Возвращение задач в порядке просмотра
    }

    private List<Task> getTask() {
        List<Task> task = new ArrayList<>(); // Создание списка для задач
        Node current = head; // Задается текущим head
        while (current != null) { // Пока текущий узел не null
            task.add(current.task); // В список задач добавляется текущая задача
            current = current.next; // Переход к следующему узлу
        }
        return task;
    }

    private void linkLast(Node node) { // Добавление узла в конец списка
        if (head == null) { // Если список пуст
            head = node; // новый узел становится и head, и tail
            tail = node;
        } else {
            tail.next = node; // Связка текущего хвоста с узлом
            node.prev = tail;
            tail = node; // Новый узел теперь хвост
        }
    }

    private void removeNode(Node node) {
        if (node.prev != null) { // Обновление ссылки у предыдущего узла
            node.prev.next = node.next; // Пропуск удаленного узла
        } else {
            head = node.next; // Если удален head, сдвиг вправо
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // Если удален tail, сдвиг влево
        }
    }
}
