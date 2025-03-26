package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ECommerceDB.DatabaseManager;
import Session.UserSession;
import ECommerceObjects.CartItem;
import ECommerceObjects.Order;

public class NetworkOrderService {
    private final DatabaseManager dbManager;
    private final UserSession session;
    private final PrintWriter out;
    private final BufferedReader in;

    public NetworkOrderService(DatabaseManager dbManager, UserSession session, PrintWriter out, BufferedReader in) {
        this.dbManager = dbManager;
        this.session = session;
        this.out = out;
        this.in = in;
    }

    public void viewOrderHistory() throws IOException {
        out.println("\n=== Your Order History ===");
        List<Order> orders = fetchOrderHistory();

        if (orders.isEmpty()) {
            out.println("You have no orders yet.");
            return;
        }

        displayOrderHistoryMenu(orders);
    }

    private List<Order> fetchOrderHistory() {
        List<Order> orders = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT order_id, total_amount, order_date FROM `Order` WHERE user_id = ? ORDER BY order_date DESC")) {

            stmt.setInt(1, session.getUserId());

            try (ResultSet rs = stmt.executeQuery()) {
                printOrderHistoryHeader();

                int index = 1;
                while (rs.next()) {
                    int orderId = rs.getInt("order_id");
                    double amount = rs.getDouble("total_amount");
                    Date date = rs.getDate("order_date");

                    Order order = new Order(orderId, amount, date);
                    orders.add(order);

                    printOrderSummary(index++, order);
                }

                out.println("-------------------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Error retrieving order history from database.");
        }

        return orders;
    }

    private void printOrderHistoryHeader() {
        out.println("-------------------------------------------");
        out.println(String.format("%-5s %-15s %-15s %-10s", "No.", "Order ID", "Date", "Amount"));
        out.println("-------------------------------------------");
    }

    private void printOrderSummary(int index, Order order) {
        out.println(String.format("%-5d %-15d %-15s $%-10.2f",
                index, order.getId(), order.getOrderDate().toString(), order.getTotalAmount()));
    }

    private void displayOrderHistoryMenu(List<Order> orders) throws IOException {
        out.println("\n1. View Order Details");
        out.println("2. Return to Main Menu");
        out.println("\nSelect an option: ");

        String choice = in.readLine();

        if (choice.equals("1") && !orders.isEmpty()) {
            handleOrderDetailSelection(orders);
        }
    }

    private void handleOrderDetailSelection(List<Order> orders) throws IOException {
        out.println("Enter order number to view details: ");
        try {
            int orderIndex = Integer.parseInt(in.readLine()) - 1;

            if (orderIndex >= 0 && orderIndex < orders.size()) {
                viewOrderDetails(orders.get(orderIndex).getId());
            } else {
                out.println("Invalid order number.");
            }
        } catch (NumberFormatException e) {
            out.println("Please enter a valid number.");
        }
    }

    private void viewOrderDetails(int orderId) throws IOException {
        out.println("\n=== Order Details (Order ID: " + orderId + ") ===");

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = prepareOrderDetailsStatement(conn, orderId);
             ResultSet rs = stmt.executeQuery()) {

            printOrderDetailsHeader();
            double total = printOrderItems(rs);
            printOrderTotal(total);

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Error retrieving order details from database.");
        }
    }

    private PreparedStatement prepareOrderDetailsStatement(Connection conn, int orderId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(
                "SELECT oi.quantity, oi.price, p.name " +
                        "FROM order_item oi JOIN product p ON oi.product_id = p.product_id " +
                        "WHERE oi.order_id = ?");
        stmt.setInt(1, orderId);
        return stmt;
    }

    private void printOrderDetailsHeader() {
        out.println("-------------------------------------------");
        out.println(String.format("%-25s %-10s %-10s %-10s", "Product", "Price", "Quantity", "Subtotal"));
        out.println("-------------------------------------------");
    }

    private double printOrderItems(ResultSet rs) throws SQLException {
        double total = 0;

        while (rs.next()) {
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            int quantity = rs.getInt("quantity");
            double subtotal = price * quantity;

            total += subtotal;

            out.println(String.format("%-25s $%-9.2f %-10d $%-10.2f",
                    name, price, quantity, subtotal));
        }

        return total;
    }

    private void printOrderTotal(double total) {
        out.println("-------------------------------------------");
        out.println(String.format("%-47s $%-10.2f", "TOTAL:", total));
        out.println("-------------------------------------------");
    }

    public void checkout(List<CartItem> cartItems, double totalPrice) throws IOException {
        out.println("\n=== Checkout ===");
        out.println("Total amount: $" + String.format("%.2f", totalPrice));
        out.println("Confirm order (y/n): ");

        String confirm = in.readLine();

        if (confirm.equalsIgnoreCase("y")) {
            processOrder(cartItems, totalPrice);
        } else {
            out.println("Order canceled.");
        }
    }

    private void processOrder(List<CartItem> cartItems, double totalPrice) {
        try (Connection conn = dbManager.getConnection()) {
            // Disable auto-commit for transaction
            conn.setAutoCommit(false);

            try {
                // Create order and process items
                int orderId = createOrder(conn, totalPrice);
                processOrderItems(conn, orderId, cartItems);
                clearUserCart();

                // Commit transaction
                conn.commit();
                out.println("Order placed successfully! Order ID: " + orderId);

            } catch (SQLException e) {
                // Rollback transaction on error
                conn.rollback();
                e.printStackTrace();
                out.println("Error processing order. Transaction rolled back.");
            } finally {
                // Restore auto-commit
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Database error during checkout.");
        }
    }

    private void processOrderItems(Connection conn, int orderId, List<CartItem> cartItems) throws SQLException {
        for (CartItem item : cartItems) {
            createOrderItem(conn, orderId, item);
            updateProductStock(conn, item.getProductId(), item.getQuantity());
        }
    }

    private void clearUserCart() {
        NetworkCartService cartService = new NetworkCartService(dbManager, session, out);
        cartService.clearCart();
    }

    private int createOrder(Connection conn, double totalPrice) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO `Order` (user_id, total_amount, order_date) VALUES (?, ?, NOW())",
                Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, session.getUserId());
            stmt.setDouble(2, totalPrice);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    private void createOrderItem(Connection conn, int orderId, CartItem item) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO order_item (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)")) {

            stmt.setInt(1, orderId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getPrice());
            stmt.executeUpdate();
        }
    }

    private void updateProductStock(Connection conn, int productId, int quantity) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "UPDATE product SET stock_quantity = stock_quantity - ? WHERE product_id = ?")) {

            stmt.setInt(1, quantity);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }
}