package org.example.diytomcat;

import org.example.diytomcat.classloader.CommonClassLoader;
import java.lang.reflect.Method;


/**
 * 作者：cjy
 * 类名：Bootstrap
 * 全路径类名：org.example.diytomcat.Bootstrap
 * 父类或接口：
 * 描述：引导
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {

        CommonClassLoader commonClassLoader = new CommonClassLoader();

        Thread.currentThread().setContextClassLoader(commonClassLoader);

        String serverClassName = "org.example.diytomcat.catalina.Server";

        //通过 CommonClassLoader 加载 Server类
        Class<?> serverClazz = commonClassLoader.loadClass(serverClassName);
        //然后实例化
        Object serverObject = serverClazz.newInstance();
        //然后再通过反射调用其 start 方法
        Method m = serverClazz.getMethod("start");

        m.invoke(serverObject);
        System.out.println(serverClazz.getClassLoader());

        // 不能关闭，否则后续就不能使用啦
        // commonClassLoader.close();

    }
}