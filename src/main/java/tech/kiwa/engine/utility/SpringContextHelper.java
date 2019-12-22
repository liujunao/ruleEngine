package tech.kiwa.engine.utility;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
//import org.springframework.web.context.WebApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

//通过SpringContext获取Bean/Service等信息
@Service
public class SpringContextHelper implements ApplicationContextAware {
    private static SpringContextHelper thisInstance = new SpringContextHelper();
    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static SpringContextHelper getInstance() {
        if (null == thisInstance) {
            return null;
        }
        return thisInstance;
    }

    /***
     * 根据bean的id获取配置文件中相应的bean
     *
     * @param name  要取得的bean的名称
     * @return 返回service对象，或者是bean对象
     * @throws BeansException   bean找不到等异常。
     */
    public static Object getBean(String name) throws BeansException {
        return (applicationContext).getBean(name);
    }

    /***
     * 类似于getBean(String name)只是在参数中提供了需要返回到的类型
     *
     * @param <T> 参数类型的模板类
     * @param name 要取得的bean的名称
     * @param requiredType  要取得的bean的类型
     * @return 返回service对象，或者是bean对象
     * @throws BeansException  bean找不到等异常。
     */
    public static <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(name, requiredType);
    }

    /***
     * 类似于getBean(String name)只是在参数中提供了需要返回到的类型
     *
     * @param <T> 参数类型的模板类
     * @param requiredType   要取得的bean的类型
     * @return 返回service对象，或者是bean对象
     * @throws BeansException bean找不到等异常。
     */
    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }

    /***
     * 类似于getBean(String name)只是在参数中提供了需要返回到的类型
     *
     * @param name 要取得的bean的名称
     * @param args  要取得的bean的类型
     * @return 返回service对象，或者是bean对象
     * @throws BeansException  bean找不到等异常。
     */
    public static Object getBean(String name, Object... args) throws BeansException {
        return applicationContext.getBean(name, args);
    }

    /**
     * 如果BeanFactory包含与名称匹配的bean定义，则返回true
     *
     * @param name 是否包含bean的名称
     * @return boolean  true--包含， false -- 不包含
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    /**
     * 判断以给定名字注册的bean定义是一个singleton还是多个prototype
     * 如果与给定名字相应的bean定义没有被找到，将会抛出一个个异常（NoSuchBeanDefinitionException
     *
     * @param name 是否是单一类，bean的名称
     * @return boolean  true -- 是  false -- 否
     * @throws NoSuchBeanDefinitionException bean找不到等异常
     */
    public static boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.isSingleton(name);
    }

    /**
     * @param name Bean对象名称。
     * @return Class 注册对象的类
     * @throws NoSuchBeanDefinitionException bean找不到等异常
     */
    @SuppressWarnings("rawtypes")
    public static Class getType(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getType(name);
    }

    /**
     * 如果给定的bean名字在bean定义中有别名，则返回这些别名
     *
     * @param name bean的名称
     * @return bean的别名
     * @throws NoSuchBeanDefinitionException bean找不到等异常
     */
    public static String[] getAliases(String name) throws NoSuchBeanDefinitionException {
        return applicationContext.getAliases(name);
    }

    /**
     * 设置上下文
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHelper.applicationContext = applicationContext;
    }
}
