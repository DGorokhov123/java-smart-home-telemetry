package ru.yandex.practicum.poller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PollingStarter {

    private final TaskExecutor executor;
    private final List<? extends AbstractPoller<?>> pollers;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Application ready, starting pollers...");
        for (AbstractPoller<?> poller : pollers) executor.execute(poller);
    }

    @EventListener(ContextClosedEvent.class)
    public void onApplicationShutdown(ContextClosedEvent event) {
        log.info("Application shutdown initiated, stopping pollers...");
        for (AbstractPoller<?> poller : pollers) poller.shutdown();
    }

}
