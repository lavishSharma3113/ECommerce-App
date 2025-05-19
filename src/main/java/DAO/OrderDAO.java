package DAO;

import ECommerceDB.DatabaseManager;
import ECommerceObjects.CartItem;
import ECommerceObjects.Order;

import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {
    private final DatabaseManager dbManager;

    public OrderDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    public List<Order> fetchOrderHistory(int userId) {
        List<Order> orders = new ArrayList<>();

        String query = "SELECT order_id, total_amount, order_date FROM `Order` WHERE user_id = ? ORDER BY order_date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(new Order(
                            rs.getInt("order_id"),
                            rs.getDouble("total_amount"),
                            rs.getDate("order_date")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return orders;
    }

    public void displayOrderDetails(int orderId, PrintWriter out) {
        String query = "SELECT oi.quantity, oi.price, p.name FROM order_item oi JOIN product p ON oi.product_id = p.product_id WHERE oi.order_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                double total = 0;
                out.println("-------------------------------------------");
                out.println(String.format("%-25s %-10s %-10s %-10s", "Product", "Price", "Quantity", "Subtotal"));
                out.println("-------------------------------------------");

                while (rs.next()) {
                    String name = rs.getString("name");
                    double price = rs.getDouble("price");
                    int quantity = rs.getInt("quantity");
                    double subtotal = price * quantity;
                    total += subtotal;

                    out.println(String.format("%-25s $%-9.2f %-10d $%-10.2f",
                            name, price, quantity, subtotal));
                }

                out.println("-------------------------------------------");
                out.println(String.format("%-47s $%-10.2f", "TOTAL:", total));
                out.println("-------------------------------------------");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("Error retrieving order details.");
        }
    }

    public void checkoutOrder(int userId, List<CartItem> cartItems, double totalPrice) {
        try (Connection conn = dbManager.getConnection()) {
            conn.setAutoCommit(false);

            try {
                int orderId = insertOrder(conn, userId, totalPrice);
                for (CartItem item : cartItems) {
                    insertOrderItem(conn, orderId, item);
                    updateStock(conn, item);
                }
                conn.commit();
                System.out.println("Order placed successfully! Order ID: " + orderId);
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
                System.out.println("Transaction rolled back due to error.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int insertOrder(Connection conn, int userId, double totalPrice) throws SQLException {
        String query = "INSERT INTO `Order` (user_id, total_amount, order_date) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, userId);
            stmt.setDouble(2, totalPrice);
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
                throw new SQLException("Order ID not generated.");
            }
        }
    }

    private void insertOrderItem(Connection conn, int orderId, CartItem item) throws SQLException {
        String query = "INSERT INTO order_item (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, item.getProductId());
            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getPrice());
            stmt.executeUpdate();
        }
    }

    private void updateStock(Connection conn, CartItem item) throws SQLException {
        String query = "UPDATE product SET stock_quantity = stock_quantity - ? WHERE product_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, item.getQuantity());
            stmt.setInt(2, item.getProductId());
            stmt.executeUpdate();
        }
    }
}
