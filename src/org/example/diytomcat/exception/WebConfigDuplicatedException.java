package org.example.diytomcat.exception;
//准备个自定义异常，在配置 web.xml 里面发生 servlet 重复配置的时候会抛出。

/**
 * 作者：cjy
 * 类名：WebConfigDuplicatedException
 * 全路径类名：org.example.diytomcat.exception.WebConfigDuplicatedException
 * 父类或接口：@see Exception
 * 描述：在配置 web.xml 里面发生 servlet 重复配置的时候会抛出。
 */
public class WebConfigDuplicatedException extends Exception {
    public WebConfigDuplicatedException(String msg) {
        super(msg);
    }
}