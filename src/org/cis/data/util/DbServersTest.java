package org.cis.data.util;

import org.junit.Test;

import java.sql.ResultSet;

import static org.junit.Assert.*;

public class DbServersTest {
    @Test
    public void executeQuery() throws Exception {
        ResultSet rs = DbServers.executeQuery("select * from results");
        while (rs.next()) {
            System.out.println(rs.getDouble(1) + " " + rs.getDouble(2) + " " + rs.getDouble(3) + " " + rs.getDouble(4));
        }
        DbServers.close();
        while (rs.next()) {
            System.out.println(rs.getDouble(1) + " " + rs.getDouble(2) + " " + rs.getDouble(3) + " " + rs.getDouble(4));
        }
    }

    @Test
    public void executeUpdate() throws Exception {
        DbServers.executeUpdate("insert into results values (0.01, 0.07, 0.8815, 0.2450)");
        DbServers.close();
    }

}