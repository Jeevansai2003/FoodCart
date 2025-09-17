package com.foodapp.dao;
import com.foodapp.db.DatabaseConnection;
import com.foodapp.model.FoodItem;
import com.foodapp.model.Order;
import com.foodapp.model.OrderedItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.*;
public class OrderDAO {
	
	public void placeOrder(List<OrderedItem> selectedItems) {
	    double total = selectedItems.stream()
	        .mapToDouble(OrderedItem::getTotalPrice)
	        .sum();

	    String orderSql = "INSERT INTO orders (order_date, total_amount) VALUES (?, ?)";
	    String orderItemSql = "INSERT INTO order_items (order_id, food_item_id, quantity) VALUES (?, ?, ?)";

	    try (Connection conn = DatabaseConnection.getConnection()) {
	        conn.setAutoCommit(false);

	        // Insert order
	        try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS)) {
	            orderStmt.setDate(1, java.sql.Date.valueOf(java.time.LocalDate.now()));
	            orderStmt.setDouble(2, total);
	            orderStmt.executeUpdate();

	            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
	            int orderId = -1;
	            if (generatedKeys.next()) {
	                orderId = generatedKeys.getInt(1);
	            }

	            // Insert order items with quantity
	            try (PreparedStatement itemStmt = conn.prepareStatement(orderItemSql)) {
	                for (OrderedItem orderedItem : selectedItems) {
	                    itemStmt.setInt(1, orderId);
	                    itemStmt.setInt(2, orderedItem.getFoodItem().getId()); // Get ID from FoodItem inside OrderedItem
	                    itemStmt.setInt(3, orderedItem.getQuantity());         // Use actual quantity!
	                    itemStmt.addBatch();
	                }
	                itemStmt.executeBatch();
	            }

	            conn.commit();
	            System.out.println("Order placed successfully! Total: $" + total);
	        }
	    } catch (SQLException e) {
	        System.err.println("Error placing order: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	    public List<Order> getMonthlySalesAnalysis() {
	        List<Order> monthlySales = new ArrayList<>();

	        String sql = "SELECT " +
	                "YEAR(order_date) as year, " +
	                "MONTH(order_date) as month, " +
	                "SUM(total_amount) as monthly_total " +
	                "FROM orders " +
	                "GROUP BY YEAR(order_date), MONTH(order_date) " +
	                "ORDER BY year DESC, month DESC";
	        try (Connection conn = DatabaseConnection.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql);
	             ResultSet rs = stmt.executeQuery()) {

	            while (rs.next()) {
	                int year = rs.getInt("year");
	                int month = rs.getInt("month");
	                double total = rs.getDouble("monthly_total");

	                // Format month name
	                String monthName = new java.text.SimpleDateFormat("MMMM").format(
	                    new java.util.Date(year - 1900, month - 1, 1));

	                Order order = new Order(0, LocalDate.of(year, month, 1), total, new ArrayList<>());
	                monthlySales.add(order);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

	        return monthlySales;
	    }
	}

