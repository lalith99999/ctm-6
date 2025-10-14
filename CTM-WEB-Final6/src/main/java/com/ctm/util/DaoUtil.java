package com.ctm.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import oracle.jdbc.driver.OracleDriver;

public class DaoUtil {

    private static final String URL = "jdbc:oracle:thin:@//localhost:1521/xe";
    private static final String USER = "system";
    private static final String PASS = "Hyd9678";

    public static Connection getMyConnection() {
        try {
            DriverManager.registerDriver(new OracleDriver());
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Statement getMyStatement() {
        try {
            Connection con = getMyConnection();
            return con != null ? con.createStatement() : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static PreparedStatement getMyPreparedStatement(String sql) {
        try {
            Connection con = getMyConnection();
            return con != null ? con.prepareStatement(sql) : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
