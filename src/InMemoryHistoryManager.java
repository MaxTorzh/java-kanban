import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        history.add(task); // Добавление задач в историю
        if (history.size() > 10) { // Если размер превышает 10
            history.remove(0); // Удалить самую первую задачу
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history); // Возврат копии истории
    }
}
