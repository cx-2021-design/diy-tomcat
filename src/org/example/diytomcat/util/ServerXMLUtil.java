package org.example.diytomcat.util;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.io.FileUtil;
import org.example.diytomcat.catalina.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者：cjy
 * 类名：ServerXMLUtil
 * 全路径类名：org.example.diytomcat.util.ServerXMLUtil
 * 父类或接口：
 * 描述：解析 server.xml文件
 */
public class ServerXMLUtil {
    /**
     * 方法名：getConnectors
     * 传入参数：@param service 服务
     * 异常类型：
     * 返回类型：@return {@link List }<{@link Connector }>
     * 描述：根据服务获取 Connectors 集合
     */
    public static List<Connector> getConnectors(Service service) {
        List<Connector> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);//获取 server.xml 的内容
        Document d = Jsoup.parse(xml);//转换成 jsoup document
        Elements es = d.select("Connector");//查询所有的 Context 节点

        //遍历这些节点,并获取对应的信息，以生成 Context 对象， 然后放进 result 返回。
        for (Element e : es) {
            int port = Convert.toInt(e.attr("port"));
            String compression = e.attr("compression");

            int compressionMinSize = Convert.toInt(e.attr("compressionMinSize"), 0);
            String noCompressionUserAgents = e.attr("noCompressionUserAgents");
            String compressableMimeType = e.attr("compressableMimeType");
            Connector c = new Connector(service);
            c.setPort(port);
            c.setCompression(compression);
            c.setCompressableMimeType(compressableMimeType);
            c.setNoCompressionUserAgents(noCompressionUserAgents);
            c.setCompressableMimeType(compressableMimeType);
            c.setCompressionMinSize(compressionMinSize);
            result.add(c);
        }
        return result;
    }

    /**
     * 方法名：getContexts
     * 传入参数：@param host 主机
     * 异常类型：
     * 返回类型：@return {@link List }<{@link Context }>
     * 描述：根据host获得Contexts
     */
    public static List<Context> getContexts(Host host) {
        List<Context> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);////获取 server.xml 的内容
        Document d = Jsoup.parse(xml);//转换成 jsoup document

        Elements es = d.select("Context");//查询所有的 Context 节点

        //遍历这些节点
        for (Element e : es) {
            //并获取对应的 path、docBase和reloadable
            String path = e.attr("path");
            String docBase = e.attr("docBase");

            boolean reloadable = Convert.toBool(e.attr("reloadable"), true);
            Context context = new Context(path, docBase, host, reloadable);//生成 Context 对象
            result.add(context);//然后放进 result 返回。
        }
        return result;
    }

    public static String getEngineDefaultHost() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);

        Element host = d.select("Engine").first();
        return host.attr("defaultHost");
    }

    public static String getServiceName() {
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);

        Element host = d.select("Service").first();
        return host.attr("name");
    }

    public static List<Host> getHosts(Engine engine) {
        List<Host> result = new ArrayList<>();
        String xml = FileUtil.readUtf8String(Constant.serverXmlFile);
        Document d = Jsoup.parse(xml);

        Elements es = d.select("Host");
        for (Element e : es) {
            String name = e.attr("name");
            Host host = new Host(name,engine);
            result.add(host);
        }
        return result;
    }
}