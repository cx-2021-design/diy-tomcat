package org.example.diytomcat.classloader;

import cn.hutool.core.io.FileUtil;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * 作者：cjy
 * 类名：WebappClassLoader
 * 全路径类名：org.example.diytomcat.classloader.WebappClassLoader
 * 父类或接口：@see URLClassLoader
 * 描述：应用类加载器
 * 注意，因为是目录，所以加的时候，要在结尾跟上 "/" , URLClassLoader 才会把它当作目录来处理
 */
public class WebappClassLoader extends URLClassLoader {

    public WebappClassLoader(String docBase, ClassLoader commonClassLoader) {
        super(new URL[] {}, commonClassLoader);

        try {
            File webinfFolder = new File(docBase, "WEB-INF");
            //1. 扫描 Context 对应的 docBase 下的 classes 和 lib
            File classesFolder = new File(webinfFolder, "classes");
            File libFolder = new File(webinfFolder, "lib");
            //2. 把 jar 通过 addURL 加进去
            URL url;
            url = new URL("file:" + classesFolder.getAbsolutePath() + "/");
            this.addURL(url);
            List<File> jarFiles = FileUtil.loopFiles(libFolder);
            //把 classes 目录，通过 addURL 加进去。
            for (File file : jarFiles) {
                url = new URL("file:" + file.getAbsolutePath());
                this.addURL(url);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
