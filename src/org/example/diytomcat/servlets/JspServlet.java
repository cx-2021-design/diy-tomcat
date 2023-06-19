package org.example.diytomcat.servlets;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import org.example.diytomcat.catalina.Context;
import org.example.diytomcat.classloader.JspClassLoader;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.util.Constant;
import org.example.diytomcat.util.JspUtil;
import org.example.diytomcat.util.WebXMLUtil;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * 作者：cjy
 * 类名：JspServlet
 * 全路径类名：org.example.diytomcat.servlets.JspServlet
 * 父类或接口：@see HttpServlet
 * 描述：专门处理 jsp 文件的 Servlet;
 *      JspServlet 的基本处理逻辑是先把 jsp 转换为 java 文件，然后编译成 class 文件，再加载之后运行。
 */
public class JspServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static JspServlet instance = new JspServlet();

    public static synchronized JspServlet getInstance() {
        return instance;
    }

    private JspServlet() {

    }

    public void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        try {
            Request request = (Request) httpServletRequest;
            Response response = (Response) httpServletResponse;

            String uri = request.getRequestURI();

            if ("/".equals(uri))
                uri = WebXMLUtil.getWelcomeFile(request.getContext());

            String fileName = StrUtil.removePrefix(uri, "/");
            File file = FileUtil.file(request.getRealPath(fileName));

            File jspFile = file;
            if (jspFile.exists()) {
                Context context = request.getContext();
                String path = context.getPath();

                //subFolder 这个变量是用于处理 ROOT的
                //对于ROOT 而言， 它的 path 是 "/",
                //那么在 work 目录下，对应的应用目录就是 "_"。
                String subFolder;
                if ("/".equals(path))
                    subFolder = "_";
                else
                    subFolder = StrUtil.subAfter(path, '/', false);
                //通过 JspUtil 获取 servlet 路径，看看是否存在。
                String servletClassPath = JspUtil.getServletClassPath(uri, subFolder);
                File jspServletClassFile = new File(servletClassPath);
                if (!jspServletClassFile.exists()) {
                    JspUtil.compileJsp(context, jspFile);
                } else if (jspFile.lastModified() > jspServletClassFile.lastModified()) {//如果存在，再看看最后修改时间与 jsp 文件的最后修改时间 谁早谁晚
                    JspUtil.compileJsp(context, jspFile);//Jsp 转移和编译为java
                    JspClassLoader.invalidJspClassLoader(uri, context);//当发现 jsp 更新之后,就会与之前的 JspClassLoader 脱钩。
                }

                String extName = FileUtil.extName(file);
                String mimeType = WebXMLUtil.getMimeType(extName);
                response.setContentType(mimeType);

                JspClassLoader jspClassLoader = JspClassLoader.getJspClassLoader(uri, context);//根据uri 和 context 获取当前jsp 对应的 JspClassLoader
                String jspServletClassName = JspUtil.getJspServletClassName(uri, subFolder);//获取 jsp 对应的 servlet Class Name
                Class jspServletClass = jspClassLoader.loadClass(jspServletClassName);//通过 JspClassLoader 根据 servlet Class Name 加载类对象：jspServletClass

                //使用 context 现成的用于进行单例管理的 getServlet 方法获取 servlet 实例，然后调用其 service 方法。
                HttpServlet servlet = context.getServlet(jspServletClass);
                servlet.service(request,response);
                if(null!=response.getRedirectPath())//判断getRedirectPath 是否有值
                    response.setStatus(Constant.CODE_302);//如果有就返回 302 状态码
                else
                    response.setStatus(Constant.CODE_200);//设置 状态码为200.
            } else {
                response.setStatus(Constant.CODE_404);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}