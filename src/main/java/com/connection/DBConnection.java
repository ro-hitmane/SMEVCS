package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static String DBName = "smevcs";
    public static String DBUSER = "root";
    public static String DBPASSWORD = "";
    public static String DBPORT = "3306"; // change to 3306 if your MySQL uses the default port

    private static void loadDriver() throws ClassNotFoundException {
        try {
            // Connector/J 8 and newer
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // Connector/J 5.x
            Class.forName("com.mysql.jdbc.Driver");
        }
    }

    public static Connection getConnection() {
        try {
            loadDriver();

            String url = "jdbc:mysql://localhost:" + DBPORT + "/" + DBName
                    + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            Connection con = DriverManager.getConnection(url, DBUSER, DBPASSWORD);
            System.out.println("Database connected successfully!");
            return con;

        } catch (ClassNotFoundException e) {
            System.out.println("MySQL driver not found. Put the mysql-connector JAR in WEB-INF/lib.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}