package org.example.diytomcat.http;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 作者：cjy
 * 类名：StandardServletConfig
 * 全路径类名：org.example.diytomcat.http.StandardServletConfig
 * 父类或接口：@see ServletConfig
 * 描述：标准servlet配置
 */
public class StandardServletConfig implements ServletConfig {
    /**
     * 描述：servlet上下文
     */
    private ServletContext servletContext;
    /**
     * 描述：初始化参数
     */
    private Map<String, String> initParameters;
    /**
     * 描述：servlet名称
     */
    private String servletName;

    public StandardServletConfig(ServletContext servletContext, String servletName,
                                 Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.servletName = servletName;
        this.initParameters = initParameters;
        if (null == this.initParameters)
            this.initParameters = new HashMap<>();
    }

    @Override
    public String getInitParameter(String name) {
        // TODO Auto-generated method stub
        return initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(initParameters.keySet());
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

}
