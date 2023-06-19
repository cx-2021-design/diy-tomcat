package org.example.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import cn.hutool.system.SystemUtil;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


/**
 * 作者：cjy
 * 类名：Server
 * 全路径类名：org.example.diytomcat.catalina.Server
 * 父类或接口：
 * 描述：服务器
 */
public class Server {
    /**
     * 描述：服务
     */
    private Service service;

    /**
     * 方法名：Server
     * 传入参数：
     * 异常类型：
     * 返回类型：@return
     * 描述：服务器
     */
    public Server(){
        this.service = new Service(this);
    }

    /**
     * 方法名：start
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：开始
     */
    public void start(){
        TimeInterval timeInterval = DateUtil.timer();
        logJVM();
        init();
        LogFactory.get().info("Server startup in {} ms",timeInterval.intervalMs());
    }

    /**
     * 方法名：init
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：初始化
     */
    private void init() {
        service.start();
    }

    /**
     * 方法名：logJVM
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：日志jvm
     */
    private static void logJVM() {
        Map<String,String> infos = new LinkedHashMap<>();
        infos.put("Server version", "How2J DiyTomcat/1.0.1");
        infos.put("Server built", "2020-04-08 10:20:22");
        infos.put("Server number", "1.0.1");
        infos.put("OS Name\t", SystemUtil.get("os.name"));
        infos.put("OS Version", SystemUtil.get("os.version"));
        infos.put("Architecture", SystemUtil.get("os.arch"));
        infos.put("Java Home", SystemUtil.get("java.home"));
        infos.put("JVM Version", SystemUtil.get("java.runtime.version"));
        infos.put("JVM Vendor", SystemUtil.get("java.vm.specification.vendor"));

        Set<String> keys = infos.keySet();
        for (String key : keys) {
            LogFactory.get().info(key+":\t\t" + infos.get(key));//获取日志对象
        }
    }

}