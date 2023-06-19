package org.example.diytomcat.util;

import cn.hutool.core.io.FileUtil;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 作者：cjy
 * 类名：ContextXMLUtil
 * 全路径类名：org.example.diytomcat.util.ContextXMLUtil
 * 父类或接口：
 * 描述：取得Context.xml的配置信息
 */
public class ContextXMLUtil {

    public static String getWatchedResource() {
        try {
            String xml = FileUtil.readUtf8String(Constant.contextXmlFile);
            Document d = Jsoup.parse(xml);
            Element e = d.select("WatchedResource").first();
            return e.text();
        } catch (Exception e) {
            e.printStackTrace();
            return "WEB-INF/web.xml";
        }
    }
}