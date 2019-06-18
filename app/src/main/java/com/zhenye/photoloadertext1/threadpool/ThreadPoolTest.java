package com.zhenye.photoloadertext1.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolTest {
    //默認模式的線程池
    public static ThreadPoolExecutor getThreadPoolExecutor() {
        return new ThreadPoolExecutor(3, 5, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(25));
    }

    //只有核心線程的線程池
    public static ExecutorService getFixedThreadPool(int nThread) {
        return new ThreadPoolExecutor(nThread, nThread, 0L, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());
    }

    //只有非核心線程的線程池
    public static ExecutorService getCacheThreadPool() {
        return new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
    }

    //只有一个核心线程的线程池
    public static ExecutorService getSingleThreadExecutor() {
        return new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>());
    }

    /**
     * 定时执行的线程池,延时执行，循环执行
     * scheduledExecutorService.scheduleAtFixedRate(runnable,1,12, TimeUnit.SECONDS);//延时1S后启动，每12s执行一次
     *
     * @param corePoolSize 开启线程数。
     * @return
     */
    public static ScheduledExecutorService getSchedulePool(int corePoolSize) {
        return new ScheduledThreadPoolExecutor(corePoolSize);
    }

    public interface DataPass {
        abstract void dataPass(String s);
    }

    public DataPass mdataPass;

    public void setDataPass(DataPass dataPass) {
        this.mdataPass = dataPass;
    }

    public void dodataPass(String s) {
        mdataPass.dataPass(s);
    }
}
