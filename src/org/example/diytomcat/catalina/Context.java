package org.example.diytomcat.catalina;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import org.apache.jasper.JspC;
import org.apache.jasper.compiler.JspRuntimeContext;
import org.example.diytomcat.classloader.WebappClassLoader;
import org.example.diytomcat.exception.WebConfigDuplicatedException;
import org.example.diytomcat.http.ApplicationContext;
import org.example.diytomcat.http.StandardServletConfig;
import org.example.diytomcat.util.ContextXMLUtil;
import org.example.diytomcat.watcher.ContextFileChangeWatcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.util.*;

/**
 * 作者：cjy
 * 类名：Context
 * 全路径类名：org.example.diytomcat.catalina.Context
 * 父类或接口：
 * 描述：上下文
 */
public class Context {
    /**
     * 描述：访问的路径
     */
    private String path;
    /**
     * 描述：对应在文件系统的位置
     */
    private String docBase;
    /**
     * 描述：对应 XXX/WEB-INF/web.xml 文件
     */
    private File contextWebXmlFile;

    /**
     * 描述：
     *  1. 地址 对应 Servlet 的类名
     *  2. 地址 对应 Servlet 的名称
     *  3. Servlet的名称 对应 类名
     *  4. Servlet类名 对应 名称
     */
    private Map<String, String> url_servletClassName;
    private Map<String, String> url_ServletName;
    private Map<String, String> servletName_className;
    private Map<String, String> className_servletName;
    /**
     * 描述：用于servlet存放的初始化信息
     */
    private Map<String, Map<String, String>> servlet_className_init_params;

    /**
     * 描述：过滤器相关配置
     */
    private Map<String, List<String>> url_filterClassName;
    private Map<String, List<String>> url_FilterNames;
    private Map<String, String> filterName_className;
    private Map<String, String> className_filterName;
    private Map<String, Map<String, String>> filter_className_init_params;

    /**
     * 描述：用于存放哪些类需要做自启动(让 servlet 伴随着 context 的启动而被初始化，这就叫做 Servlet 的自启动)
     */
    private List<String> loadOnStartupServletClassNames;

    /**
     * 描述：应用类加载器
     */
    private WebappClassLoader webappClassLoader;

    /**
     * 描述：主机
     */
    private Host host;
    /**
     * 描述：热加载
     */
    private boolean reloadable;
    /**
     * 描述：上下文文件改变观察者
     */
    private ContextFileChangeWatcher contextFileChangeWatcher;
    /**
     * 描述：servlet上下文
     */
    private ServletContext servletContext;
    /**
     * 描述：存放 servlet 池子
     */
    private Map<Class<?>, HttpServlet> servletPool;
    /**
     * 描述：过滤器池
     */
    private Map<String, Filter> filterPool;
    /**
     * 描述：
     * 描述：监听者们
     */
    private List<ServletContextListener> listeners;

    /**
     * 方法名：Context
     * 传入参数：@param path 路径
     *
     * @param docBase    绝对路径
     * @param host       主机
     * @param reloadable 可写
     *                   异常类型：
     *                   返回类型：@return
     *                   描述：上下文
     */
    public Context(String path, String docBase, Host host, boolean reloadable) {
        TimeInterval timeInterval = DateUtil.timer();
        this.host = host;
        this.reloadable = reloadable;

        this.path = path;
        this.docBase = docBase;
        this.contextWebXmlFile = new File(docBase, ContextXMLUtil.getWatchedResource());

        this.url_servletClassName = new HashMap<>();
        this.url_ServletName = new HashMap<>();
        this.servletName_className = new HashMap<>();
        this.className_servletName = new HashMap<>();
        this.servlet_className_init_params = new HashMap<>();

        //初始化过滤器配置
        this.url_filterClassName = new HashMap<>();
        this.url_FilterNames = new HashMap<>();
        this.filterName_className = new HashMap<>();
        this.className_filterName = new HashMap<>();
        this.filter_className_init_params = new HashMap<>();

        this.loadOnStartupServletClassNames = new ArrayList<>();

        this.servletContext = new ApplicationContext(this);

        // 让commonClassLoader 作为 WebappClassLoader 父类存在。
        ClassLoader commonClassLoader = Thread.currentThread().getContextClassLoader();//获取到 Bootstrap 里通过 Thread.currentThread().setContextClassLoader(commonClassLoader)设置的 commonClassLoader.
        this.webappClassLoader = new WebappClassLoader(docBase, commonClassLoader);

        this.servletPool = new HashMap<>();
        this.filterPool = new HashMap<>();//初始化过滤池

        //在构造方法中初始化 listeners
        listeners=new ArrayList<ServletContextListener>();

        LogFactory.get().info("Deploying web application directory {}", this.docBase);
        deploy();
        LogFactory.get().info("Deployment of web application directory {} has finished in {} ms", this.docBase,timeInterval.intervalMs());
    }

