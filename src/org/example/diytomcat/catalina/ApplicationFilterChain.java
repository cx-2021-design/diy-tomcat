package org.example.diytomcat.catalina;

import java.io.IOException;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import cn.hutool.core.util.ArrayUtil;

/**
 * 作者：cjy
 * 类名：ApplicationFilterChain
 * 全路径类名：org.example.diytomcat.catalina.ApplicationFilterChain
 * 父类或接口：@see FilterChain
 * 描述：过滤器责任链对象
 */
public class ApplicationFilterChain implements FilterChain{

    /**
     * 描述：过滤器数组
     */
    private Filter[] filters;
    /**
     * 描述：执行业务的servlet
     */
    private Servlet servlet;
    /**
     * 描述：当前正在使用的过滤器
     */
    int pos;

    /**
     * 方法名：ApplicationFilterChain
     * 传入参数：@param filterList 过滤器列表
     *         @param servlet servlet
     * 返回类型：@return
     * 描述：初始化的时候带上过滤器集合和servlet
     */
    public ApplicationFilterChain(List<Filter> filterList,Servlet servlet){
        this.filters = ArrayUtil.toArray(filterList,Filter.class);
        this.servlet = servlet;
    }

    /**
     * 方法名：doFilter
     * 传入参数：@param request 请求
     *         @param response 响应
     * 异常类型：@throws IOException ioexception
     *        @throws ServletException servlet异常
     * 返回类型：
     * 描述：挨个执行所有的过滤器，执行结束之后，执行 servlet
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if(pos < filters.length) {
            Filter filter= filters[pos++];
            filter.doFilter(request, response, this);
        }
        else {
            servlet.service(request, response);
        }
    }

}
