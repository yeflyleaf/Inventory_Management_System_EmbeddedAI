package com.example.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DBDumper {
    public static void main(String[] args) {
        System.out.println("--- DUMPING SYSTEM SETTINGS ---");
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://127.0.0.1:3306/demo2?useSSL=false&allowPublicKeyRetrieval=true&connectTimeout=5000",
                "user1", "user1"
            );
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT setting_key, setting_value FROM system_setting");
            while (rs.next()) {
                System.out.println(rs.getString("setting_key") + " => " + rs.getString("setting_value"));
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("--- DUMP END ---");
    }
}
