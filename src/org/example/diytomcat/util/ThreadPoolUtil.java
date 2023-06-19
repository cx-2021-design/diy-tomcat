package org.example.diytomcat.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作者：cjy
 * 类名：ThreadPoolUtil
 * 全路径类名：org.example.diytomcat.util.ThreadPoolUtil
 * 父类或接口：
 * 描述：线程池
 */
public class ThreadPoolUtil {
    //第三个和第四个参数 表示如果 新增加出来的线程如果空闲时间超过 60秒，那么就会被回收，最后保留 20根线程。
    //第五个参数 new LinkedBlockingQueue<Runnable>(10)， 表示当有很多请求短时间过来，使得20根核心线程都满了之后，并不会马上分配新的线程处理更多的请求，
    //而是把这些请求放过在 这个 LinkedBlockingQueue里，
    //当核心线程忙过来了，就会来处理 这个队列里的请求。 只有当处理不过来的请求数目超过 了 10个之后，才会增加更多的线程来处理。
    private static ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 100, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(10));

    public static void run(Runnable r) {
        threadPool.execute(r);
    }

}
