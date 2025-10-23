package ru.yandex.practicum.poller;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public abstract class AbstractPoller<T> implements Runnable {

    protected volatile boolean databaseIsModified = true;
    protected volatile boolean killMeRightNow = false;

    public void touch() {
        log.info("[{}] got touch. Set databaseIsModified = true", getClass().getSimpleName());
        databaseIsModified = true;
    }

    public void shutdown() {
        killMeRightNow = true;
    }

    @Override
    public final void run() {
        log.info("[{}] Poller started", getClass().getSimpleName());

        while (!killMeRightNow) {
            if (!databaseIsModified) {
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    break;
                }
            }

            Optional<T> optionalEntity = getEntity();
            if (optionalEntity.isEmpty()) {
                log.info("[{}] got empty DB result. Set databaseIsModified = false", getClass().getSimpleName());
                databaseIsModified = false;
                continue;
            }
            T entity = optionalEntity.get();
            log.info("[{}] Processing {} # {}", getClass().getSimpleName(), name(), id(entity));

            try {
                handleNormally(entity);
                log.info("[{}] Saved {} # {}", getClass().getSimpleName(), name(), id(entity));
            } catch (FeignException e) {
                if (exceedTimeoutCondition(entity)) {
                    handleIfTimeout(entity);
                    log.info("[{}] Timeout is reached processing {} # {}", getClass().getSimpleName(), name(), id(entity));
                } else {
                    try {
                        log.info("[{}] Caught FeignException (I'll be back in 5 sec) : {}", getClass().getSimpleName(), e.getMessage());
                        handleForRetry(entity);
                        Thread.sleep(5000);
                    } catch (InterruptedException ex) {
                        break;
                    }
                }
            }
        }

        log.info("[{}] Poller finished", getClass().getSimpleName());
    }

    protected abstract Optional<T> getEntity();

    protected abstract void handleNormally(T entity);

    protected abstract void handleForRetry(T entity);

    protected abstract void handleIfTimeout(T entity);

    protected abstract boolean exceedTimeoutCondition(T entity);

    protected abstract String id(T entity);

    protected abstract String name();

}
