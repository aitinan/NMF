package org.cis.data.util;

import javax.naming.Context;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


class DbConnection {

	static {
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	static Connection getConnection() {
		Connection conn = null;
		String dbUrl = "jdbc:mysql://localhost/rcfnmf";
		String user = "root";
		String pwd = "mysql";
		try {
			conn = DriverManager.getConnection(dbUrl, user, pwd);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}
}
