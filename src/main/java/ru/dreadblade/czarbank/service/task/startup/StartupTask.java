package ru.dreadblade.czarbank.service.task.startup;

import org.springframework.boot.ApplicationRunner;
import ru.dreadblade.czarbank.service.task.Task;

public interface StartupTask extends ApplicationRunner, Task {
}
