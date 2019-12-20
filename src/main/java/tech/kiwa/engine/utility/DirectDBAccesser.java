package tech.kiwa.engine.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Properties;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.filter.config.ConfigTools;
import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.druid.util.StringUtils;

import tech.kiwa.engine.framework.DBAccesser;

public class DirectDBAccesser implements DBAccesser {
    private static Logger log = LoggerFactory.getLogger(DirectDBAccesser.class);

    private static boolean UseDruid = true;
    private static ArrayList<Connection> connList = new ArrayList<Connection>(); //只用了一个连接， 没有用到连接池
    private static volatile DataSource dataSource = null;

    public static boolean isUseDruid() {
        return UseDruid;
    }

    public static void setUseDruid(boolean useDruid) {
        UseDruid = useDruid;
    }

    /**
     * 根据类型获取数据源.  非线程安全的函数
     *
     * @return druid或者dbcp数据源
     * @throws Exception the exception
     */
    public static final DataSource getDataSource() throws Exception {
        Properties prop = PropertyUtil.loadPropertyFile("druid.properties");
        if (prop == null) {
            throw new Exception("druid.properties file load error.");
        }
        String password = prop.getProperty("password");
        String publicKey = prop.getProperty("publickey");

        if (!StringUtils.isEmpty(publicKey)) {
            password = ConfigTools.decrypt(publicKey, password);
            prop.setProperty("password", password);
        }
        dataSource = DruidDataSourceFactory.createDataSource(prop);

        return dataSource;
    }

    private Connection openConnection() {
        Connection conn = null;

        String driver = PropertyUtil.getProperty("jdbc.driver");        // "oracle.jdbc.driver.OracleDriver";
        String url = PropertyUtil.getProperty("jdbc.url");                //"jdbc:Oracle:thin:@localhost:1521:orcl";
        String userName = PropertyUtil.getProperty("jdbc.username");
        String password = PropertyUtil.getProperty("jdbc.password");
        String publicKey = PropertyUtil.getProperty("jdbc.publickey");
        if (!StringUtils.isEmpty(publicKey)) {
            try {
                password = ConfigTools.decrypt(publicKey, password);
                Class.forName(driver);
                DriverManager.setLoginTimeout(30000);
                conn = DriverManager.getConnection(url, userName, password);
            } catch (ClassNotFoundException e) {
                log.debug(e.getMessage());
            } catch (Exception e) {
                log.debug(e.getMessage());
            }
        }
        if (null != conn) {
            connList.add(conn);
        }
        return conn;
    }

    public Connection getConnection() {
        if (UseDruid) {
            try {
                if (dataSource == null) {
                    synchronized (DataSource.class) {
                        if (dataSource == null) {
                            dataSource = getDataSource();
                        }
                    }
                }
                return dataSource.getConnection();
            } catch (SQLException e) {
                log.error(e.getMessage());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
        //如果是直接连接,或者是druid读取失败的情况下。
        for (Connection conn : connList) {
            try {
                if (!conn.isClosed()) {
                    return conn;
                }
            } catch (SQLException e) {
                log.debug(e.getMessage());
            }
        }
        //if the proper connection is not found , create a new one.
        return openConnection();
    }

    //@SuppressWarnings("unused")
    public void closeConnection(Connection conn) {
        if (UseDruid) {
            try {
                conn.close();
            } catch (SQLException e) {
                log.debug(e.getMessage());
            }
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                if (connList.contains(conn)) {
                    synchronized (connList) {
                        connList.remove(conn);
                    }
                }
            }
        } catch (SQLException e) {
            log.debug(e.getMessage());
        }
    }
}
