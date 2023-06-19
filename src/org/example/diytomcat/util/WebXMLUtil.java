package org.example.diytomcat.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.example.diytomcat.catalina.Context;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import cn.hutool.core.io.FileUtil;
import static org.example.diytomcat.util.Constant.webXmlFile;

/**
 * 作者：cjy
 * 类名：WebXMLUtil
 * 全路径类名：org.example.diytomcat.util.WebXMLUtil
 * 父类或接口：
 * 描述：web xmlutil
 */
public class WebXMLUtil {
    /**
     * 描述：mime类型映射
     */
    private static Map<String, String> mimeTypeMapping = new HashMap<>();


    /**
     * 方法名：getMimeType
     * 传入参数：@param extName ext名字
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：根据后缀名获取mimeType.
     *      第一次调用会初始化， 如果找不到对应的，就默认返回 "text/html"
     *      这里做了 synchronized 线程安全的处理， 因为会调用 initMimeType 进行初始化，如果两个线程同时来，那么可能导致被初始化两次。
     */
    public static synchronized String getMimeType(String extName) {
        if (mimeTypeMapping.isEmpty())
            initMimeType();

        String mimeType = mimeTypeMapping.get(extName);
        if (null == mimeType)
            return "text/html";

        return mimeType;

    }

    private static void initMimeType() {
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document d = Jsoup.parse(xml);

        Elements es = d.select("mime-mapping");
        for (Element e : es) {
            String extName = e.select("extension").first().text();
            String mimeType = e.select("mime-type").first().text();
            mimeTypeMapping.put(extName, mimeType);
        }

    }
    public static String getWelcomeFile(Context context) {
        String xml = FileUtil.readUtf8String(webXmlFile);
        Document d = Jsoup.parse(xml);
        Elements es = d.select("welcome-file");
        for (Element e : es) {
            String welcomeFileName = e.text();
            File f = new File(context.getDocBase(), welcomeFileName);
            if (f.exists())
                return f.getName();
        }
        return "index.html";
    }
}