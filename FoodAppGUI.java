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

    // --- ADD FOOD FORM ---
    private JTextField nameField;
    private JTextField priceField;
    private JTextField categoryField;
    private JButton addButton;

    // --- CATEGORY TABS ---
    private JTabbedPane categoryTabs;
    private DefaultListModel<FoodItem> sweetsModel;
    private DefaultListModel<FoodItem> dailyFoodModel;
    private JPanel sweetsPanel;
    private JPanel dailyFoodPanel;

    // --- STORE DIRECT REFERENCES TO JLISTS ---
    private JList<FoodItem> sweetsJList;
    private JList<FoodItem> dailyFoodJList;

    // --- SEARCH BAR ---
    private JTextField searchField;

    // --- ORDER CART ---
    private JList<OrderedItem> orderList;
    private DefaultListModel<OrderedItem> orderModel;
    private JButton addButtonToOrder;
    private JButton removeButtonFromOrder;

    // --- QUANTITY SELECTOR ---
    private JSpinner qtySpinner;

    // --- ORDER SUMMARY (SMALLEST!) ---
    private JTextArea orderDisplay;

    // --- BUTTONS ---
    private JButton orderButton;
    private JButton analyzeButton;

    // --- ORDER MANAGEMENT ---
    private List<OrderedItem> selectedItems;

    public FoodAppGUI() {
        setTitle("FoodCart Pro");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
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

        // --- CATEGORY MODELS AND PANELS ---
        sweetsModel = new DefaultListModel<>();
        dailyFoodModel = new DefaultListModel<>();

        // Create JLists and store references
        sweetsJList = new JList<>(sweetsModel);
        dailyFoodJList = new JList<>(dailyFoodModel);

        // Wrap each JList in a JScrollPane with custom scroll buttons
        sweetsPanel = createScrollablePanel(sweetsJList, "Sweets");
        dailyFoodPanel = createScrollablePanel(dailyFoodJList, "Daily Food");

        // Create tabbed pane
        categoryTabs = new JTabbedPane();
        categoryTabs.addTab("Sweets", sweetsPanel);
        categoryTabs.addTab("Daily Food", dailyFoodPanel);

        // --- SEARCH BAR ---
        searchField = new JTextField(15);
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterFoodItems(searchField.getText()); }
        });

        // --- ORDER CART ---
        orderModel = new DefaultListModel<>();
        orderList = new JList<>(orderModel);
        orderList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // --- CART CONTROL BUTTONS + QUANTITY ---
        addButtonToOrder = new JButton("+ Add to Order");
        removeButtonFromOrder = new JButton("- Remove from Order");
        qtySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 9999, 1));
        qtySpinner.setPreferredSize(new Dimension(60, 25));

        // --- ORDER SUMMARY (TINY!) ---
        orderDisplay = new JTextArea(2, 30); // ← Only 2 rows!
        orderDisplay.setEditable(false);
        orderDisplay.setFont(new Font("Arial", Font.PLAIN, 11));
        orderDisplay.setBackground(Color.LIGHT_GRAY);

        // --- MAIN ACTION BUTTONS ---
        orderButton = new JButton("Place Order");
        analyzeButton = new JButton("Analyze Monthly Sales");
    }

    // 👇 CUSTOM METHOD: Creates a panel with scroll arrows above/below list
    private JPanel createScrollablePanel(JList<FoodItem> list, String title) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        // Scroll buttons
        JButton upButton = new JButton("▲");
        JButton downButton = new JButton("▼");
        upButton.setPreferredSize(new Dimension(40, 25));
        downButton.setPreferredSize(new Dimension(40, 25));

        upButton.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index > 0) list.setSelectedIndex(index - 1);
        });

        downButton.addActionListener(e -> {
            int index = list.getSelectedIndex();
            if (index < list.getModel().getSize() - 1) list.setSelectedIndex(index + 1);
        });

        // Wrap list in scroll pane
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Hide default scrollbar
        scrollPane.setPreferredSize(new Dimension(200, 250)); // Fixed height

        // Layout: Up button | Scroll area | Down button
        JPanel content = new JPanel(new BorderLayout(0, 5));
        content.add(upButton, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(downButton, BorderLayout.SOUTH);

        panel.add(content, BorderLayout.CENTER);
        return panel;
    }

    private void layoutComponents() {
        setLayout(new BorderLayout());

        // --- TOP: Add New Food Item ---
        JPanel addPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        addPanel.setBorder(BorderFactory.createTitledBorder("Add New Food Item"));
        addPanel.add(new JLabel("Food Name:")); addPanel.add(nameField);
        addPanel.add(new JLabel("Price:")); addPanel.add(priceField);
        addPanel.add(new JLabel("Category:")); addPanel.add(categoryField);
        addPanel.add(new JLabel()); addPanel.add(addButton);

        add(addPanel, BorderLayout.NORTH);

        // --- CENTER: Available Food + Cart ---
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // === LEFT: Available Food Items ===
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2; gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.6; gbc.weighty = 0.7;

        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Available Food Items"));

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        leftPanel.add(searchPanel, BorderLayout.NORTH);

        // Tabbed pane
        leftPanel.add(categoryTabs, BorderLayout.CENTER);

        centerPanel.add(leftPanel, gbc);

        // === MIDDLE: Cart Controls ===
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2; gbc.gridheight = 1;
        gbc.weighty = 0.1;
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("Cart Actions"));
        controlPanel.add(new JLabel("Qty:"));
        controlPanel.add(qtySpinner);
        controlPanel.add(addButtonToOrder);
        controlPanel.add(removeButtonFromOrder);

        centerPanel.add(controlPanel, gbc);

        // === RIGHT: Your Order (BIG!) ===
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridwidth = 1; gbc.gridheight = 2;
        gbc.weightx = 0.4; gbc.weighty = 0.9;
        gbc.fill = GridBagConstraints.BOTH;
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Your Order"));
        rightPanel.add(new JScrollPane(orderList), BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(300, 380)); // Big and clear!

        centerPanel.add(rightPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // --- BOTTOM: Action Buttons ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.add(orderButton);
        bottomPanel.add(analyzeButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // --- RIGHT PANEL: Order Summary (TINY!) ---
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Order Summary"));
        summaryPanel.add(new JScrollPane(orderDisplay), BorderLayout.CENTER);
        summaryPanel.setPreferredSize(new Dimension(250, 55)); // ← Super compact!
        add(summaryPanel, BorderLayout.EAST);
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

        // Clear all models
        sweetsModel.clear();
        dailyFoodModel.clear();

        // Sort into categories
        for (FoodItem item : items) {
            String category = item.getCategory().toLowerCase().trim();
            if (category.contains("sweet") || category.contains("dessert")) {
                sweetsModel.addElement(item);
            } else {
                dailyFoodModel.addElement(item);
            }
        }
    }

    private void filterFoodItems(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadFoodItems(); // Reset to full list
            return;
        }

        String lowerQuery = query.toLowerCase().trim();

        // Clear all models
        sweetsModel.clear();
        dailyFoodModel.clear();

        // Re-filter from database
        FoodItemDAO dao = new FoodItemDAO();
        List<FoodItem> allItems = dao.getAllFoodItems();

        for (FoodItem item : allItems) {
            if (item.getName().toLowerCase().contains(lowerQuery)) {
                String category = item.getCategory().toLowerCase().trim();
                if (category.contains("sweet") || category.contains("dessert")) {
                    sweetsModel.addElement(item);
                } else {
                    dailyFoodModel.addElement(item);
                }
            }
        }
    }

    private void addToOrder() {
        int selectedIndex = -1;
        JList<FoodItem> activeList = null;

        switch (categoryTabs.getSelectedIndex()) {
            case 0: // Sweets
                activeList = sweetsJList;
                break;
            case 1: // Daily Food
                activeList = dailyFoodJList;
                break;
            default:
                JOptionPane.showMessageDialog(this, "Please select a food item first!");
                return;
        }

        selectedIndex = activeList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select a food item first!");
            return;
        }

        FoodItem foodItem = activeList.getSelectedValue();
        int quantity = (Integer) qtySpinner.getValue();

        OrderedItem orderedItem = new OrderedItem(foodItem, quantity);
        orderModel.addElement(orderedItem);

        // Optional: Deselect after adding
        activeList.clearSelection();
    }

    private void removeFromOrder() {
        int selectedIndex = orderList.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item in your order to remove!");
            return;
        }

        orderModel.remove(selectedIndex);
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

        List<OrderedItem> selectedItems = new ArrayList<>();
        for (int i = 0; i < orderModel.size(); i++) {
            selectedItems.add(orderModel.getElementAt(i));
        }

        OrderDAO dao = new OrderDAO();
        dao.placeOrder(selectedItems);

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

        // Clear cart
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FoodAppGUI());
    }
}
