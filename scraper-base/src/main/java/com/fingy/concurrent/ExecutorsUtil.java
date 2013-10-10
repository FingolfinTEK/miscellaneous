package com.fingy.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ExecutorsUtil {

    private static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();
    private static final int DEFAULT_WORK_QUEUE_CAPACITY = 5000;

    private ExecutorsUtil() {
    }

    public static void shutDownExecutorServiceAndAwaitTermination(ExecutorService executorService, long timeout, TimeUnit timeUnit) {
        try {
            executorService.shutdown();
            executorService.awaitTermination(timeout, timeUnit);
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    public static ThreadPoolExecutor createThreadPool(int processorMultiplier) {
        return creteThreadPool(processorMultiplier, DEFAULT_WORK_QUEUE_CAPACITY);
    }

    public static ThreadPoolExecutor creteThreadPool(int processorMultiplier, int workQueueCapacity) {
        final int corePoolSize = AVAILABLE_PROCESSORS * processorMultiplier;
        final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, corePoolSize * 2, 1, TimeUnit.DAYS, new LinkedBlockingQueue<Runnable>(workQueueCapacity));
        threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPoolExecutor;
    }
}
