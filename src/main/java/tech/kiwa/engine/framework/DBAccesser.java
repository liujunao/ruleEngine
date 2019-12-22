package tech.kiwa.engine.framework;

import java.sql.Connection;

//实现该接口，实现自定义连接
public interface DBAccesser {
    Connection getConnection();
}
