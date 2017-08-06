package com.io;

import com.google.gson.JsonArray;
import controller.DatabaseService;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by hoangnh on 04/08/2017.
 */
public class TestDbFree {
    public static void main(String[] args) {
        DatabaseService service = new DatabaseService();
//        String id = "1364621416988372";
//        if(service.checkOrder(id)){
//            System.out.println("Have");
//        }else {
//            service.order("1364621416988372");
//        }
//        service.getBooking();
//        JsonArray array = service.getUser();
//        System.out.println(array);
//        service.getBooking();
        JsonArray array = service.getUser();
        System.out.println(array);
    }
}
