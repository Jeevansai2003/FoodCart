package com.foodapp.model;


import java.time.LocalDate;
import java.util.List;

public class Order {

	    private int id;
	    private LocalDate orderDate;
	    private double totalAmount;
	    private List<OrderedItem> items;// items in this order

	    public Order(int id, LocalDate orderDate, double totalAmount,  List<OrderedItem> items) {
	        this.id = id;
	        this.orderDate = orderDate;
	        this.totalAmount = totalAmount;
	        this.items = items;
	    }

		// Getters
	    public int getId() { return id; }
	    public LocalDate getOrderDate() { return orderDate; }
	    public double getTotalAmount() { return totalAmount; }
	    public List<OrderedItem> getItems() { return items; }
	    
	    public String toString() {
	        return "Order{id=" + id +
	               ", date=" + orderDate +
	               ", total=" + String.format("%.2f", totalAmount) +
	               ", items=" + (items != null ? items.size() : "null") + '}';
	}
}
