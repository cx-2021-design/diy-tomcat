package org.example.diytomcat.catalina;

import org.example.diytomcat.util.ServerXMLUtil;
import java.util.List;

/**
 * 作者：cjy
 * 类名：Engine
 * 全路径类名：org.example.diytomcat.catalina.Engine
 * 父类或接口：
 * 描述：引擎,用来处理 servlet 的请求。
 */
public class Engine {
    /**
     * 描述：默认主机
     */
    private String defaultHost;
    /**
     * 描述：主机集合
     */
    private List<Host> hosts;
    /**
     * 描述：服务
     */
    private Service service;

    public Engine(Service service) {
        this.service = service;
        this.defaultHost = ServerXMLUtil.getEngineDefaultHost();//获取defaultHost的值
        this.hosts = ServerXMLUtil.getHosts(this);
        checkDefault();
    }

    public Service getService() {
        return service;
    }

    /**
     * 方法名：checkDefault
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：判断默认的host是否存在，否则就会抛出异常
     */
    private void checkDefault() {
        if(null==getDefaultHost())
            throw new RuntimeException("the defaultHost" + defaultHost + " does not exist!");
    }

    /**
     * 方法名：getDefaultHost
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link Host }
     * 描述：获取默认的Host对象
     */
    public Host getDefaultHost(){
        for (Host host : hosts) {
            if(host.getName().equals(defaultHost))
                return host;
        }
        return null;
    }

}
