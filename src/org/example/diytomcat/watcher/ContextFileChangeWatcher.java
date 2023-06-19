package org.example.diytomcat.watcher;

import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import cn.hutool.log.LogFactory;
import org.example.diytomcat.catalina.Context;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * 作者：cjy
 * 类名：ContextFileChangeWatcher
 * 全路径类名：org.example.diytomcat.watcher.ContextFileChangeWatcher
 * 父类或接口：
 * 描述：Context 文件改变监听器
 */
public class ContextFileChangeWatcher {

    /**
     * 描述：监听
     */
    private WatchMonitor monitor;

    private boolean stop = false;

    public ContextFileChangeWatcher(Context context) {

        //通过WatchUtil.createAll 创建 监听器。
        //context.getDocBase() 代表监听的文件夹
        //Integer.MAX_VALUE 代表监听的深入，如果是0或者1，就表示只监听当前目录，而不监听子目录
        //new Watcher 当有文件发生变化，那么就会访问 Watcher 对应的方法
        this.monitor = WatchUtil.createAll(context.getDocBase(), Integer.MAX_VALUE, new Watcher() {

            /**
             * 方法名：dealWith
             * 传入参数：@param event 事件
             * 异常类型：
             * 返回类型：
             * 描述：处理
             */
            private void dealWith(WatchEvent<?> event) {
                //首先加上 synchronized 同步。
                //因为这是一个异步处理的，当文件发生变化，会发过来很多次事件。
                //所以我们得一个一个事件的处理，否则搞不好就会让 Context 加载多次。
                synchronized (ContextFileChangeWatcher.class) {
                    String fileName = event.context().toString();//取得当前发生变化的文件或者文件夹名称
                    if (stop)//当 stop 的时候，就表示已经重载过了，后面再来的消息就别搭理了。
                        return;
                    //只应对 jar class 和 xml 发生的变化，其他的不需要重启，比如 html ,txt等，没必要重启
                    if (fileName.endsWith(".jar") || fileName.endsWith(".class") || fileName.endsWith(".xml")) {
                        stop = true;// 标记一下，后续消息就别处理了
                        LogFactory.get().info(ContextFileChangeWatcher.this + " 检测到了Web应用下的重要文件变化 {} " , fileName);
                        context.reload();
                    }

                }
            }

            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event);

            }

            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event);

            }

            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event);
            }

        });

        this.monitor.setDaemon(true);
    }

    public void start() {
        monitor.start();
    }

    public void stop() {
        monitor.close();
    }
}
