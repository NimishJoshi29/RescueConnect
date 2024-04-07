package com.example.rescueconnect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLConnection {

    Connection connection;

    Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        connection = DriverManager.getConnection("jdbc:mysql:sql6.freesqldatabase.com:3306/sql6695806","sql6695806","RKQQGBl47q");
        return connection;
    }
}
