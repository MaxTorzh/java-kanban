package taskmanager.core.model;

import taskmanager.core.util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {
    private List<Integer> subtaskIds; // id подзадач, относящихся к epic
    private transient LocalDateTime startTime;
    private transient Duration duration;
    private transient LocalDateTime endTime;
    private String endTimeStr;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Epic(String title, String description) {
        super(title, description, Status.NEW, null, null);
        this.subtaskIds = new ArrayList<>(); // Создание списка id подзадач
    }

    public String getEndTimeStr() {
        return endTime != null ? endTime.format(FORMATTER) : null;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
        this.endTime = (endTimeStr != null && !endTimeStr.isEmpty())
                ? LocalDateTime.parse(endTimeStr, FORMATTER)
                : null;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        syncTransientFields();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public void setDuration(Duration duration) {
        this.duration = duration;
        syncTransientFields();
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            this.endTimeStr = endTime.format(FORMATTER);
        }
    }

    /**
     * Проверка на пустоту списка подзадач (если у epic нет подзадач, сброс всех временных параметров в null)
     * Поиск самого раннего startTime
     * @param subtasks
     */
    public void updateTimeParameters(List<Subtask> subtasks) {
        if (subtasks == null || subtasks.isEmpty()) {
            this.setStartTime(null);
            this.setDuration(null);
            this.setEndTime(null);
            return;
        }

        // Определение самого раннего startTime
        LocalDateTime earliestStart = subtasks.stream() // Создание потока из списка подзадач
                .map(Subtask::getStartTime) // Для каждой подзадачи получение ее startTime (преобразование в startTime)
                .filter(Objects::nonNull) // Фильтрация null значений
                .min(LocalDateTime::compareTo) // Поиск самого раннего времени
                .orElse(null); // Если все startTime = null, возвращается null

        // Определение самого позднего endTime
        LocalDateTime latestEnd = subtasks.stream() // Создание потока из списка подзадач
                .map(Subtask::getEndTime) // Для каждой подзадачи вычисление ее endTime (преобразование в endTime)
                .filter(Objects::nonNull) // Фильтрация null значений
                .max(LocalDateTime::compareTo) // Поиск самого позднего времени
                .orElse(null); // Если все endTime = null, возвращается null

        this.setStartTime(earliestStart); // Старт epic = старт самой ранней подзадачи
        this.endTime = latestEnd; // Окончание epic = окончание самой поздней подзадачи
        if (earliestStart != null && latestEnd != null) {
            this.setDuration(Duration.between(earliestStart, latestEnd)); // Длительность = разница времен
        } else {
            this.setDuration(null);
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds; // Возврат оригинала списка id подзадач
    }

    public void setSubtaskIds(List<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtask(int subtaskId) {
        if (subtaskId == this.getId()) return;

        if (subtaskIds == null) {
            subtaskIds = new ArrayList<>();
        }

        if (!subtaskIds.contains(subtaskId)) {
            subtaskIds.add(subtaskId);
        }
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return "Epic{" +
                ", " + super.toString() +
                "subtasksIds=" + subtaskIds + // Вывод списка id подзадач
                '}';
    }

    protected void syncTransientFields() {
        super.syncTransientFields();

        if (endTime != null) {
            this.endTimeStr = endTime.format(FORMATTER);
        } else {
            this.endTimeStr = null;
        }
    }
}