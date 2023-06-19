package org.example.diytomcat.util;

import cn.hutool.system.SystemUtil;
import java.io.File;

/**
 * 作者：cjy
 * 类名：Constant
 * 全路径类名：org.example.diytomcat.util.Constant
 * 父类或接口：
 * 描述：常数
 */
public class Constant {
    /**
     * 描述：代码200
     */
    public static final int CODE_200 = 200;
    /**
     * 描述：代码302
     */
    public static final int CODE_302 = 302;
    /**
     * 描述：代码404
     */
    public static final int CODE_404 = 404;
    /**
     * 描述：代码500
     */
    public static final int CODE_500 = 500;
    /**
     * 描述：响应头302
     */
    public static final String response_head_302 =
            "HTTP/1.1 302 Found\r\nLocation: {}\r\n\r\n";

    /**
     * 描述：响应头200
     */
    public static final String response_head_200 =
            "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: {}{}" +
                    "\r\n\r\n";

    /**
     * 描述：进行了压缩的时候的响应头
     */
    public static final String response_head_200_gzip =
            "HTTP/1.1 200 OK\r\nContent-Type: {}{}\r\n" +
                    "Content-Encoding:gzip" +
                    "\r\n\r\n";

    /**
     * 描述：响应头404
     */
    public static final String response_head_404 =
            "HTTP/1.1 404 Not Found\r\n" +
                    "Content-Type: text/html\r\n\r\n";

    /**
     * 描述：响应头500
     */
    public static final String response_head_500 = "HTTP/1.1 500 Internal Server Error\r\n"
            + "Content-Type: text/html\r\n\r\n";

    /**
     * 描述：文本格式404,返回 404 代码已经 Not Found 字符串
     */
    public static final String textFormat_404 =
            "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>" +
                    "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} " +
                    "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} " +
                    "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} " +
                    "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} " +
                    "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} " +
                    "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}" +
                    "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> " +
                    "</head><body><h1>HTTP Status 404 - {}</h1>" +
                    "<HR size='1' noshade='noshade'><p><b>type</b> Status report</p><p><b>message</b> <u>{}</u></p><p><b>description</b> " +
                    "<u>The requested resource is not available.</u></p><HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>" +
                    "</body></html>";

    /**
     * 描述：文本格式500
     */
    public static final String textFormat_500 = "<html><head><title>DIY Tomcat/1.0.1 - Error report</title><style>"
            + "<!--H1 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:22px;} "
            + "H2 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:16px;} "
            + "H3 {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;font-size:14px;} "
            + "BODY {font-family:Tahoma,Arial,sans-serif;color:black;background-color:white;} "
            + "B {font-family:Tahoma,Arial,sans-serif;color:white;background-color:#525D76;} "
            + "P {font-family:Tahoma,Arial,sans-serif;background:white;color:black;font-size:12px;}"
            + "A {color : black;}A.name {color : black;}HR {color : #525D76;}--></style> "
            + "</head><body><h1>HTTP Status 500 - An exception occurred processing {}</h1>"
            + "<HR size='1' noshade='noshade'><p><b>type</b> Exception report</p><p><b>message</b> <u>An exception occurred processing {}</u></p><p><b>description</b> "
            + "<u>The server encountered an internal error that prevented it from fulfilling this request.</u></p>"
            + "<p>Stacktrace:</p>" + "<pre>{}</pre>" + "<HR size='1' noshade='noshade'><h3>DiyTocmat 1.0.1</h3>"
            + "</body></html>";
    /**
     * 描述：webapps文件夹
     */
    public final static File webappsFolder = new File(SystemUtil.get("user.dir"),"webapps");
    /**
     * 描述：ROOT文件夹
     */
    public final static File rootFolder = new File(webappsFolder,"ROOT");

    /**
     * 描述：conf文件夹
     */
    public static final File confFolder = new File(SystemUtil.get("user.dir"),"conf");

    /**
     * 描述： conf/server.xml
     */
    public static final File serverXmlFile = new File(confFolder, "server.xml");

    /**
     * 描述：conf/web.xml 。
     */
    public static final File webXmlFile = new File(confFolder, "web.xml");
    /**
     * 描述：conf/context.xml
     */
    public static final File contextXmlFile = new File(confFolder, "context.xml");
    /**
     * 描述：work文件夹，当一个 jsp 被转译成为 .java 文件之后，会被保存在 %TOMCAT_HOME%/ work 这个目录下
     */
    public static final String workFolder = SystemUtil.get("user.dir") + File.separator + "work";
}