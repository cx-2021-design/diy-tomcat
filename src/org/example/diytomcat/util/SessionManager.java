package org.example.diytomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.SecureUtil;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.http.StandardSession;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * 作者：cjy
 * 类名：SessionManager
 * 全路径类名：org.example.diytomcat.util.SessionManager
 * 父类或接口：
 * 描述：会话管理器
 */
public class SessionManager {
    /**
     * 描述：所有的session都放在这里
     */
    private static Map<String, StandardSession> sessionMap = new HashMap<>();
    /**
     * 描述：session的默认失效时间
     */
    private static int defaultTimeout = getTimeout();

    static {//默认启动检测Session是否失效的线程
        startSessionOutdateCheckThread();
    }

    /**
     * 方法名：getSession
     * 传入参数：@param jsessionid jsessionid
     *         @param request  请求
     *         @param response 响应
     * 返回类型：@return {@link HttpSession }
     * 描述：获取session
     */
    public static HttpSession getSession(String jsessionid, Request request, Response response) {

        if (null == jsessionid) {//如果浏览器没有传递 jsessionid 过来
            return newSession(request, response);//那么就创建一个新的session
        } else {
            StandardSession currentSession = sessionMap.get(jsessionid);
            if (null == currentSession) {//如果浏览器传递过来的 jsessionid 无效
                return newSession(request, response);//那么也创建一个新的 session
            } else {//否则就使用现成的session
                currentSession.setLastAccessedTime(System.currentTimeMillis());//并且修改它的lastAccessedTime
                createCookieBySession(currentSession, request, response);//以及创建对应的 cookie
                return currentSession;
            }
        }
    }

    private static void createCookieBySession(HttpSession session, Request request, Response response) {
        Cookie cookie = new Cookie("JSESSIONID", session.getId());
        cookie.setMaxAge(session.getMaxInactiveInterval());
        cookie.setPath(request.getContext().getPath());
        response.addCookie(cookie);
    }

    /**
     * 方法名：newSession
     * 传入参数：@param request 请求
     *         @param response 响应
     * 返回类型：@return {@link HttpSession }
     * 描述：创建session
     */
    private static HttpSession newSession(Request request, Response response) {
        ServletContext servletContext = request.getServletContext();
        String sid = generateSessionId();
        StandardSession session = new StandardSession(sid, servletContext);
        session.setMaxInactiveInterval(defaultTimeout);
        sessionMap.put(sid, session);
        createCookieBySession(session, request, response);
        return session;
    }

    /**
     * 方法名：getTimeout
     * 传入参数：
     * 异常类型：
     * 返回类型：@return int
     * 描述：从web.xml 中获取默认失效时间
     */
    private static int getTimeout() {
        int defaultResult = 30;
        try {
            Document d = Jsoup.parse(Constant.webXmlFile, "utf-8");
            Elements es = d.select("session-config session-timeout");
            if (es.isEmpty())
                return defaultResult;
            return Convert.toInt(es.get(0).text());
        } catch (IOException e) {
            return defaultResult;
        }
    }

    /**
     * 方法名：checkOutDateSession
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：从sessionMap里根据 lastAccessedTime 筛选出过期的 jsessionids ,然后把他们从 sessionMap 里去掉
     */
    private static void checkOutDateSession() {
        Set<String> jsessionids = sessionMap.keySet();
        List<String> outdateJessionIds = new ArrayList<>();

        for (String jsessionid : jsessionids) {
            StandardSession session = sessionMap.get(jsessionid);
            long interval = System.currentTimeMillis() -  session.getLastAccessedTime();
            if (interval > session.getMaxInactiveInterval() * 1000)
                outdateJessionIds.add(jsessionid);
        }

        for (String jsessionid : outdateJessionIds) {
            sessionMap.remove(jsessionid);
        }
    }

    /**
     * 方法名：startSessionOutdateCheckThread
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：默认启动检测Session是否失效的线程
     */
    private static void startSessionOutdateCheckThread() {
        new Thread() {
            public void run() {
                while (true) {
                    //启动线程，每隔30秒调用一次 checkOutDateSession 方法
                    checkOutDateSession();
                    ThreadUtil.sleep(1000 * 30);
                }
            }

        }.start();

    }

    /**
     * 方法名：generateSessionId
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：创建 sessionid
     */
    public static synchronized String generateSessionId() {
        String result = null;
        byte[] bytes = RandomUtil.randomBytes(16);
        result = new String(bytes);
        result = SecureUtil.md5(result);
        result = result.toUpperCase();
        return result;
    }

}
