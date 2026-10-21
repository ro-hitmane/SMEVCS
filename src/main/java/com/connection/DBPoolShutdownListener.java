package com.connection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Closes the connection pool when the web application stops or is redeployed,
 * so its threads and connections do not linger in Tomcat.
 */
@WebListener
public class DBPoolShutdownListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // The pool is created on first use.
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        DBConnection.shutdown();
        // Stop the MySQL driver's background thread so Tomcat can unload the app cleanly.
        try {
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread")
                    .getMethod("uncheckedShutdown").invoke(null);
        } catch (Throwable ignored) {
            // not present in older drivers; nothing to stop
        }
    }
}