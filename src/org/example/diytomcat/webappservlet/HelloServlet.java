package org.example.diytomcat.webappservlet;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 作者：cjy
 * 类名：HelloServlet
 * 全路径类名：org.example.diytomcat.webappservlet.HelloServlet
 * 父类或接口：@see HttpServlet
 * 描述：hello servlet
 */
public class HelloServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response){
        try {
            response.getWriter().println("Hello DIY Tomcat from HelloServlet");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}