package org.example.diytomcat.catalina;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.LogFactory;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.servlets.DefaultServlet;
import org.example.diytomcat.servlets.InvokerServlet;
import org.example.diytomcat.servlets.JspServlet;
import org.example.diytomcat.util.Constant;
import org.example.diytomcat.util.SessionManager;
import javax.servlet.Filter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

/**
 * 作者：cjy
 * 类名：HttpProcessor
 * 全路径类名：org.example.diytomcat.catalina.HttpProcessor
 * 父类或接口：
 * 描述：处理请求
 */
public class HttpProcessor {
    /**
     * 方法名：execute
     * 传入参数：@param s 年代
     *         @param request  请求
     *         @param response 响应
     * 描述：执行处理
     */
    public void execute(Socket s, Request request, Response response){
        try {
            String uri = request.getUri();
            if(null==uri)
                return;

            prepareSession(request, response);

            Context context = request.getContext();
            String servletClassName = context.getServletClassName(uri);
            //因为 Servlet 的 service 方法是在 chain 里面调用的,并且用一个 workingServlet 分别指向它们。
            HttpServlet workingServlet;
            if(null!=servletClassName)
                workingServlet = InvokerServlet.getInstance();//交给InvokderServlet 去处理了
            else if(uri.endsWith(".jsp"))//当 uri 以 jsp 结束的时候
                workingServlet = JspServlet.getInstance();// 使用 JspServlet 来处理
            else
                workingServlet = DefaultServlet.getInstance();//处理静态资源
            //接着通过 getMatchedFilters 获取对应的 Filters 集合
            List<Filter> filters = request.getContext().getMatchedFilters(request.getRequestURI());
            //再通过 filterChain 去调用它们
            ApplicationFilterChain filterChain = new ApplicationFilterChain(filters, workingServlet);
            filterChain.doFilter(request, response);

            if(request.isForwarded())
                return;

            if(Constant.CODE_200 == response.getStatus()){
                handle200(s, request, response);
                return;
            }

            //根据返回状态码是否为302，来访问 handle302
            if(Constant.CODE_302 == response.getStatus()){
                handle302(s, response);
                return;
            }
            if(Constant.CODE_404 == response.getStatus()){
                handle404(s, uri);
                return;
            }

        } catch (Exception e) {
            LogFactory.get().error(e);//在捕捉异常这里，打印日志
            handle500(s,e);//并且调用 handle500 来进行处理。
        }
        finally{
            try {
                if(!s.isClosed())
                    s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void handle200(Socket s, Request request, Response response)
            throws IOException {
        OutputStream os = s.getOutputStream();

        String contentType = response.getContentType();

        byte[] body = response.getBody();
        String cookiesHeader = response.getCookiesHeader();//获取 cookiesHeader，并在 headText 中使用它

        boolean gzip = isGzip(request, body, contentType);

        String headText;
        if (gzip)//如果要进行gzip 压缩
            headText = Constant.response_head_200_gzip;//那么使用 gzip 头
        else
            headText = Constant.response_head_200;

        headText = StrUtil.format(headText, contentType, cookiesHeader);

        if (gzip)//如果要进行gzip 压缩
            body = ZipUtil.gzip(body);//并且把 body 用 ZipUtil进行 gzip 压缩。

        byte[] head = headText.getBytes();
        byte[] responseBytes = new byte[head.length + body.length];
        ArrayUtil.copy(head, 0, responseBytes, 0, head.length);
        ArrayUtil.copy(body, 0, responseBytes, head.length, body.length);

        os.write(responseBytes,0,responseBytes.length);
        os.flush();
        os.close();

    }

    /**
     * 方法名：handle404
     * 传入参数：@param s 年代
     *         @param uri uri
     * 异常类型：@throws IOException ioexception
     * 返回类型：
     * 描述：handle 404 ，
     */
    private void handle404(Socket s, String uri) throws IOException {
        OutputStream os = s.getOutputStream();

        //根据 uri， Constant.textFormat_404 和 Constant.response_head_404 组成 404 返回响应。
        String responseText = StrUtil.format(Constant.textFormat_404, uri, uri);
        responseText = Constant.response_head_404 + responseText;

        byte[] responseByte = responseText.getBytes("utf-8");
        os.write(responseByte);
    }

    /**
     * 方法名：handle302
     * 传入参数：@param s 套接字
     *         @param response 响应
     * 异常类型：@throws IOException ioexception
     * 描述：用于处理302跳转
     */
    private void handle302(Socket s, Response response) throws IOException {
        OutputStream os = s.getOutputStream();
        String redirectPath = response.getRedirectPath();
        String head_text = Constant.response_head_302;
        String header = StrUtil.format(head_text, redirectPath);
        byte[] responseBytes = header.getBytes("utf-8");
        os.write(responseBytes);
    }

    /**
     * 方法名：handle500
     * 传入参数：@param s 套接字
     *         @param e 异常
     * 描述：
     */
    private void handle500(Socket s, Exception e) {
        try {
            OutputStream os = s.getOutputStream();

            //e.getStackTrace(); 拿到 Exception 的异常堆栈，
            //比如平时我们看到一个报错，都会打印最哪个类的哪个方法，依次调用过来的信息。
            //这个信息就放在这个 StackTrace里，是个 StackTraceElement 数组。
            StackTraceElement stes[] = e.getStackTrace();
            StringBuffer sb = new StringBuffer();//准备个 StringBuffer()
            sb.append(e.toString());//把 e.toString() 信息放进去
            sb.append("\r\n");
            for (StackTraceElement ste : stes) {//挨个把这些堆栈信息放进去
                sb.append("\t");
                sb.append(ste.toString());
                sb.append("\r\n");
            }

            String msg = e.getMessage();

            if (null != msg && msg.length() > 20)
                msg = msg.substring(0, 19);//有时候消息太长，超过20个，截短一点方便显示

            //结合头和文本模板，组成http 响应，发出去
            String text = StrUtil.format(Constant.textFormat_500, msg, e.toString(), sb.toString());
            text = Constant.response_head_500 + text;
            byte[] responseBytes = text.getBytes("utf-8");
            os.write(responseBytes);

        } catch (IOException e1) {//在前面的判断uri的地方，如果发现是访问的500.html ，故意抛出一个异常
            e1.printStackTrace();
        }
    }

    /**
     * 方法名：prepareSession
     * 传入参数：@param request 请求
     *         @param response 响应
     * 描述：准备session
     */
    public void prepareSession(Request request, Response response) {
        String jsessionid = request.getJSessionIdFromCookie();//先通过 cookie拿到 jsessionid
        HttpSession session = SessionManager.getSession(jsessionid, request, response);//然后通过 SessionManager 创建 session
        request.setSession(session);// 并且设置在 requeset 上
    }

    /**
     * 方法名：isGzip
     * 传入参数：@param request 请求
     *         @param body 响应体
     *         @param mimeType mime类型
     * 返回类型：@return boolean
     * 描述：判断是否要进行gzip
     */
    private boolean isGzip(Request request, byte[] body, String mimeType) {
        String acceptEncodings=  request.getHeader("Accept-Encoding");
        if(!StrUtil.containsAny(acceptEncodings, "gzip"))
            return false;

        Connector connector = request.getConnector();

        if (mimeType.contains(";"))
            mimeType = StrUtil.subBefore(mimeType, ";", false);

        if (!"on".equals(connector.getCompression()))
            return false;

        if (body.length < connector.getCompressionMinSize())
            return false;

        String userAgents = connector.getNoCompressionUserAgents();
        String[] eachUserAgents = userAgents.split(",");
        for (String eachUserAgent : eachUserAgents) {
            eachUserAgent = eachUserAgent.trim();
            String userAgent = request.getHeader("User-Agent");
            if (StrUtil.containsAny(userAgent, eachUserAgent))
                return false;
        }

        String mimeTypes = connector.getCompressableMimeType();
        String[] eachMimeTypes = mimeTypes.split(",");
        for (String eachMimeType : eachMimeTypes) {
            if (mimeType.equals(eachMimeType))
                return true;
        }

        return false;
    }
}