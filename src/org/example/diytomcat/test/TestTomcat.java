package org.example.diytomcat.test;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.NetUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import org.example.diytomcat.util.MiniBrowser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 作者：cjy
 * 类名：TestTomcat
 * 全路径类名：org.example.diytomcat.test.TestTomcat
 * 父类或接口：
 * 描述：单元测试
 */
public class TestTomcat {
    private static int port = 18080;
    private static String ip = "127.0.0.1";
    @BeforeClass
    public static void beforeClass() {
        //所有测试开始前看diy tomcat 是否已经启动了
        if(NetUtil.isUsableLocalPort(port)) {
            System.err.println("请先启动 位于端口: " +port+ " 的diy tomcat，否则无法进行单元测试");
            System.exit(1);
        }
        else {
            System.out.println("检测到 diy tomcat已经启动，开始进行单元测试");
        }
    }

    @Test
    public void testHelloTomcat() {
        String html = getContentString("/");
        Assert.assertEquals(html,"Hello DIY Tomcat from cjy");
    }

    @Test
    public void testTimeConsumeHtml() throws InterruptedException {
        //准备一个线程池，里面有20根线程。
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(20, 20, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(10));

        TimeInterval timeInterval = DateUtil.timer();//开始计时

        for(int i = 0; i<3; i++){//连续执行3个任务
            threadPool.execute(new Runnable(){
                public void run() {
                    getContentString("/timeConsume.html");
                }
            });
        }
        threadPool.shutdown();
        threadPool.awaitTermination(1, TimeUnit.HOURS);//给线程池1个小时的时间去执行

        long duration = timeInterval.intervalMs();//获取经过了多长时间的毫秒数

        Assert.assertTrue(duration < 3000);//断言它是超过3秒的
    }

    /**
     * 方法名：testaIndex
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：访问 /a 看是否会跳到欢迎页面
     */
    @Test
    public void testaIndex() {
        String html = getContentString("/a");
        Assert.assertEquals(html,"Hello DIY Tomcat from index.html@a");
    }

    /**
     * 方法名：testbIndex
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：访问 /b/ 看是否会跳到欢迎页面
     */
    @Test
    public void testbIndex() {
        String html = getContentString("/b/");
        Assert.assertEquals(html,"Hello DIY Tomcat from index.html@b");
    }

    @Test
    public void test404() {
        String response  = getHttpString("/not_exist.html");
        containAssert(response, "HTTP/1.1 404 Not Found");
    }
    @Test
    public void test500() {
        String response  = getHttpString("/500.html");
        containAssert(response, "HTTP/1.1 500 Internal Server Error");
    }

    @Test
    public void testaTxt() {
        String response  = getHttpString("/a.txt");
        containAssert(response, "Content-Type: text/plain");
    }

    @Test
    public void testPNG() {
        byte[] bytes = getContentBytes("/logo.png");
        int pngFileLength = 1672;
        Assert.assertEquals(pngFileLength, bytes.length);
    }
    @Test
    public void testPDF() {
        byte[] bytes = getContentBytes("/etf.pdf");
        int pngFileLength = 3590775;
        Assert.assertEquals(pngFileLength, bytes.length);
    }

    @Test
    public void testhello() {
        String html = getContentString("/j2ee/hello");
        Assert.assertEquals(html,"Hello DIY Tomcat from HelloServlet");
    }

    @Test
    public void testJavawebHello() {
        String html = getContentString("/javaweb/hello");
        containAssert(html,"Hello DIY Tomcat from HelloServlet@javaweb");
    }

