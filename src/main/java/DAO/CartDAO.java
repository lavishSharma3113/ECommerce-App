package DAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import ECommerceObjects.CartItem;

public class CartDAO {
    private final DatabaseManager dbManager;
    private final SQLQueryManager queryManager;

    public CartDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        this.queryManager = new SQLQueryManager();
    }

    public List<CartItem> getCartItems(int userId) {
        List<CartItem> items = new ArrayList<>();
        String query = "SELECT c.cart_id, p.product_id, p.name, p.price, c.quantity " +
                "FROM cart c JOIN product p ON c.product_id = p.product_id WHERE c.user_id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                items.add(new CartItem(
                        rs.getInt("cart_id"),
                        rs.getInt("product_id"),
                        rs.getString("name"),
                        rs.getDouble("price"),
                        rs.getInt("quantity")));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving cart items: " + e.getMessage());
        }

        return items;
    }

    public boolean updateCartItem(int userId, int productId, int quantity) {
        String checkQuery = "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, userId);
            checkStmt.setInt(2, productId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                int newQuantity = rs.getInt("quantity") + quantity;
                try (PreparedStatement updateStmt = conn.prepareStatement(
                        "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?")) {
                    updateStmt.setInt(1, newQuantity);
                    updateStmt.setInt(2, userId);
                    updateStmt.setInt(3, productId);
                    updateStmt.executeUpdate();
                }
                return true;
            }
        } catch (SQLException e) {
            System.out.println("Error updating cart item: " + e.getMessage());
        }
        return false;
    }

    public void insertCartItem(int userId, int productId, int quantity) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)")) {
            stmt.setInt(1, userId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting cart item: " + e.getMessage());
        }
    }

    public void removeCartItem(int userId, int cartId) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ? AND user_id = ?")) {
            stmt.setInt(1, cartId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing cart item: " + e.getMessage());
        }
    }

    public void clearCart(int userId) {
        String query = queryManager.getQuery("QUERY_CLEAR_CART");
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error clearing cart: " + e.getMessage());
        }
    }
}

