package io.innospots.base.utils.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Smars
 * @date 2023/10/24
 */
public class ThreadTaskExecutor extends ThreadPoolExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ThreadTaskExecutor.class);

    private long awaitTerminationMillis = 0L;

    private boolean waitForTasksToCompleteOnShutdown = false;

    private String poolName;

    public ThreadTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ThreadTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ThreadTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ThreadTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    public void setAwaitTerminationMillis(long awaitTerminationMillis) {
        this.awaitTerminationMillis = awaitTerminationMillis;
    }

    public void setWaitForTasksToCompleteOnShutdown(boolean waitForTasksToCompleteOnShutdown) {
        this.waitForTasksToCompleteOnShutdown = waitForTasksToCompleteOnShutdown;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public void shutdown() {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Shutting down ExecutorService" + (this.poolName != null ? " '" + this.poolName + "'" : ""));
        }

        if (this.waitForTasksToCompleteOnShutdown) {
            this.shutdown();
        } else {
            List<Runnable> runnableList = this.shutdownNow();

            for (Runnable remainingTask : runnableList) {
                this.cancelRemainingTask(remainingTask);
            }
        }

        this.awaitTerminationIfNecessary(this);


    }

    protected void cancelRemainingTask(Runnable task) {
        if (task instanceof Future) {
            ((Future) task).cancel(true);
        }

    }

    private void awaitTerminationIfNecessary(ExecutorService executor) {
        if (this.awaitTerminationMillis > 0L) {
            try {
                if (!executor.awaitTermination(this.awaitTerminationMillis, TimeUnit.MILLISECONDS) && this.logger.isWarnEnabled()) {
                    this.logger.warn("Timed out while waiting for executor" + (this.poolName != null ? " '" + this.poolName + "'" : "") + " to terminate");
                }
            } catch (InterruptedException e) {
                if (this.logger.isWarnEnabled()) {
                    this.logger.warn("Interrupted while waiting for executor" + (this.poolName != null ? " '" + this.poolName + "'" : "") + " to terminate");
                }
                Thread.currentThread().interrupt();
            }
        }
    }
}
