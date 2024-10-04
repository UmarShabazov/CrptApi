package org.example.service;

import java.util.concurrent.*;

public class RateLimitedExecutor implements Executor {
    private final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RateLimitedExecutor(int requestLimit, TimeUnit timeUnit) {
        this.semaphore = new Semaphore(requestLimit, true);

        // Scheduler resets the semaphore permits after the interval
        scheduler.scheduleAtFixedRate(() -> {
            semaphore.release(requestLimit - semaphore.availablePermits());
        }, 1, 1, timeUnit);

        // Thread for processing the queue
        Thread processingThread = new Thread(() -> {
            while (true) {
                try {
                    Runnable action = queue.take();
                    semaphore.acquire();
                    action.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        processingThread.setDaemon(true);
        processingThread.start();
    }

    @Override
    public void execute(Runnable action) {
        queue.add(action);
    }
}