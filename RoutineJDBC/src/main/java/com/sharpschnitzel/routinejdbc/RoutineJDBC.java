/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sharpschnitzel.routinejdbc;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author SharpSchnitzel
 */
public class RoutineJDBC {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        DataHandler.setDataSource("jdbc:postgresql://localhost:5432/postgres", "postgres", "postgres");
        
        Map<String, String> data = new HashMap<>();
        
        data.put("last_name", "Some");
        data.put("first_name", "Specific");
        data.put("middle_name", "Data");
        
        DataHandler.setTable("table_one");
        
        DataHandler.processRow(data);
        data = DataHandler.getRow(13);
        if (data == null) {
            System.out.println("Null happens.");
        } else {
            System.out.println("------------------------------");
            data.forEach((key, value)->{
                System.out.println(key + " - " + value);
            });
            System.out.println("------------------------------");
            int id = Integer.valueOf(data.get("id"));
            data.replace("last_name", "Modified Data");
            DataHandler.processRow(id, data);
            DataHandler.removeRow(id);
        }
        
//          A new row has been inserted into the table table_one
//          ------------------------------
//          last_name - Some
//          id - 13
//          middle_name - Data
//          first_name - Specific
//          ------------------------------
//          A row with id 13 within table table_one has been updated.
//          A row with id 13 within table table_one has been deleted.
        
//        create table table_one (
//            id serial primary key,
//            last_name varchar not null,
//            first_name varchar,
//            middle_name varchar
//        );
//
//        create table table_two (
//            id serial primary key,
//            mark varchar,
//            model varchar not null,
//            vin varchar
//        );
    }
}
