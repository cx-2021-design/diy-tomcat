package org.example.diytomcat.util;

import cn.hutool.http.HttpUtil;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 作者：cjy
 * 类名：MiniBrowser
 * 全路径类名：org.example.diytomcat.util.MiniBrowser
 * 父类或接口：
 * 描述：迷你浏览器（模拟发送 Http 协议的请求，并且获取完整的 Http 响应）
 */
public class MiniBrowser {

    public static void main(String[] args) throws Exception {
        String url = "http://127.0.0.1:18080/javaweb/setCookie";
        String contentString= getContentString(url,false);
        System.out.println(contentString);
        String httpString= getHttpString(url,false);
        System.out.println(httpString);
    }


    /**
     * 方法名：getContentBytes
     * 传入参数：@param url 路径
     *         @param isGet  是得到
     * 返回类型：@return {@link byte[] }
     * 描述：获取内容字节
     */
    public static byte[] getContentBytes(String url, Map<String,Object> params, boolean isGet) {
        return getContentBytes(url, false,params,isGet);
    }

    /**
     * 方法名：getContentBytes
     * 传入参数：@param url url
     *
     * @param gzip gzip
     *             异常类型：
     *             返回类型：@return {@link byte[] }
     *             描述：获取内容字节
     */
    public static byte[] getContentBytes(String url, boolean gzip) {
        return getContentBytes(url, gzip,null,true);
    }

    /**
     * 方法名：getContentBytes
     * 传入参数：@param url url
     * 异常类型：
     * 返回类型：@return {@link byte[] }
     * 描述：获取内容字节
     */
    public static byte[] getContentBytes(String url) {
        return getContentBytes(url, false,null,true);
    }

    /**
     * 方法名：getContentString
     * 传入参数：@param url url
     *
     * @param params 参数个数
     * @param isGet  是得到
     *               异常类型：
     *               返回类型：@return {@link String }
     *               描述：获取内容字符串
     */
    public static String getContentString(String url, Map<String,Object> params, boolean isGet) {
        return getContentString(url,false,params,isGet);
    }

    /**
     * 方法名：getContentString
     * 传入参数：@param url url
     *
     * @param gzip gzip
     *             异常类型：
     *             返回类型：@return {@link String }
     *             描述：获取内容字符串
     */
    public static String getContentString(String url, boolean gzip) {
        return getContentString(url, gzip, null, true);
    }

    /**
     * 方法名：getContentString
     * 传入参数：@param url url
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：获取内容字符串
     */
    public static String getContentString(String url) {
        return getContentString(url, false, null, true);
    }

