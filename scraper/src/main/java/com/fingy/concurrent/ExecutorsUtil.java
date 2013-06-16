package com.fingy.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public final class ExecutorsUtil {

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
}
