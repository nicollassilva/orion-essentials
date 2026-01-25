package dev.thewarrior.Essentials.Utils;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ThrottledTask {
    private final ScheduledExecutorService executorService;

    private ScheduledFuture<?> future;

    private final long delay;

    private final Runnable task;

    public ThrottledTask(final ScheduledExecutorService executorService, final Runnable task, final long delay) {
        this.executorService = executorService;
        this.task = task;
        this.delay = delay;
    }

    public void execute() {
        if(this.executorService.isShutdown()) return;

        if(this.future != null && !this.future.isDone()) return;

        this.future = this.executorService.schedule(this.task, this.delay, TimeUnit.MILLISECONDS);
    }
}

