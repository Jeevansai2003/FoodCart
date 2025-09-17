package com.foodapp.ui;

import com.foodapp.dao.FoodItemDAO;
import com.foodapp.dao.OrderDAO;
import com.foodapp.model.FoodItem;
import com.foodapp.model.Order;
import com.foodapp.model.OrderedItem;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class FoodAppGUI extends JFrame implements ActionListener {

    // --- INPUT FIELDS FOR ADDING FOOD ---
    private JTextField nameField;
    private JTextField priceField;
    private JTextField categoryField;
    private JButton addButton;
   
 // --- ORDER CART ---
    private JList<OrderedItem> orderList;           // Shows items in cart
    private DefaultListModel<OrderedItem> orderModel; // Model for cart
    private JButton addButtonToOrder;               // "+" button
    private JButton removeButtonFromOrder;          // "-" button

    // --- FOOD LIST AND SEARCH ---
    private JList<FoodItem> foodList;
    private DefaultListModel<FoodItem> listModel;
    private JTextField searchField;

    // --- QUANTITY SELECTOR ---
    private JSpinner qtySpinner;

    // --- ORDER SUMMARY ---
    private JTextArea orderDisplay;

    // --- BUTTONS ---
    private JButton orderButton;
    private JButton analyzeButton;

    // --- ORDER MANAGEMENT ---
    private List<OrderedItem> selectedItems;

    public FoodAppGUI() {
        setTitle("Food Order Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        selectedItems = new ArrayList<>();

        initializeComponents();
        layoutComponents();
        setupListeners();
        loadFoodItems();

        setVisible(true);
    }

    private void initializeComponents() {
        // --- ADD FOOD FORM ---
        nameField = new JTextField(15);
        priceField = new JTextField(10);
        categoryField = new JTextField(10);
        addButton = new JButton("Add Food Item");

        // --- SEARCH BAR ---
        searchField = new JTextField(20);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
        });

        // --- AVAILABLE FOOD LIST ---
        listModel = new DefaultListModel<>();
        foodList = new JList<>(listModel);
        foodList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Single select for clarity
        JScrollPane foodScroll = new JScrollPane(foodList);
        foodScroll.setPreferredSize(new Dimension(250, 300)); // Big box!

        // --- ORDER CART LIST ---
        orderModel = new DefaultListModel<>();
        orderList = new JList<>(orderModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane orderScroll = new JScrollPane(orderList);
        orderScroll.setPreferredSize(new Dimension(250, 300)); // Big box!

        // --- QUANTITY SPINNER ---
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setPreferredSize(new Dimension(80, 25));

        // --- CART CONTROL BUTTONS ---
        addButtonToOrder = new JButton("+ Add to Order");
        removeButtonFromOrder = new JButton("- Remove from Order");

        // --- ORDER SUMMARY ---
        orderDisplay = new JTextArea(10, 30);
        orderDisplay.setEditable(false);

        // --- MAIN ACTION BUTTONS ---
        orderButton = new JButton("Place Order");
        analyzeButton = new JButton("Analyze Monthly Sales");
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // --- TOP PANEL: Add Food Form ---
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Add New Food Item"));
        inputPanel.add(new JLabel("Food Name:")); inputPanel.add(nameField);
        inputPanel.add(new JLabel("Price:")); inputPanel.add(priceField);
        inputPanel.add(new JLabel("Category:")); inputPanel.add(categoryField);
        inputPanel.add(new JLabel()); inputPanel.add(addButton);

        // --- CENTER PANEL: Available Food + Cart ---
        JPanel centerPanel = new JPanel(new BorderLayout(20, 10));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // LEFT SIDE: Available Food Items
        JPanel availablePanel = new JPanel(new BorderLayout());
        availablePanel.setBorder(BorderFactory.createTitledBorder("Available Food Items"));
        availablePanel.add(new JLabel("Search:"), BorderLayout.NORTH);
        availablePanel.add(searchField, BorderLayout.CENTER);
        availablePanel.add(new JScrollPane(foodList), BorderLayout.CENTER);
        availablePanel.add(qtySpinner, BorderLayout.SOUTH);

        // MIDDLE CONTROLS: Add/Remove buttons
        JPanel controlPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        controlPanel.add(addButtonToOrder);
        controlPanel.add(removeButtonFromOrder);

        // RIGHT SIDE: Your Order (Cart)
        JPanel orderPanel = new JPanel(new BorderLayout());
        orderPanel.setBorder(BorderFactory.createTitledBorder("Your Order"));
        orderPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);

        // Put left + middle + right together
        JPanel foodAndCartPanel = new JPanel(new BorderLayout(10, 0));
        foodAndCartPanel.add(availablePanel, BorderLayout.WEST);
        foodAndCartPanel.add(controlPanel, BorderLayout.CENTER);
        foodAndCartPanel.add(orderPanel, BorderLayout.EAST);

        centerPanel.add(inputPanel, BorderLayout.NORTH);
        centerPanel.add(foodAndCartPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // --- RIGHT PANEL: Order Summary ---
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Order Summary"));
        rightPanel.add(new JScrollPane(orderDisplay), BorderLayout.CENTER);

        // Bottom action buttons
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(orderButton);
        bottomPanel.add(analyzeButton);
        rightPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(rightPanel, BorderLayout.EAST);
    }
    private void setupListeners() {
        addButton.addActionListener(this);
        orderButton.addActionListener(this);
        analyzeButton.addActionListener(this);
        addButtonToOrder.addActionListener(this);
        removeButtonFromOrder.addActionListener(this);
    }

    private void loadFoodItems() {
        FoodItemDAO dao = new FoodItemDAO();
        List<FoodItem> items = dao.getAllFoodItems();
        listModel.clear();
        for (FoodItem item : items) {
            listModel.addElement(item);
        }
    }
    private void addToOrder() {
        int selectedIndex = foodList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item first!");
            return;
        }

        FoodItem foodItem = listModel.getElementAt(selectedIndex);
        int quantity = (Integer) qtySpinner.getValue();

        OrderedItem orderedItem = new OrderedItem(foodItem, quantity);
        orderModel.addElement(orderedItem); // Adds to cart list

        // Optional: Clear selection after adding
        foodList.clearSelection();
    }
    private void removeFromOrder() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item in your order to remove!");
            return;
        }

        orderModel.remove(selectedIndex); // Removes from cart
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == addButton) {
            addFoodItem();
        } else if (e.getSource() == addButtonToOrder) {
            addToOrder();
        } else if (e.getSource() == removeButtonFromOrder) {
            removeFromOrder();
        } else if (e.getSource() == orderButton) {
            placeOrder();
        } else if (e.getSource() == analyzeButton) {
            analyzeMonthlySales();
        }
    }

    private void addFoodItem() {
        String name = nameField.getText().trim();
        String priceStr = priceField.getText().trim();
        String category = categoryField.getText().trim();

        if (name.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and Price are required!");
            return;
        }

        try {
            double price = Double.parseDouble(priceStr);
            FoodItemDAO dao = new FoodItemDAO();
            dao.addFoodItem(name, price, category);

            // Refresh list
            loadFoodItems();

            // Clear fields
            nameField.setText("");
            priceField.setText("");
            categoryField.setText("");

            JOptionPane.showMessageDialog(this, "Food item added successfully!");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid price format!");
        }
    }

    private void placeOrder() {
        if (orderModel.size() == 0) {
            JOptionPane.showMessageDialog(this, "Your order is empty! Add some items first.");
            return;
        }

        // Convert cart model to list for DAO
        List<OrderedItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < orderModel.size(); i++) {
            selectedItems.add(orderModel.getElementAt(i));
        }

        // Place order in database
        OrderDAO dao = new OrderDAO();
        dao.placeOrder(selectedItems);

        // Show order summary
        StringBuilder sb = new StringBuilder();
        sb.append("✅ ORDER PLACED!\n");
        sb.append("===================\n");
        for (OrderedItem item : selectedItems) {
            sb.append(item).append("\n");
        }
        double total = selectedItems.stream()
            .mapToDouble(OrderedItem::getTotalPrice)
            .sum();
        sb.append("===================\n");
        sb.append("TOTAL: ").append(String.format("%.2f", total)).append("\n");
        sb.append("Date: ").append(java.time.LocalDate.now());

        orderDisplay.setText(sb.toString());

        // Clear cart after placing order
        orderModel.clear();
    }

    private void analyzeMonthlySales() {
        OrderDAO dao = new OrderDAO();
        List<Order> monthlySales = dao.getMonthlySalesAnalysis();

        StringBuilder sb = new StringBuilder();
        sb.append("📊 MONTHLY SALES ANALYSIS\n");
        sb.append("=========================\n");

        if (monthlySales.isEmpty()) {
            sb.append("No sales recorded yet.");
        } else {
            for (Order order : monthlySales) {
                String monthName = order.getOrderDate().getMonth().toString();
                sb.append(monthName).append(" ")
                  .append(order.getOrderDate().getYear())
                  .append(": ").append(String.format("%.2f", order.getTotalAmount()))
                  .append("\n");
            }
        }

        orderDisplay.setText(sb.toString());
    }

    private void filterFoodItems(String query) {
        List<FoodItem> allItems = new FoodItemDAO().getAllFoodItems();
        listModel.clear();
        

        if (query == null || query.trim().isEmpty()) {
            for (FoodItem item : allItems) {
                listModel.addElement(item);
            }
        } else {
            String lowerQuery = query.toLowerCase();
            for (FoodItem item : allItems) {
                if (item.getName().toLowerCase().contains(lowerQuery)) {
                    listModel.addElement(item);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodAppGUI());
    }
}