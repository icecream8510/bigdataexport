package com.boco.eoms.base.poiutil;



import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

/**
 * 读取jdbc配置文件
 * @author FYK<br/> 2018年10月25日
 * @version 1.0
 * @since JDK：1.6
 */
public class JDBCPropertiesHelper {
    private static final transient Logger log = Logger.getLogger(JDBCPropertiesHelper.class);

    /**
     * 常用配置属性文件名称.
     */
    private final static String PROPERTIES_FILENAME = "database.properties";
    /**
     * 配置属性对象静态化
     */
    private static Properties properties;

    public static Properties getProperties() {
        if (properties == null) {
            synchronized (JDBCPropertiesHelper.class) {
                if (properties == null) {
                    loadProperties(PROPERTIES_FILENAME);
                }
            }
        }
        return properties;
    }

    /**
     * 根据key获取配置的字符串value值
     * 
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        if (key == null) {
            return null;
        }
        // 读取snaker.properties文件，获取key对应的值
        return getProperties().getProperty(key);
    }

    /**
     * 根据key获取配置的数字value值
     * 
     * @param key
     * @return
     */
    public static int getNumerProperty(String key) {
        String value = getProperties().getProperty(key);
        if (NumberUtils.isNumber(value)) {
            return Integer.parseInt(value);
        } else {
            return 0;
        }
    }

    public static void loadProperties(Properties props) {
        properties = props;
    }

    /**
     * 根据指定的文件名称，从类路径中加载属性文件，构造Properties对象
     * 
     * @param filename
     *            属性文件名称
     */
    public static void loadProperties(String filename) {
        InputStream in = null;
        // 当前正在执行的线程对象的引用，然后根据该引用返回该线程的上下文 ClassLoader，可用来动态加载jar包或者资源文件
        ClassLoader threadContextClassLoader = Thread.currentThread().getContextClassLoader();
        properties = new Properties();
        if (threadContextClassLoader != null) {
            // 读取资源的输入流，如果无法找到资源，则返回 null
            in = threadContextClassLoader.getResourceAsStream(filename);
        }
        if (in == null) {
            in = JDBCPropertiesHelper.class.getResourceAsStream(filename);
            if (in == null) {
                log.warn("No properties file found in the classpath by filename " + filename);
            }
        } else {
            try {
                // 从输入流中读取属性列表（键和元素对）。
                properties.load(in);
                if (log.isInfoEnabled()) {
                    log.info("Properties read " + properties);
                }
            } catch (Exception e) {
                log.error("Error reading from " + filename, e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn("IOException while closing InputStream: " + e.getMessage());
                }
            }
        }
    }
}
