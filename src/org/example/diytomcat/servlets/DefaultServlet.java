package org.example.diytomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.StrUtil;
import org.example.diytomcat.catalina.Context;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.util.Constant;
import org.example.diytomcat.util.WebXMLUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 作者：cjy
 * 类名：DefaultServlet
 * 全路径类名：org.example.diytomcat.servlets.DefaultServlet
 * 父类或接口：@see HttpServlet
 * 描述：专门处理静态资源的 DefaultServlet
 */
public class DefaultServlet extends HttpServlet {
    private static DefaultServlet instance = new DefaultServlet();

    public static synchronized DefaultServlet getInstance() {
        return instance;
    }

    private DefaultServlet() {

    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        Request request = (Request) httpServletRequest;
        Response response = (Response) httpServletResponse;

        Context context = request.getContext();

        String uri = request.getUri();
        if ("/500.html".equals(uri))
            throw new RuntimeException("this is a deliberately created exception");

        if ("/".equals(uri))//当 uri 等于 "/" 的时候， uri 就修改成欢迎文件，后面就当作普通文件来处理了
            uri = WebXMLUtil.getWelcomeFile(request.getContext());

        if(uri.endsWith(".jsp")){//当访问 "/" 地址的时候，发现welcome 文件是 jsp 文件
            JspServlet.getInstance().service(request,response);//那么就会交由 JspServlet来处理
            return;
        }

        String fileName = StrUtil.removePrefix(uri, "/");
        File file = FileUtil.file(request.getRealPath(fileName));

        if (file.exists()) {
            String extName = FileUtil.extName(file);
            String mimeType = WebXMLUtil.getMimeType(extName);
            response.setContentType(mimeType);

            byte body[] = FileUtil.readBytes(file);
            response.setBody(body);

            if (fileName.equals("timeConsume.html"))
                ThreadUtil.sleep(1000);

            response.setStatus(Constant.CODE_200);
        } else {
            response.setStatus(Constant.CODE_404);
        }

    }

}