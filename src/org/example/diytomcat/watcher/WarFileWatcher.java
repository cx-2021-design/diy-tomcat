package org.example.diytomcat.watcher;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.watch.WatchMonitor;
import cn.hutool.core.io.watch.WatchUtil;
import cn.hutool.core.io.watch.Watcher;
import org.example.diytomcat.catalina.Host;
import org.example.diytomcat.util.Constant;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

/**
 * 作者：cjy
 * 类名：WarFileWatcher
 * 全路径类名：org.example.diytomcat.watcher.WarFileWatcher
 * 父类或接口：
 * 描述：war 动态部署（tomcat已经处于运行状态了，此时向 webapps 目录下扔一个 war 文件，就会自动解压并部署对应的 Context。）
 *     监控 webapps 目录， 当发现新创建了 war 文件的时候，就调用 host 的 loadWar方法（把 war 文件解压为目录，并把文件夹加载为 Context）即可
 */
public class WarFileWatcher {
    /**
     * 描述：监控
     */
    private WatchMonitor monitor;

    /**
     * 方法名：WarFileWatcher
     * 传入参数：@param host 主机
     * 异常类型：
     * 返回类型：@return
     * 描述：war文件监控
     */
    public WarFileWatcher(Host host) {
        this.monitor = WatchUtil.createAll(Constant.webappsFolder, 1, new Watcher() {
            private void dealWith(WatchEvent<?> event, Path currentPath) {
                synchronized (WarFileWatcher.class) {
                    String fileName = event.context().toString();
                    if(fileName.toLowerCase().endsWith(".war")  && ENTRY_CREATE.equals(event.kind())) {
                        File warFile = FileUtil.file(Constant.webappsFolder, fileName);
                        host.loadWar(warFile);
                    }
                }
            }

            /**
             * 方法名：onCreate
             * 传入参数：@param event 事件
             *         @param currentPath 当前路径
             * 描述：在创建
             */
            @Override
            public void onCreate(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

            /**
             * 方法名：onModify
             * 传入参数：@param event 事件
             *         @param currentPath 当前路径
             * 描述：在修改
             */
            @Override
            public void onModify(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);

            }

            /**
             * 方法名：onDelete
             * 传入参数：@param event 事件
             *         @param currentPath 当前路径
             * 描述：在删除
             */
            @Override
            public void onDelete(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }
            @Override
            public void onOverflow(WatchEvent<?> event, Path currentPath) {
                dealWith(event, currentPath);
            }

        });
    }

    /**
     * 方法名：start
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：开始
     */
    public void start() {
        monitor.start();
    }

    /**
     * 方法名：stop
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：停止
     */
    public void stop() {
        monitor.interrupt();
    }

}
