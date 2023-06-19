package org.example.diytomcat.http;

import org.example.diytomcat.catalina.Context;
import java.io.File;
import java.util.*;

/**
 * 作者：cjy
 * 类名：ApplicationContext
 * 全路径类名：org.example.diytomcat.http.ApplicationContext
 * 父类或接口：@see BaseServletContext
 * 描述：应用程序上下文
 */
public class ApplicationContext extends BaseServletContext {

    /**
     * 描述：存储属性
     */
    private Map<String, Object> attributesMap;
    /**
     * 描述：上下文
     */
    private Context context;

    public ApplicationContext(Context context) {
        this.attributesMap = new HashMap<>();
        this.context = context;
    }


    //这部分在 jsp 里的用法就是那个 <% application.setAttribute()%> ，那个 application 内置对象就是这个 ApplicationContext
    public void setAttribute(String name, Object value) {
        attributesMap.put(name, value);
    }

    public void removeAttribute(String name) {
        attributesMap.remove(name);
    }

    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    //来获取硬盘上的真实路径
    public String getRealPath(String path) {
        return new File(context.getDocBase(), path).getAbsolutePath();
    }
}
