package org.cis.data.util;

import java.sql.*;

public class DbServers {

	private static Connection conn = null;
	private static PreparedStatement st = null;
	private static ResultSet rs = null;
	
	public static ResultSet executeQuery(String sql) {
		if(conn == null){
			conn = DbConnection.getConnection();
		}
		try {
			st = conn.prepareStatement(sql);
			rs = st.executeQuery();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}

	public static int executeUpdate(String sql) {
		if(conn == null){
			conn = DbConnection.getConnection();
		}
		int result = 0;
		try {
			st = conn.prepareStatement(sql);
			result = st.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void close() {
        try {
            if (rs != null) {
                rs.close();
            }
            if (st != null) {
                st.close();
            }
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
