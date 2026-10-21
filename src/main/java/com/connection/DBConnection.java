package com.connection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DBConnection {

    private static final Object LOCK = new Object();
    private static volatile HikariDataSource dataSource;

    private DBConnection() {
    }

    /**
     * Returns a pooled connection, or null if the database cannot be reached
     * (same contract as before, so callers keep their null checks).
     * Callers must close the connection to return it to the pool.
     */
    public static Connection getConnection() {
        try {
            return pool().getConnection();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        } catch (RuntimeException e) {
            // bad configuration, missing driver, pool already shut down
            System.out.println("Database pool is not available: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /** Closes the pool. Called when the web application stops. */
    public static void shutdown() {
        synchronized (LOCK) {
            if (dataSource != null) {
                dataSource.close();
                dataSource = null;
            }
        }
    }

    private static HikariDataSource pool() {
        HikariDataSource ds = dataSource;
        if (ds == null) {
            synchronized (LOCK) {
                ds = dataSource;
                if (ds == null) {
                    ds = createPool();
                    dataSource = ds;
                }
            }
        }
        return ds;
    }

    private static HikariDataSource createPool() {
        Properties file = loadPropertiesFile();

        String user = setting(file, "DB_USER", "db.user", null);
        if (user == null || user.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Database user is not configured. Set the DB_USER environment variable "
                    + "or db.user in db.properties.");
        }
        String password = setting(file, "DB_PASSWORD", "db.password", "");

        String url = setting(file, "DB_URL", "db.url", null);
        if (url == null || url.trim().isEmpty()) {
            String host = setting(file, "DB_HOST", "db.host", "127.0.0.1");
            String port = setting(file, "DB_PORT", "db.port", "3306");
            String name = setting(file, "DB_NAME", "db.name", "smevcs");
            String params = setting(file, "DB_PARAMS", "db.params",
                    "useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true");
            url = "jdbc:mysql://" + host + ":" + port + "/" + name + (params.isEmpty() ? "" : "?" + params);
        }

        HikariConfig config = new HikariConfig();
        config.setPoolName("smevcs-pool");
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);

        config.setMaximumPoolSize(intSetting(file, "DB_POOL_MAX_SIZE", "db.pool.maxSize", 10));
        config.setMinimumIdle(intSetting(file, "DB_POOL_MIN_IDLE", "db.pool.minIdle", 2));
        config.setConnectionTimeout(intSetting(file, "DB_POOL_CONNECTION_TIMEOUT_MS",
                "db.pool.connectionTimeoutMs", 10000));
        // Retire connections before MySQL's own wait_timeout (8 hours) can drop them.
        config.setMaxLifetime(25L * 60 * 1000);
        // Warn in the log if a connection is held for over a minute (a sign it was never closed).
        config.setLeakDetectionThreshold(60_000);
        // Start even if MySQL is down; connections are retried as callers ask for them.
        config.setInitializationFailTimeout(-1);

        // Recommended MySQL driver tuning for pooled use.
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        return new HikariDataSource(config);
    }

    private static String setting(Properties file, String envName, String propName, String fallback) {
        String value = System.getenv(envName);
        if (value == null) {
            value = file.getProperty(propName);
        }
        if (value == null) {
            return fallback;
        }
        return value.trim();
    }

    private static int intSetting(Properties file, String envName, String propName, int fallback) {
        String value = setting(file, envName, propName, null);
        if (value == null || value.isEmpty()) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Setting " + envName + " / " + propName
                    + " must be a whole number, but was: " + value);
        }
    }

    private static Properties loadPropertiesFile() {
        Properties props = new Properties();
        String external = System.getenv("DB_CONFIG_FILE");
        if (external == null || external.trim().isEmpty()) {
            external = System.getProperty("db.config.file");
        }
        try {
            if (external != null && !external.trim().isEmpty()) {
                try (InputStream in = Files.newInputStream(Paths.get(external.trim()))) {
                    props.load(in);
                }
            } else {
                ClassLoader cl = DBConnection.class.getClassLoader();
                try (InputStream in = (cl == null) ? null : cl.getResourceAsStream("db.properties")) {
                    if (in != null) {
                        props.load(in);
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read the database settings file: " + e.getMessage(), e);
        }
        return props;
    }
}