    /**
     * 方法名：reload
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：重新加载
     */
    public void reload() {
        host.reload(this);
    }

    /**
     * 方法名：deploy
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：部署
     */
    private void deploy() {
        loadListeners();//从 javaweb 的web.xml中扫描出监听器类

        init();

        if(reloadable){
            contextFileChangeWatcher = new ContextFileChangeWatcher(this);
            contextFileChangeWatcher.start();
        }

        //这里进行了JspRuntimeContext 的初始化
        //就是为了能够在jsp所转换的 java 文件
        //里的 javax.servlet.jsp.JspFactory.getDefaultFactory() 这行能够有返回值
        JspC c = new JspC();
        new JspRuntimeContext(servletContext, c);
    }

    /**
     * 方法名：init
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：初始化
     */
    private void init() {
        if (!contextWebXmlFile.exists())//先判断是否有 web.xml 文件
            return;//如果没有就返回了

        try {
            checkDuplicated();// 然后判断是否重复
        } catch (WebConfigDuplicatedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);
        parseServletMapping(d);
        parseFilterMapping(d);//解析javaweb的web.xml的filter

        parseServletInitParams(d);
        parseFilterInitParams(d);//解析javaweb的web.xml的filter的参数信息

        initFilter();//初始化过滤器

        parseLoadOnStartup(d);
        handleLoadOnStartup();

        fireEvent("init");//初始化事件
    }

    /**
     * 方法名：parseServletMapping
     * 传入参数：@param d d
     * 异常类型：
     * 返回类型：
     * 描述：解析javaweb的 web.xml的servlet
     */
    private void parseServletMapping(Document d) {
        // url_ServletName
        Elements mappingurlElements = d.select("servlet-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String servletName = mappingurlElement.parent().select("servlet-name").first().text();
            url_ServletName.put(urlPattern, servletName);
        }
        // servletName_className / className_servletName
        Elements servletNameElements = d.select("servlet servlet-name");
        for (Element servletNameElement : servletNameElements) {
            String servletName = servletNameElement.text();
            String servletClass = servletNameElement.parent().select("servlet-class").first().text();
            servletName_className.put(servletName, servletClass);
            className_servletName.put(servletClass, servletName);
        }
        // url_servletClassName
        Set<String> urls = url_ServletName.keySet();
        for (String url : urls) {
            String servletName = url_ServletName.get(url);
            String servletClassName = servletName_className.get(servletName);
            url_servletClassName.put(url, servletClassName);
        }
    }

    /**
     * 方法名：checkDuplicated
     * 传入参数：@param d d
     *
     * @param mapping 映射
     * @param desc    desc
     *                异常类型：@throws WebConfigDuplicatedException 网络配置复制异常
     *                返回类型：
     *                描述：检查servlet是否重复配置
     */
    private void checkDuplicated(Document d, String mapping, String desc) throws WebConfigDuplicatedException {
        Elements elements = d.select(mapping);
        // 判断逻辑是放入一个集合，然后把集合排序之后看两临两个元素是否相同
        List<String> contents = new ArrayList<>();
        for (Element e : elements) {
            contents.add(e.text());
        }

        Collections.sort(contents);

        for (int i = 0; i < contents.size() - 1; i++) {
            String contentPre = contents.get(i);
            String contentNext = contents.get(i + 1);
            if (contentPre.equals(contentNext)) {
                throw new WebConfigDuplicatedException(StrUtil.format(desc, contentPre));
            }
        }

    }

    private void checkDuplicated() throws WebConfigDuplicatedException {
        String xml = FileUtil.readUtf8String(contextWebXmlFile);
        Document d = Jsoup.parse(xml);

        checkDuplicated(d, "servlet-mapping url-pattern", "servlet url 重复,请保持其唯一性:{} ");
        checkDuplicated(d, "servlet servlet-name", "servlet 名称重复,请保持其唯一性:{} ");
        checkDuplicated(d, "servlet servlet-class", "servlet 类名重复,请保持其唯一性:{} ");
    }

