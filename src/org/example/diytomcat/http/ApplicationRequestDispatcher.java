package org.example.diytomcat.http;

import org.example.diytomcat.catalina.HttpProcessor;
import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 作者：cjy
 * 类名：ApplicationRequestDispatcher
 * 全路径类名：org.example.diytomcat.http.ApplicationRequestDispatcher
 * 父类或接口：@see RequestDispatcher
 * 描述：应用程序请求调度程序
 */
public class ApplicationRequestDispatcher implements RequestDispatcher {

    /**
     * 描述：uri
     */
    private String uri;
    public ApplicationRequestDispatcher(String uri) {
        if(!uri.startsWith("/"))
            uri = "/" + uri;
        this.uri = uri;
    }

    @Override
    public void forward(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        Request request = (Request) servletRequest;
        Response response = (Response) servletResponse;

        request.setUri(uri);

        HttpProcessor processor = new HttpProcessor();
        processor.execute(request.getSocket(), request,response);
        request.setForwarded(true);

    }

    @Override
    public void include(ServletRequest arg0, ServletResponse arg1) throws ServletException, IOException {
        // TODO Auto-generated method stub

    }

}