    @Test
    public void testJavawebHelloSingleton() {
        String html1 = getContentString("/javaweb/hello");
        String html2 = getContentString("/javaweb/hello");
        Assert.assertEquals(html1,html2);
    }
    @Test
    public void testgetParam() {
        String uri = "/javaweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","meepo");
        String html = MiniBrowser.getContentString(url, params, true);
        Assert.assertEquals(html,"get name:meepo");
    }
    @Test
    public void testpostParam() {
        String uri = "/javaweb/param";
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        Map<String,Object> params = new HashMap<>();
        params.put("name","meepo");
        String html = MiniBrowser.getContentString(url, params, false);
        Assert.assertEquals(html,"post name:meepo");

    }

    /**
     * 方法名：testheader
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：看看 user-agent 获取出来是否是 miniBrowser 中定义的那个值
     */
    @Test
    public void testheader() {
        String html = getContentString("/javaweb/header");
        Assert.assertEquals(html,"how2j mini brower / java1.8");
    }

    /**
     * 方法名：testsetCookie
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：测试 Cookie
     */
    @Test
    public void testsetCookie() {
        String http = getHttpString("/javaweb/setCookie");
        containAssert(http,"Set-Cookie: name=Gareen(cookie); Expires=");
    }

    @Test
    public void testgetCookie() throws IOException {
        String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getCookie");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        conn.setRequestProperty("Cookie","name=Gareen(cookie)");
        conn.connect();
        InputStream is = conn.getInputStream();
        String html = IoUtil.read(is, "utf-8");
        containAssert(html,"name:Gareen(cookie)");
    }

    /**
     * 方法名：testSession
     * 传入参数：
     * 异常类型：@throws IOException ioexception
     * 返回类型：
     * 描述：测试会话
     * , 。
     */
    @Test
    public void testSession() throws IOException {
        //先通过访问 setSession，设置 name_in_session, 并且得到 jsessionid
        String jsessionid = getContentString("/javaweb/setSession");
        if(null!=jsessionid)
            jsessionid = jsessionid.trim();

        String url = StrUtil.format("http://{}:{}{}", ip,port,"/javaweb/getSession");
        URL u = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();
        //然后 把 jsessionid 作为 Cookie 的值提交到 getSession
        conn.setRequestProperty("Cookie","JSESSIONID="+jsessionid);
        conn.connect();
        InputStream is = conn.getInputStream();
        //就获取了session 中的数据了
        String html = IoUtil.read(is, "utf-8");
        containAssert(html,"Gareen(session)");
    }


    /**
     * 方法名：testGzip
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：通过 gzip 等于 true 获取首页的字节数组，然后把这个字节数组ungzip之后，看看是否是期望的字符串
     */
    @Test
    public void testGzip() {
        byte[] gzipContent = getContentBytes("/",true);
        byte[] unGzipContent = ZipUtil.unGzip(gzipContent);
        String html = new String(unGzipContent);
        Assert.assertEquals(html, "Hello DIY Tomcat from cjy");
    }

    @Test
    public void testJsp() {
        String html = getContentString("/javaweb/");
        Assert.assertEquals(html, "hello jsp@javaweb");
    }
    @Test
    public void testClientJump(){
        String http_servlet = getHttpString("/javaweb/jump1");
        containAssert(http_servlet,"HTTP/1.1 302 Found");
        String http_jsp = getHttpString("/javaweb/jump1.jsp");
        containAssert(http_jsp,"HTTP/1.1 302 Found");
    }
    @Test
    public void testServerJump(){
        String http_servlet = getHttpString("/javaweb/jump2");
        containAssert(http_servlet,"Hello DIY Tomcat from HelloServlet");
    }
    @Test
    public void testServerJumpWithAttributes(){
        String http_servlet = getHttpString("/javaweb/jump2");
        containAssert(http_servlet,"Hello DIY Tomcat from HelloServlet@javaweb, the name is gareen");
    }
    @Test
    public void testJavaweb0Hello() {
        String html = getContentString("/javaweb0/hello");
        containAssert(html,"Hello DIY Tomcat from HelloServlet@javaweb");
    }

    private byte[] getContentBytes(String uri) {
        return getContentBytes(uri,false);
    }
    private byte[] getContentBytes(String uri,boolean gzip) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        return MiniBrowser.getContentBytes(url,gzip);
    }
    private String getContentString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        String content = MiniBrowser.getContentString(url);
        return content;
    }

    /**
     * 方法名：getHttpString
     * 传入参数：@param uri uri
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：得到http字符串
     */
    private String getHttpString(String uri) {
        String url = StrUtil.format("http://{}:{}{}", ip,port,uri);
        String http = MiniBrowser.getHttpString(url);
        return http;
    }

    /**
     * 方法名：containAssert
     * 传入参数：@param html html
     *         @param string 字符串
     * 异常类型：
     * 返回类型：
     * 描述：containAssert 断言，来判断html 里是否包含某段字符串的断言
     */
    private void containAssert(String html, String string) {
        boolean match = StrUtil.containsAny(html, string);
        Assert.assertTrue(match);
    }
}