    /**
     * 方法名：getServletClassName
     * 传入参数：@param uri uri
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：通过 uri 获取Servlet 类名
     */
    public String getServletClassName(String uri) {
        return url_servletClassName.get(uri);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDocBase() {
        return docBase;
    }

    public void setDocBase(String docBase) {
        this.docBase = docBase;
    }

    public WebappClassLoader getWebappClassLoader() {
        return webappClassLoader;
    }

    /**
     * 方法名：stop
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：停止
     */
    public void stop() {
        webappClassLoader.stop();
        contextFileChangeWatcher.stop();

        destroyServlets();

        fireEvent("destroy");//销毁事件
    }

    public boolean isReloadable() {
        return reloadable;
    }

    public void setReloadable(boolean reloadable) {
        this.reloadable = reloadable;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * 方法名：getServlet
     * 传入参数：@param clazz clazz
     * 异常类型：@throws InstantiationException 实例化异常
     *         @throws IllegalAccessException 非法访问异常
     *         @throws ServletException       servlet异常
     * 返回类型：@return {@link HttpServlet }
     * 描述：根据类对象来获取 servlet 对象
     */
    public synchronized HttpServlet  getServlet(Class<?> clazz)
            throws InstantiationException, IllegalAccessException, ServletException {
        HttpServlet servlet = servletPool.get(clazz);

        if (null == servlet) {
            servlet = (HttpServlet) clazz.newInstance();
            ServletContext servletContext = this.getServletContext();

            String className = clazz.getName();
            String servletName = className_servletName.get(className);

            //让 servlet 对象放进池子之前做初始化
            Map<String, String> initParameters = servlet_className_init_params.get(className);
            ServletConfig servletConfig = new StandardServletConfig(servletContext, servletName, initParameters);

            servlet.init(servletConfig);
            servletPool.put(clazz, servlet);
        }

        return servlet;
    }

    /**
     * 方法名：parseServletInitParams
     * 传入参数：@param d d
     * 异常类型：
     * 返回类型：
     * 描述：从 web.xml 中解析servlet初始化参数
     */
    private void parseServletInitParams(Document d) {
        Elements servletClassNameElements = d.select("servlet-class");
        for (Element servletClassNameElement : servletClassNameElements) {
            String servletClassName = servletClassNameElement.text();

            Elements initElements = servletClassNameElement.parent().select("init-param");
            if (initElements.isEmpty())
                continue;

            Map<String, String> initParams = new HashMap<>();

            for (Element element : initElements) {
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initParams.put(name, value);
            }

            servlet_className_init_params.put(servletClassName, initParams);

        }

//      System.out.println("class_name_init_params:" + servlet_className_init_params);

    }

    /**
     * 方法名：destroyServlets
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：销毁所有的 servlets
     */
    private void destroyServlets() {
        Collection<HttpServlet> servlets = servletPool.values();
        for (HttpServlet servlet : servlets) {
            servlet.destroy();
        }
    }

    /**
     * 方法名：parseLoadOnStartup
     * 传入参数：@param d d
     * 异常类型：
     * 返回类型：
     * 描述：解析哪些类需要做自启动
     */
    public void parseLoadOnStartup(Document d) {
        Elements es = d.select("load-on-startup");
        for (Element e : es) {
            String loadOnStartupServletClassName = e.parent().select("servlet-class").text();
            loadOnStartupServletClassNames.add(loadOnStartupServletClassName);
        }
    }

    /**
     * 方法名：handleLoadOnStartup
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：对这些类做自启动
     */
    public void handleLoadOnStartup() {
        for (String loadOnStartupServletClassName : loadOnStartupServletClassNames) {
            try {
                Class<?> clazz = webappClassLoader.loadClass(loadOnStartupServletClassName);
                getServlet(clazz);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | ServletException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 方法名：getWebClassLoader
     * 传入参数：
     * 异常类型：
     * 返回类型：@return {@link WebappClassLoader }
     * 描述：返回 WebClassLoader。
     */
    public WebappClassLoader getWebClassLoader() {
        return webappClassLoader;
    }

    /**
     * 方法名：parseFilterMapping
     * 传入参数：@param d d
     * 异常类型：
     * 返回类型：
     * 描述：解析 javaweb的 web.xml 里面的 Filter 信息
     */
    public void parseFilterMapping(Document d) {
        // filter_url_name
        Elements mappingurlElements = d.select("filter-mapping url-pattern");
        for (Element mappingurlElement : mappingurlElements) {
            String urlPattern = mappingurlElement.text();
            String filterName = mappingurlElement.parent().select("filter-name").first().text();

            List<String> filterNames= url_FilterNames.get(urlPattern);
            if(null==filterNames) {
                filterNames = new ArrayList<>();
                url_FilterNames.put(urlPattern, filterNames);
            }
            filterNames.add(filterName);
        }
        // class_name_filter_name
        Elements filterNameElements = d.select("filter filter-name");
        for (Element filterNameElement : filterNameElements) {
            String filterName = filterNameElement.text();
            String filterClass = filterNameElement.parent().select("filter-class").first().text();
            filterName_className.put(filterName, filterClass);
            className_filterName.put(filterClass, filterName);
        }
        // url_filterClassName

        Set<String> urls = url_FilterNames.keySet();
        for (String url : urls) {
            List<String> filterNames = url_FilterNames.get(url);
            if(null == filterNames) {
                filterNames = new ArrayList<>();
                url_FilterNames.put(url, filterNames);
            }
            for (String filterName : filterNames) {
                String filterClassName = filterName_className.get(filterName);
                List<String> filterClassNames = url_filterClassName.get(url);
                if(null==filterClassNames) {
                    filterClassNames = new ArrayList<>();
                    url_filterClassName.put(url, filterClassNames);
                }
                filterClassNames.add(filterClassName);
            }
        }
    }

    /**
     * 方法名：parseFilterInitParams
     * 传入参数：@param d d
     * 异常类型：
     * 返回类型：
     * 描述：解析过滤器参数信息
     */
    private void parseFilterInitParams(Document d) {
        Elements filterClassNameElements = d.select("filter-class");
        for (Element filterClassNameElement : filterClassNameElements) {
            String filterClassName = filterClassNameElement.text();

            Elements initElements = filterClassNameElement.parent().select("init-param");
            if (initElements.isEmpty())
                continue;

            Map<String, String> initParams = new HashMap<>();

            for (Element element : initElements) {
                String name = element.select("param-name").get(0).text();
                String value = element.select("param-value").get(0).text();
                initParams.put(name, value);
            }

            filter_className_init_params.put(filterClassName, initParams);

        }

    }

    /**
     * 方法名：initFilter
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：初始化过滤器
     */
    private void initFilter() {
        Set<String> classNames = className_filterName.keySet();
        for (String className : classNames) {
            try {
                Class clazz =  this.getWebClassLoader().loadClass(className);
                Map<String,String> initParameters = filter_className_init_params.get(className);
                String filterName = className_filterName.get(className);

                FilterConfig filterConfig = new StandardFilterConfig(servletContext, filterName, initParameters);

                Filter filter = filterPool.get(clazz);
                if(null==filter) {
                    filter = (Filter) ReflectUtil.newInstance(clazz);
                    filter.init(filterConfig);
                    filterPool.put(className, filter);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Filter> getMatchedFilters(String uri) {
        List<Filter> filters = new ArrayList<>();
        Set<String> patterns = url_filterClassName.keySet();
        Set<String> matchedPatterns = new HashSet<>();

        for (String pattern : patterns) {
            if(match(pattern,uri)) {
                matchedPatterns.add(pattern);
            }
        }

        Set<String> matchedFilterClassNames = new HashSet<>();
        for (String pattern : matchedPatterns) {
            List<String> filterClassName = url_filterClassName.get(pattern);
            matchedFilterClassNames.addAll(filterClassName);
        }
        for (String filterClassName : matchedFilterClassNames) {
            Filter filter = filterPool.get(filterClassName);
            filters.add(filter);
        }
        return filters;
    }

    private boolean match(String pattern, String uri) {
        // 完全匹配
        if(StrUtil.equals(pattern, uri))
            return true;

        // /* 模式
        if(StrUtil.equals(pattern, "/*"))
            return true;

        // 后缀名 /*.jsp
        if(StrUtil.startWith(pattern, "/*.")) {
            String patternExtName = StrUtil.subAfter(pattern, '.', false);
            String uriExtName = StrUtil.subAfter(uri, '.', false);
            if(StrUtil.equals(patternExtName, uriExtName))
                return true;
        }
        // 其他模式就懒得管了
        return false;
    }

    /**
     * 方法名：addListener
     * 传入参数：@param listener 侦听器
     * 异常类型：
     * 返回类型：
     * 描述：添加监听器
     */
    public void addListener(ServletContextListener listener){
        listeners.add(listener);
    }
    public void removeListener(ServletContextListener listener){
        listeners.remove(listener);
    }

    /**
     * 方法名：loadListeners
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：从 javaweb 的web.xml中扫描出监听器类
     */
    private void loadListeners()  {
        try {
            if(!contextWebXmlFile.exists())
                return;
            String xml = FileUtil.readUtf8String(contextWebXmlFile);
            Document d = Jsoup.parse(xml);

            Elements es = d.select("listener listener-class");
            for (Element e : es) {
                String listenerClassName = e.text();

                Class<?> clazz= this.getWebClassLoader().loadClass(listenerClassName);
                ServletContextListener listener = (ServletContextListener) clazz.newInstance();
                addListener(listener);

            }
        } catch (IORuntimeException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 方法名：fireEvent
     * 传入参数：@param type 类型
     * 异常类型：
     * 返回类型：
     * 描述：生命周期事件
     */
    private void fireEvent(String type) {
        ServletContextEvent event = new ServletContextEvent(servletContext);
        for (ServletContextListener servletContextListener : listeners) {
            if("init".equals(type))
                servletContextListener.contextInitialized(event);
            if("destroy".equals(type))
                servletContextListener.contextDestroyed(event);
        }
    }

}