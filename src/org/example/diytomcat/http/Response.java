package org.example.diytomcat.http;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 作者：cjy
 * 类名：Response
 * 全路径类名：org.example.diytomcat.http.Response
 * 父类或接口：@see BaseResponse
 * 描述：响应
 */
public class Response extends BaseResponse {
    /**
     * 描述：
     * 描述：存放 html 文本
     */
    private StringWriter stringWriter;
    /**
     * 描述：写操作
     */
    private PrintWriter writer;
    /**
     * 描述：对应响应头信息里的 Content-type ，默认是 "text/html"。
     */
    private String contentType;
    /**
     * 描述：来存放二进制文件。如pdf或图片
     */
    private byte[] body;
    /**
     * 描述：状态
     */
    private int status;
    /**
     * 描述：声明 cookies
     */
    private List<Cookie> cookies;
    /**
     * 描述：客户端跳转路径
     */
    private String redirectPath;

    /**
     * 方法名：Response
     * 传入参数：
     * 异常类型：
     * 返回类型：@return
     * 描述：响应
     */
    public Response(){
        //为了这样使用response.getWriter().println()
        this.stringWriter = new StringWriter();
        this.writer = new PrintWriter(stringWriter);//写进去的数据最后都写到 stringWriter 里面去
        this.contentType = "text/html";
        this.cookies = new ArrayList<>();
    }

    /**
     * 方法名：getRedirectPath
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：得到客户端跳转路径
     */
    public String getRedirectPath() {
        return this.redirectPath;
    }

    /**
     * 方法名：sendRedirect
     * 传入参数：@param redirect 重定向
     * 异常类型：@throws IOException ioexception
     * 返回类型：
     * 描述：保存客户端跳转路径
     */
    public void sendRedirect(String redirect) throws IOException {
        this.redirectPath = redirect;
    }

    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public PrintWriter getWriter() {
        return writer;
    }

    /**
     * 方法名：getBody
     * 传入参数：
     * 异常类型：@throws UnsupportedEncodingException 不支持编码异常
     * 返回类型：@return {@link byte[] }
     * 描述：html 的字节数组。
     */
    public byte[] getBody() throws UnsupportedEncodingException {
        if(null==body) {
            String content = stringWriter.toString();
            body = content.getBytes("utf-8");
        }
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
    @Override
    public void setStatus(int status) {
        this.status = status;
    }
    @Override
    public int getStatus() {
        return status;
    }

    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    public List<Cookie> getCookies() {
        return this.cookies;
    }

    /**
     * 方法名：getCookiesHeader
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：把Cookie集合转换成 cookie Header
     */
    public String getCookiesHeader() {
        if(null==cookies)
            return "";

        String pattern = "EEE, d MMM yyyy HH:mm:ss 'GMT'";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.ENGLISH);

        StringBuffer sb = new StringBuffer();
        for (Cookie cookie : getCookies()) {
            sb.append("\r\n");
            sb.append("Set-Cookie: ");
            sb.append(cookie.getName() + "=" + cookie.getValue() + "; ");
            if (-1 != cookie.getMaxAge()) { //-1 mean forever
                sb.append("Expires=");
                Date now = new Date();
                Date expire = DateUtil.offset(now, DateField.MINUTE, cookie.getMaxAge());
                sb.append(sdf.format(expire));
                sb.append("; ");
            }
            if (null != cookie.getPath()) {
                sb.append("Path=" + cookie.getPath());
            }
        }

        return sb.toString();
    }

}