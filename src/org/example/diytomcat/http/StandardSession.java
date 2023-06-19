package org.example.diytomcat.http;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * 作者：cjy
 * 类名：StandardSession
 * 全路径类名：org.example.diytomcat.http.StandardSession
 * 父类或接口：@see HttpSession
 * 描述：标准会话
 */
public class StandardSession implements HttpSession {
    /**
     * 描述：用于在 session 中存放数据的
     */
    private Map<String, Object> attributesMap;

    /**
     * 描述：当前 session 的唯一id
     */
    private String id;
    /**
     * 描述：创建时间
     */
    private long creationTime;
    /**
     * 描述：最后访问时间 (用于对 session 自动失效。 一般默认是30分钟，如果不登录， session 就会自动失效了。)
     */
    private long lastAccessedTime;
    /**
     * 描述：servlet上下文
     */
    private ServletContext servletContext;

    /**
     * 描述：最大持续时间的分钟数
     */
    private int maxInactiveInterval;

    public StandardSession(String jsessionid, ServletContext servletContext) {
        this.attributesMap = new HashMap<>();
        this.id = jsessionid;
        this.creationTime = System.currentTimeMillis();
        this.servletContext = servletContext;
    }

    public void removeAttribute(String name) {
        attributesMap.remove(name);

    }

    public void setAttribute(String name, Object value) {
        attributesMap.put(name, value);
    }

    public Object getAttribute(String name) {
        return attributesMap.get(name);
    }

    public Enumeration<String> getAttributeNames() {
        Set<String> keys = attributesMap.keySet();
        return Collections.enumeration(keys);
    }

    public long getCreationTime() {

        return this.creationTime;
    }

    public String getId() {
        return id;
    }

    public long getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(long lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }

    public int getMaxInactiveInterval() {
        return this.maxInactiveInterval;
    }
    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    public HttpSessionContext getSessionContext() {

        return null;
    }

    public Object getValue(String arg0) {

        return null;
    }

    public String[] getValueNames() {

        return null;
    }

    public void invalidate() {
        attributesMap.clear();

    }

    public boolean isNew() {
        return creationTime == lastAccessedTime;
    }

    public void putValue(String arg0, Object arg1) {

    }

    public void removeValue(String arg0) {

    }

}