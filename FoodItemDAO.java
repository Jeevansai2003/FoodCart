package com.foodapp.dao;
import com.foodapp.db.DatabaseConnection;
import com.foodapp.model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
	public class FoodItemDAO {
	    public List<FoodItem> getAllFoodItems() {
	        List<FoodItem> items = new ArrayList<>();
	        String sql = "SELECT id, name, price, category FROM food_items";

	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                FoodItem item = new FoodItem(
	                    rs.getInt("id"),
	                    rs.getString("name"),
	                    rs.getDouble("price"),
	                    rs.getString("category")
	                );
	                items.add(item);
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	        return items;
	    }

	    public void addFoodItem(String name, double price, String category) {
	        String sql = "INSERT INTO food_items (name, price, category) VALUES (?, ?, ?)";
	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, name);
	            stmt.setDouble(2, price);
	            stmt.setString(3, category);
	            stmt.executeUpdate();
	            System.out.println("Food item added: " + name);

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	    public void deleteFoodItem(String name) {
	        String sql = "DELETE FROM food_items WHERE name = ?";
	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setString(1, name);
	            stmt.executeUpdate();
	            System.out.println("Deleted food item: " + name);
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }
	}
