/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sharpschnitzel.routinejdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

/**
 *
 * @author SharpSchnitzel
 */
public class DataHandler {
    
    private static DataSource datasource;
    private static String defaultTable;
    
    private static void handleException(SQLException e) {
        System.out.println(e.getSQLState());
        System.out.println(e);
        switch (e.getSQLState()) {
            case "08001" -> System.out.println("Unable to connect.");
            case "42P01" -> System.out.println("Specified table does not exist.");
            case "42703" -> System.out.println("Wrong column name.");
            case "23502" -> System.out.println("An attempt to assign a null value to a column with a not-null constraint.");
            
            //...........
            default -> System.out.println("May your admin help you with that.");
        }
    }
    
    private static Connection getConnection() {
        try {
            return datasource.getConnection();
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }
    
    public static void setDataSource(String url, String usr, String pwd) {
        PGSimpleDataSource dts = new PGSimpleDataSource();
        dts.setUrl(url);
        dts.setUser(usr);
        dts.setPassword(pwd);
        datasource = dts;
    }
    
    public static void setTable(String tbl) {
        if (validateTableName(tbl)) {
            defaultTable = tbl.toLowerCase();
        }
    }
    
    private static boolean validateTableName(String tbl) {
        if (tbl.length() > 31 || !tbl.matches("^(_|[a-zA-Z])\\w*$")) {
            System.out.println("Invalid table name.");
            return false;
        } else {
            return true;
        }
    }
    
    public static void processRow(Map<String, String> data) {
        if (defaultTable == null) {
            System.out.println("Default table is not specified. Either set default table or provide suitable table name for this method.");
        } else {
            processRow(defaultTable, data);
        }
    }
    
    public static void processRow(String table, Map<String, String> data) {
        if (data.isEmpty()) {
            System.out.println("Attributes map is empty");
            return;
        }
        if (!validateTableName(table)) return;
        
        Connection conn = getConnection();
        if (conn == null) return;
        
        String a = ",?".repeat(data.size()).substring(1);
        String[] filtered = data.keySet().stream().filter(key -> {
            return key != null && !key.isBlank() && data.get(key) != null && !key.isBlank();
        }).toArray(size -> new String[size]);
        String sql = "insert into " + table.toLowerCase() + " (" + String.join(",", filtered) + ") VALUES (" + ",?".repeat(filtered.length).substring(1) +")";
        
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < filtered.length; i++) {
                stmt.setString(i + 1, data.get(filtered[i]));
            }
            
            int processed = stmt.executeUpdate();
            if (processed > 0) {
                System.out.println("A new row has been inserted into the table " + table);
            } else {
                System.out.println("No rows were affected.");
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }
    
    public static void processRow(int index, Map<String, String> data) {
        if (defaultTable == null) {
            System.out.println("Default table is not specified. Either set default table or provide suitable table name for this method.");
        } else {
            processRow(defaultTable, index, data);
        }
    }
    
    public static void processRow(String table, int index, Map<String, String> data) {
        if (index < 1) {
            System.out.println("Invalid index supplied.");
            return;
        }
        if (data.isEmpty()) {
            System.out.println("Attributes map is empty");
            return;
        }
        if (!validateTableName(table)) return;
        
        Connection conn = getConnection();
        if (conn == null) return;

        String[] filtered = data.keySet().stream().filter(key -> {
            return key != null && !key.isBlank() && !key.equals("id");
        }).toArray(size -> new String[size]);
        
        String sql = "update " + table.toLowerCase() + " set " +
        Arrays.stream(filtered).map(key -> {return key +" = ?";}).collect(Collectors.joining(",")) + " where id = " + index;
        
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < filtered.length; i++) {
                if (data.get(filtered[i]) == null || data.get(filtered[i]).isBlank()) {
                    stmt.setNull(i+1, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(i + 1, data.get(filtered[i]));
                }
            }
            
            int processed = stmt.executeUpdate();
            
            if (processed > 0) {
                System.out.println("A row with id " + index + " within table " + table + " has been updated.");
            } else {
                System.out.println("No rows were affected.");
            }
        } catch (SQLException e) {
            handleException(e);
        }
        
    }
    
    public static void removeRow(int index) {
        if (defaultTable == null) {
            System.out.println("Default table is not specified. Either set default table or provide suitable table name for this method.");
        } else {
            removeRow(defaultTable, index);
        }
    }
    
    public static void removeRow(String table, int index) {
        if (index < 1) {
            System.out.println("Invalid index supplied.");
            return;
        }
        if (!validateTableName(table)) return;
        
        Connection conn = getConnection();
        if (conn == null) return;
        
        String sql = "delete from " + table.toLowerCase() + " where id = " + index;
        
        try (conn; PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            int processed = stmt.executeUpdate();
            
            if (processed > 0) {
                System.out.println("A row with id " + index + " within table " + table + " has been deleted.");
            } else {
                System.out.println("No rows were affected.");
            }
        } catch (SQLException e) {
            handleException(e);
        }
    }
    
    public static Map<String, String> getRow(int index) {
        if (defaultTable == null) {
            System.out.println("Default table is not specified. Either set default table or provide suitable table name for this method.");
            return null;
        } else {
            return getRow(defaultTable, index);
        }
    }
    
    public static Map<String, String> getRow(String table, int index) {
        if (index < 1) {
            System.out.println("Invalid index supplied.");
            return null;
        }
        if (!validateTableName(table)) return null;

        Connection conn = getConnection();
        if (conn == null) return null;

        String sql = "select * from " + table + " where id = " + index;

        try (conn; PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            ResultSetMetaData meta = rs.getMetaData();
            int columns = meta.getColumnCount();

            if (rs.next()) {
                Map<String, String> result = new HashMap<>();

                for(int i=1; i<=columns; ++i){           
                    result.put(meta.getColumnName(i),rs.getString(i));
                }
                return result;
            } else {
                System.out.println("No data found.");
                return null;
            }
        } catch (SQLException e) {
            handleException(e);
            return null;
        }
    }
    
    
    
    
    
}
