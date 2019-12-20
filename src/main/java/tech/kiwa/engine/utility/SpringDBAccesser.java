package tech.kiwa.engine.utility;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import tech.kiwa.engine.framework.DBAccesser;

//通过SpringContext获取数据库连接
@Service
public class SpringDBAccesser implements ApplicationContextAware, DBAccesser {
    private static ApplicationContext applicationContext = null;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public Connection getConnection() {
        DataSource ds = (DataSource) applicationContext.getBean("dataSource");
        try {
            return ds.getConnection();
        } catch (SQLException e) {

            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringDBAccesser.applicationContext = applicationContext;
    }
}
