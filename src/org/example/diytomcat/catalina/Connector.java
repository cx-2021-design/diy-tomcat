package org.example.diytomcat.catalina;

import cn.hutool.log.LogFactory;
import org.example.diytomcat.http.Request;
import org.example.diytomcat.http.Response;
import org.example.diytomcat.util.ThreadPoolUtil;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * 作者：cjy
 * 类名：Connector
 * 全路径类名：org.example.diytomcat.catalina.Connector
 * 父类或接口：@see Runnable
 * 描述：接受 Socket 请求
 */
public class Connector implements Runnable {
    /**
     * 描述：端口
     */
    int port;
    /**
     * 描述：服务
     */
    private Service service;

    /**
     * 描述：压缩
     */
    private String compression;
    /**
     * 描述：进行压缩操作的最小尺寸(太小就没有必要压缩了)
     */
    private int compressionMinSize;
    /**
     * 描述：不进行压缩的浏览器
     */
    private String noCompressionUserAgents;
    /**
     * 描述：哪些 mimeType 才需要进行压缩
     */
    private String compressableMimeType;
    public String getCompression() {
        return compression;
    }

    public void setCompression(String compression) {
        this.compression = compression;
    }

    public int getCompressionMinSize() {
        return compressionMinSize;
    }

    public void setCompressionMinSize(int compressionMinSize) {
        this.compressionMinSize = compressionMinSize;
    }

    public String getNoCompressionUserAgents() {
        return noCompressionUserAgents;
    }

    public void setNoCompressionUserAgents(String noCompressionUserAgents) {
        this.noCompressionUserAgents = noCompressionUserAgents;
    }

    public String getCompressableMimeType() {
        return compressableMimeType;
    }

    public void setCompressableMimeType(String compressableMimeType) {
        this.compressableMimeType = compressableMimeType;
    }

    /**
     * 方法名：Connector
     * 传入参数：@param service 服务
     * 异常类型：
     * 返回类型：@return
     * 描述：连接器
     */
    public Connector(Service service) {
        this.service = service;
    }

    public Service getService() {
        return service;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * 方法名：run
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：运行
     */
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(port);
            while(true) {
                Socket s = ss.accept();
                Runnable r = new Runnable() {//当有请求来的时候，就创建一个 Runnable 任务,
                    @Override
                    public void run() {
                        try {
                            Request request = new Request(s, Connector.this);
                            Response response = new Response();
                            HttpProcessor processor = new HttpProcessor();
                            processor.execute(s,request, response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (!s.isClosed())
                                try {
                                    s.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                        }
                    }
                };
                ThreadPoolUtil.run(r);//并且把他丢进线程池运行即, 然后就再去准备接受下一个请求。
            }

        } catch (IOException e) {
            LogFactory.get().error(e);
            e.printStackTrace();
        }
    }

    /**
     * 方法名：init
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：初始化
     */
    public void init() {
        LogFactory.get().info("Initializing ProtocolHandler [http-bio-{}]",port);
    }

    /**
     * 方法名：start
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：开始
     */
    public void start() {
        LogFactory.get().info("Starting ProtocolHandler [http-bio-{}]",port);
        new Thread(this).start();
    }

}