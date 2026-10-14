package com.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static String DBName = "smevcs";
    public static String DBUSER = "root";
    public static String DBPASSWORD = "";
    public static String DBPORT = "3306"; // change to 3307 if your XAMPP MySQL port is modified

    private static void loadDriver() throws ClassNotFoundException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            Class.forName("com.mysql.jdbc.Driver");
        }
    }

    public static Connection getConnection() {
        try {
            loadDriver();

            String url = "jdbc:mysql://127.0.0.1:" + DBPORT + "/" + DBName
                    + "?useSSL=false&serverTimezone=Asia/Kolkata&allowPublicKeyRetrieval=true";

            Connection con = DriverManager.getConnection(url, DBUSER, DBPASSWORD);
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