    /**
     * 方法名：getContentString
     * 传入参数：@param url url
     *
     * @param gzip   gzip
     * @param params 参数个数
     * @param isGet  是得到
     *               异常类型：
     *               返回类型：@return {@link String }
     *               描述：获取内容字符串
     */
    public static String getContentString(String url, boolean gzip, Map<String,Object> params, boolean isGet) {
        byte[] result = getContentBytes(url, gzip,params,isGet);
        if(null==result)
            return null;
        try {
            return new String(result,"utf-8").trim();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * 方法名：getContentBytes
     * 传入参数：@param url url
     *
     * @param gzip   gzip
     * @param params 参数个数
     * @param isGet  是否为get
     *               异常类型：
     *               返回类型：@return {@link byte[] }
     *               描述：获取内容字节
     */
    public static byte[] getContentBytes(String url, boolean gzip, Map<String,Object> params, boolean isGet) {
        byte[] response = getHttpBytes(url,gzip,params,isGet);
        byte[] doubleReturn = "\r\n\r\n".getBytes();

        int pos = -1;
        for (int i = 0; i < response.length-doubleReturn.length; i++) {
            byte[] temp = Arrays.copyOfRange(response, i, i + doubleReturn.length);

            if(Arrays.equals(temp, doubleReturn)) {
                pos = i;
                break;
            }
        }
        if(-1==pos)
            return null;

        pos += doubleReturn.length;

        byte[] result = Arrays.copyOfRange(response, pos, response.length);
        return result;
    }

    /**
     * 方法名：getHttpString
     * 传入参数：@param url url
     *
     * @param gzip gzip
     *             异常类型：
     *             返回类型：@return {@link String }
     *             描述：得到http字符串
     */
    public static String getHttpString(String url,boolean gzip) {
        return getHttpString(url, gzip, null, true);
    }

    /**
     * 方法名：getHttpString
     * 传入参数：@param url url
     * 异常类型：
     * 返回类型：@return {@link String }
     * 描述：得到http字符串
     */
    public static String getHttpString(String url) {
        return getHttpString(url, false, null, true);
    }

    /**
     * 方法名：getHttpString
     * 传入参数：@param url url
     *         @param gzip   gzip
     *         @param params 参数个数
     *         @param isGet  是得到
     * 返回类型：@return {@link String }
     * 描述：返回字符串的 http 响应
     */
    public static String getHttpString(String url,boolean gzip, Map<String,Object> params, boolean isGet) {
        byte[]  bytes=getHttpBytes(url,gzip,params,isGet);
        return new String(bytes).trim();
    }

    /**
     * 方法名：getHttpString
     * 传入参数：@param url url
     *         @param params 参数个数
     *         @param isGet  是得到
     * 返回类型：@return {@link String }
     * 描述：返回字符串的 http 响应
     */
    public static String getHttpString(String url, Map<String,Object> params, boolean isGet) {
        return getHttpString(url,false,params,isGet);
    }

    /**
     * 方法名：getHttpBytes
     * 传入参数：@param url url（如：http://static.how2j.cn/diytomcat.html）
     *         @param gzip   是否进行gzip压缩
     *         @param params 参数个数
     *         @param isGet  是否为get请求
     * 返回类型：@return {@link byte[] }
     * 描述：返回二进制的 http 响应
     */
    public static byte[] getHttpBytes(String url,boolean gzip, Map<String,Object> params, boolean isGet) {
        //判断是Get请求还是post请求
        String method = isGet?"GET":"POST";
        //result就是最后返回的二进制，初始化为null
        byte[] result = null;
        try {
            URL u = new URL(url);
            Socket client = new Socket();
            int port = u.getPort();
            if(-1==port)
                port = 80;
            InetSocketAddress inetSocketAddress = new InetSocketAddress(u.getHost(), port);
            client.connect(inetSocketAddress, 1000);
            Map<String,String> requestHeaders = new HashMap<>();

            requestHeaders.put("Host", u.getHost()+":"+port);
            requestHeaders.put("Accept", "text/html");
            requestHeaders.put("Connection", "close");
            requestHeaders.put("User-Agent", "how2j mini brower / java1.8");

            if(gzip)
                requestHeaders.put("Accept-Encoding", "gzip");

            String path = u.getPath();
            if(path.length()==0)
                path = "/";

            if(null!=params && isGet){
                String paramsString = HttpUtil.toParams(params);
                path = path + "?" + paramsString;
            }

            String firstLine = method + " " + path + " HTTP/1.1\r\n";

            StringBuffer httpRequestString = new StringBuffer();
            httpRequestString.append(firstLine);
            Set<String> headers = requestHeaders.keySet();
            for (String header : headers) {
                String headerLine = header + ":" + requestHeaders.get(header)+"\r\n";
                httpRequestString.append(headerLine);
            }

            if(null!=params && !isGet){
                String paramsString = HttpUtil.toParams(params);
                httpRequestString.append("\r\n");
                httpRequestString.append(paramsString);
            }

            PrintWriter pWriter = new PrintWriter(client.getOutputStream(), true);
            pWriter.println(httpRequestString);
            InputStream is = client.getInputStream();

            result = readBytes(is,true);
            client.close();
        } catch (Exception e) {//出现异常
            e.printStackTrace();//打印异常栈堆跟踪信息
            try {
                result = e.toString().getBytes("utf-8");//以“utf-8”编码格式输出result字符串结果
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
        }

        return result;

    }

    /**
     * 方法名：readBytes
     * 传入参数：
     * @param is 是
     * @param fully 完全
     *              异常类型：@throws IOException ioexception
     *              返回类型：@return {@link byte[] }
     *              描述：读取字节
     */
    public static byte[] readBytes(InputStream is, boolean fully) throws IOException {
        int buffer_size = 1024;
        byte buffer[] = new byte[buffer_size];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while(true) {
            int length = is.read(buffer);
            if(-1==length)
                break;
            baos.write(buffer, 0, length);
            if(!fully && length!=buffer_size)
                break;
        }
        byte[] result =baos.toByteArray();
        return result;
    }
}