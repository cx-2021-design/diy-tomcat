package org.example.diytomcat.servlets;

import cn.hutool.core.util.ReflectUtil;
import org.example.diytomcat.catalina.Context;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.util.Constant;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 作者：cjy
 * 类名：InvokerServlet
 * 全路径类名：org.example.diytomcat.servlets.InvokerServlet
 * 父类或接口：@see HttpServlet
 * 描述： 处理 Servlet
 */
public class InvokerServlet extends HttpServlet {
    private static InvokerServlet instance = new InvokerServlet();

    public static synchronized InvokerServlet getInstance() {
        return instance;
    }//单例

    private InvokerServlet() {

    }

    /**
     * 方法名：service
     * 传入参数：@param httpServletRequest http servlet请求
     *         @param httpServletResponse http servlet响应
     * 异常类型：@throws IOException ioexception
     *         @throws ServletException servlet异常
     * 描述：服务
     */
    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        String uri = request.getUri();
        Context context = request.getContext();
        String servletClassName = context.getServletClassName(uri);//根据 请求的uri 获取 ServletClassName

        try {
            //实例化 servlet 对象
            Class servletClass = context.getWebappClassLoader().loadClass(servletClassName);//根据类名称，通过 context.getWebappClassLoader().loadClass() 方法去获取类对象
            Object servletObject = context.getServlet(servletClass);//通过 context.getServlet 来获取，这样就保证了单例
            ReflectUtil.invoke(servletObject, "service", request, response);//接着调用其 service 方法,service 方法实会根据 request 的 Method ，访问对应的 doGet 或者 doPost。

            if(null!=response.getRedirectPath())//判断getRedirectPath 是否有值，
                response.setStatus(Constant.CODE_302);//如果有就返回 302 状态码
            else
                response.setStatus(Constant.CODE_200);//表示处理成功了

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}