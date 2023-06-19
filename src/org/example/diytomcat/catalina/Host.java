package org.example.diytomcat.catalina;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.LogFactory;
import org.example.diytomcat.util.Constant;
import org.example.diytomcat.util.ServerXMLUtil;
import org.example.diytomcat.watcher.WarFileWatcher;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者：cjy
 * 类名：Host
 * 全路径类名：org.example.diytomcat.catalina.Host
 * 父类或接口：
 * 描述：主机
 */
public class Host {
    /**
     * 描述：名字
     */
    private String name;
    /**
     * 描述：存放路径和Context 的映射
     */
    private Map<String, Context> contextMap;
    /**
     * 描述：引擎
     */
    private Engine engine;
    public Host(String name, Engine engine){
        this.contextMap = new HashMap<>();
        this.name =  name;
        this.engine = engine;

        scanContextsOnWebAppsFolder();
        scanContextsInServerXML();
        scanWarOnWebAppsFolder();//扫描webapps 目录，处理所有的 war 文件

        new WarFileWatcher(this).start();//开始监控 webapps 目录
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * 方法名：scanContextsInServerXML
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：通过 ServerXMLUtil 获取 context, 放进 contextMap里。
     */
    private  void scanContextsInServerXML() {
        List<Context> contexts = ServerXMLUtil.getContexts(this);
        for (Context context : contexts) {
            contextMap.put(context.getPath(), context);
        }
    }

    /**
     * 方法名：scanContextsOnWebAppsFolder
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：扫描 webapps 文件夹下的目录，对这些目录调用 loadContext 进行加载。
     */
    private  void scanContextsOnWebAppsFolder() {
        File[] folders = Constant.webappsFolder.listFiles();
        for (File folder : folders) {
            if (!folder.isDirectory())
                continue;
            loadContext(folder);
        }
    }

    /**
     * 方法名：loadContext
     * 传入参数：@param folder 文件夹
     * 异常类型：
     * 返回类型：
     * 描述：加载目录成为 Context 对象。
     */
    private  void loadContext(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path))//如果是 ROOT
            path = "/";//那么path 就是 "/"
        else
            path = "/" + path;//如果是 a, 那么path 就是 "/a"

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path,docBase,this, true);//根据 path 和 它们所处于的路径创建 Context 对象
        contextMap.put(context.getPath(), context);//保存进 contextMap
    }

    /**
     * 方法名：getContext
     * 传入参数：@param path 路径
     * 异常类型：
     * 返回类型：@return {@link Context }
     * 描述：通过 path 获取 Context 对象
     */
    public Context getContext(String path) {
        return contextMap.get(path);
    }

    /**
     * 方法名：reload
     * 传入参数：@param context 上下文
     * 异常类型：
     * 返回类型：
     * 描述：重新加载Context
     */
    public void reload(Context context) {
        LogFactory.get().info("Reloading Context with name [{}] has started", context.getPath());
        // 先保存 path, docBase, relodable 这些基本信息
        String path = context.getPath();
        String docBase = context.getDocBase();
        boolean reloadable = context.isReloadable();
        // stop暂停
        context.stop();
        // remove 把它从 contextMap 里删掉
        contextMap.remove(path);
        // allocate new context 根据刚刚保存的信息，创建一个新的context
        Context newContext = new Context(path, docBase, this, reloadable);
        // assign it to map 设置到 contextMap 里
        contextMap.put(newContext.getPath(), newContext);
        LogFactory.get().info("Reloading Context with name [{}] has completed", context.getPath());

    }

    /**
     * 方法名：load
     * 传入参数：@param folder 文件夹
     * 异常类型：
     * 返回类型：
     * 描述：把一个文件夹加载为Context
     */
    public void load(File folder) {
        String path = folder.getName();
        if ("ROOT".equals(path))
            path = "/";
        else
            path = "/" + path;

        String docBase = folder.getAbsolutePath();
        Context context = new Context(path, docBase, this, false);
        contextMap.put(context.getPath(), context);
    }

    /**
     * 方法名：loadWar
     * 传入参数：@param warFile war文件
     * 异常类型：
     * 返回类型：
     * 描述：把 war 文件解压为目录，并把文件夹加载为 Context
     */
    public void loadWar(File warFile) {
        String fileName =warFile.getName();
        String folderName = StrUtil.subBefore(fileName,".",true);
        //看看是否已经有对应的 Context了
        Context context= getContext("/"+folderName);
        if(null!=context)
            return;

        //先看是否已经有对应的文件夹
        File folder = new File(Constant.webappsFolder,folderName);
        if(folder.exists())
            return;

        //移动war文件，因为jar 命令只支持解压到当前目录下
        File tempWarFile = FileUtil.file(Constant.webappsFolder, folderName, fileName);
        File contextFolder = tempWarFile.getParentFile();
        contextFolder.mkdir();
        FileUtil.copyFile(warFile, tempWarFile);
        //解压
        String command = "jar xvf " + fileName;
//      System.out.println(command);
        Process p = RuntimeUtil.exec(null, contextFolder, command);
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //解压之后删除临时war
        tempWarFile.delete();
        //然后创建新的 Context
        load(contextFolder);
    }

    /**
     * 方法名：scanWarOnWebAppsFolder
     * 传入参数：
     * 异常类型：
     * 返回类型：
     * 描述：扫描webapps 目录，处理所有的 war 文件
     */
    private void scanWarOnWebAppsFolder() {
        File folder = FileUtil.file(Constant.webappsFolder);
        File[] files = folder.listFiles();
        for (File file : files) {
            if(!file.getName().toLowerCase().endsWith(".war"))
                continue;
            loadWar(file);
        }
    }

}