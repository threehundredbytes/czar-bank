package ru.dreadblade.czarbank.service.task;

public interface Task {
    /**
     * @return {@code true} if task is completed successfully, otherwise - {@code false}
     */
    boolean execute();
}
