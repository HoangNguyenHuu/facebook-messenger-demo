package controller;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Date;
import java.util.*;

/**
 * Created by hoangnh on 05/08/2017.
 */
public class DatabaseService {
    Logger logger = LoggerFactory.getLogger(DatabaseService.class);

    public void order(String facebook_id) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());

            boolean status = checkOrder(facebook_id);
            if (status == false) {
                String query = " insert into booking (facebook_id, date)"
                        + " values (?, ?)";

                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, facebook_id);
                preparedStmt.setDate(2, currentDate);

                preparedStmt.execute();
            }

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void cancel(String facebook_id) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());

            boolean status = checkOrder(facebook_id);
            if (status == true) {
                String query = "delete from booking where facebook_id = ? and date= ?";
                PreparedStatement preparedStmt = connection.prepareStatement(query);
                preparedStmt.setString(1, facebook_id);
                preparedStmt.setDate(2, currentDate);

                preparedStmt.execute();
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void changeMode(String facebook_id, boolean auto) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());

            String query = "update User set auto = ? where facebook_id = ?";
            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setBoolean(1, auto);
            preparedStmt.setString(2, facebook_id);
            // execute the java preparedstatement
            preparedStmt.executeUpdate();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getBooking() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Statement statement = connection.createStatement();

            ResultSet rs = statement.executeQuery("SELECT * FROM booking");
            if (!rs.first()) {
                System.out.println("No Record");
            } else {
                do {
                    String facebook_id = rs.getString("facebook_id");
                    java.util.Date date = rs.getDate("date");
                    System.out.println(facebook_id + ", " + date);
                } while (rs.next());
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public JsonArray getUser() {
        JsonArray totalArr = new JsonArray();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery("SELECT * FROM User");
            if (resultSet.first()) {
                do {
                    String name = resultSet.getString("name");
                    String id = resultSet.getString("facebook_id");
                    boolean auto = resultSet.getBoolean("auto");
                    JsonObject object = new JsonObject();
                    object.addProperty("name", name);
                    object.addProperty("facebook_id", id);
                    object.addProperty("auto", auto);
                    totalArr.add(object);
                } while (resultSet.next());
            }
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalArr;
    }

    public JsonArray getBookingToday() {
        JsonArray totalArr = new JsonArray();
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());
            logger.info("date: " + currentDate);

            String query = "SELECT * FROM booking WHERE date = ?";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setDate(1, currentDate);

            ResultSet rs = preparedStmt.executeQuery();
            if (rs.first()) {
                do {
                    String facebook_id = rs.getString("facebook_id");
                    logger.info("Facebook id: " + facebook_id);
                    String query2 = "SELECT * FROM User WHERE facebook_id = ?";
                    PreparedStatement preparedStmt2 = connection.prepareStatement(query2);
                    preparedStmt2.setString(1, facebook_id);
                    ResultSet resultSet = preparedStmt2.executeQuery();
                    if (resultSet.first()) {
                        String name = resultSet.getString("name");
                        String id = resultSet.getString("facebook_id");
                        boolean auto = resultSet.getBoolean("auto");
                        JsonObject object = new JsonObject();
                        object.addProperty("name", name);
                        object.addProperty("facebook_id", facebook_id);
                        object.addProperty("auto", auto);
                        totalArr.add(object);
                    }
                } while (rs.next());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return totalArr;
    }

    public boolean checkOrder(String facebook_id) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());

            String query = "SELECT * FROM booking WHERE facebook_id = ? AND date = ?";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setString(1, facebook_id);
            preparedStmt.setDate(2, currentDate);

            ResultSet rs = preparedStmt.executeQuery();
            if (rs.first()) {
                connection.close();
                return true;
            } else {
                connection.close();
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public void setNewOrderToday() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://sql12.freemysqlhosting.net:3306/sql12188867", "sql12188867", "53hym2glgy");
            Calendar calendar = Calendar.getInstance();
            java.sql.Date currentDate = new java.sql.Date(calendar.getTime().getTime());

            String query = "SELECT * FROM User WHERE auto = ? ";

            PreparedStatement preparedStmt = connection.prepareStatement(query);
            preparedStmt.setBoolean(1, true);
            ResultSet rs = preparedStmt.executeQuery();
            if (rs.first()) {
                do {
                    String facebook_id = rs.getString("facebook_id");
                    order(facebook_id);
                } while (rs.next());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }
}
