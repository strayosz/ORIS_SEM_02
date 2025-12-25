package org.example.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static DataSource dataSource;

    public static void init() throws ClassNotFoundException {
        Class.forName("org.postgresql.Driver");

        Properties properties = new Properties();

        try(InputStream inputStream = DBConnection.class.getClassLoader()
                .getResourceAsStream("config/application.properties")){
            properties.load(inputStream);

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(properties.getProperty("database.url"));
            config.setUsername(properties.getProperty("database.username"));
            config.setPassword(properties.getProperty("database.password"));
            dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        if (dataSource != null) {
            return dataSource.getConnection();
        } else {
            try {
                init();
                return dataSource.getConnection();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void destroy() {
        ((HikariDataSource) dataSource).close();
    }
}
