package tech.kiwa.engine.framework;

import java.sql.Connection;

public interface DBAccesser {
    Connection getConnection();
}
