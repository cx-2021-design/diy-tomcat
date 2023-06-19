package org.example.diytomcat.catalina;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * 作者：cjy
 * 类名：StandardFilterConfig
 * 全路径类名：org.example.diytomcat.catalina.StandardFilterConfig
 * 父类或接口：@see FilterConfig
 * 描述：存放 Filter 的初始化参数
 */
public class StandardFilterConfig implements FilterConfig {
    /**
     * 描述：servlet上下文
     */
    private ServletContext servletContext;
    /**
     * 描述：初始化参数
     */
    private Map<String, String> initParameters;
    /**
     * 描述：过滤器名字
     */
    private String filterName;

    public StandardFilterConfig(ServletContext servletContext, String filterName,
                                Map<String, String> initParameters) {
        this.servletContext = servletContext;
        this.filterName = filterName;
        this.initParameters = initParameters;
        if (null == this.initParameters)
            this.initParameters = new HashMap<>();
    }

    @Override
    public String getInitParameter(String name) {
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
    public String getFilterName() {
        return filterName;
    }

}
