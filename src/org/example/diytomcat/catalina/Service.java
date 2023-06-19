package org.example.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.log.LogFactory;
import org.example.diytomcat.util.ServerXMLUtil;
import java.util.List;

/**
 * 作者：cjy
 * 类名：Service
 * 全路径类名：org.example.diytomcat.catalina.Service
 * 父类或接口：
 * 描述：
 * 作者：cjy
 * 类名：Service
 * 全路径类名：org.example.diytomcat.catalina.Service
 * 父类或接口：
 * 描述：服务
 */
public class Service {
    /**
     * 描述：名字
     */
    private String name;
    /**
     * 描述：引擎
     */
    private Engine engine;
    /**
     * 描述：服务器
     */
    private Server server;

    /**
     * 描述：连接器
     */
    private List<Connector> connectors;

    /**
     * 方法名：Service
     * 传入参数：@param server 服务器
     * 异常类型：
     * 返回类型：@return
     * 描述：服务
     */
    public Service(Server server){
        this.server = server;
        this.name = ServerXMLUtil.getServiceName();
        this.engine = new Engine(this);
        this.connectors = ServerXMLUtil.getConnectors(this);
    }

    /**
     * 方法名：getEngine
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link Engine }
     * 描述：得到引擎
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * 方法名：getServer
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link Server }
     * 描述：得到服务器
     */
    public Server getServer() {
        return server;
    }

    /**
     * 方法名：start
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：开始
     */
    public void start() {
        init();
    }

    /**
     * 方法名：init
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：初始化
     */
    private void init() {
        TimeInterval timeInterval = DateUtil.timer();
        for (Connector c : connectors)
            c.init();
        LogFactory.get().info("Initialization processed in {} ms",timeInterval.intervalMs());
        for (Connector c : connectors)
            c.start();
    }
}