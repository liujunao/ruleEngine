package tech.kiwa.engine.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {
    private static Logger log = LoggerFactory.getLogger(PropertyUtil.class);

    /**
     * 获取 key 属性值
     *
     * @param key
     * @return
     */
    public static String getProperty(String key) {
        String value = directGetProperty(key); //从 ruleEngine.properties 或其他配置文件获取 key 的属性值
        if (StringUtils.isNotEmpty(value)) {
            value = value.trim();
            //${} 格式化的数据，那么取括号里面的内容
            if (value.startsWith("${") && value.endsWith("}")) {
                value = value.substring(2, value.length() - 1);
                //获取 ${} 格式化中的数据
                value = directGetProperty(value);
                if (StringUtils.isNotEmpty(value)) {
                    value = value.trim();
                }
            }
        }
        return value;
    }

    public static Properties loadPropertyFile(String fileName) {
        Properties prop = null;
        if (fileName.contains(File.separator)) {
            try {
                File file = new File(fileName);
                prop = new Properties();
                InputStream fisResource = new FileInputStream(file);
                prop.load(fisResource);
                fisResource.close();
            } catch (FileNotFoundException e) {
                log.debug(e.getMessage());
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        } else {
            try {
                File dir = new File(PropertyUtil.class.getClassLoader().getResource("").getPath());
                File file = new File(dir.getAbsolutePath() + File.separator + fileName);
                prop = new Properties();
                InputStream fisResource = new FileInputStream(file);
                prop.load(fisResource);
                fisResource.close();
            } catch (FileNotFoundException e) {
                log.debug(e.getMessage());
            } catch (IOException e) {
                log.debug(e.getMessage());
            }
        }
        if (prop != null) {
            Set<Object> keySet = (Set<Object>) prop.keySet();
            for (Object key : keySet) {
                //如果key不是字符串格式的，那么跳过
                if (key instanceof String) {
                    String value = prop.getProperty((String) key);
                    //value 存在的情况下
                    if (null != value) {
                        value = value.trim();
                        //如果是变量形式的值
                        if (value.startsWith("${") && value.endsWith("}")) {
                            //那么再读取一遍
                            value = value.substring(2, value.length() - 1);
                            value = directGetProperty(value);
                            if (StringUtils.isNotEmpty(value)) {
                                value = value.trim();
                                prop.setProperty((String) key, value);
                            }
                        }
                    }
                }
            }
        }
        return prop;
    }

    //申明为全局静态变量的作用？？？
    private static Properties ruleEngineBuffer = new Properties();

    /**
     * 从 ruleEngine.properties 或其他 .properties 配置文件或系统缓存中获取 key 的属性值
     *
     * @param key
     * @return
     */
    private static String directGetProperty(String key) {
        if (!ruleEngineBuffer.isEmpty()) { //检查是否已经加载过
            return ruleEngineBuffer.getProperty(key);
        }
        final String ruleEngineFile = "ruleEngine.properties"; //配置文件
        String value = null;
        try {
            InputStream fisResource = PropertyUtil.class.getClassLoader().getResourceAsStream(ruleEngineFile);
            if (fisResource != null) {
                Properties prop = new Properties();
                prop.load(fisResource); //加载 ruleEngine.properties 文件
                fisResource.close();
                //放入 Entity 中，具体作用？？？
                ruleEngineBuffer.putAll(prop);
                value = prop.getProperty(key); //获取指定 key 的值
                if (null != value) { //value 不为空，就返回，否则执行下面的步骤
                    return value;
                }
            }
            //从其他 properties 文件中读取：xxx/RuleEngine/target/classes/ (绝对路径)
            File dir = new File(PropertyUtil.class.getClassLoader().getResource("").getPath());
            File[] propertyFiles = dir.listFiles(); //列出target/classes/ 下的文件名或文件夹名
            for (int iLoop = 0; iLoop < propertyFiles.length; iLoop++) {
                String propFile = propertyFiles[iLoop].getPath().toLowerCase();
                if (propFile.endsWith(".properties")) {
                    FileInputStream fis = new FileInputStream(propFile);
                    Properties prop = new Properties();
                    prop.load(fis);
                    fisResource.close();
                    //放入 Entity 中，具体作用？？？
                    ruleEngineBuffer.putAll(prop);
                    value = prop.getProperty(key);
                    if (null != value) { //value 不为空，就返回，否则执行下面的步骤
                        return value;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            log.debug(e.getMessage());
        } catch (IOException e) {
            log.debug(e.getMessage());
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
        // read the value from system memory
        if (null == value) {
            value = System.getProperty(key);
        }
        return value;
    }
}
