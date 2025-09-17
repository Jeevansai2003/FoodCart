package com.foodapp.model;

public class OrderedItem {
	private FoodItem foodItem;
    private int quantity;

    public OrderedItem(FoodItem foodItem, int quantity) {
        this.foodItem = foodItem;
        this.quantity = quantity;
    }

    public FoodItem getFoodItem() { return foodItem; }
    public int getQuantity() { return quantity; }
    public double getTotalPrice() { return foodItem.getPrice() * quantity; }

    @Override
    public String toString() {
        return quantity + "x " + foodItem.getName() + " (" + String.format("%.2f", getTotalPrice()) + ")";
    }
}


