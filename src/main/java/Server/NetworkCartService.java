package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ECommerceDB.DatabaseManager;
import ECommerceDB.SQLQueryManager;
import Session.UserSession;
import ECommerceObjects.CartItem;

public class NetworkCartService {
    private final DatabaseManager dbManager;
    private final UserSession session;
    private final PrintWriter out;
    private final SQLQueryManager queryManager;

    public NetworkCartService(DatabaseManager dbManager, UserSession session, PrintWriter out) {
        this.dbManager = dbManager;
        this.session = session;
        this.out = out;
        this.queryManager = new SQLQueryManager();
    }

    public void viewCart(BufferedReader in, NetworkOrderService orderService) throws IOException {
        List<CartItem> cartItems = getCartItems();
        displayCart(cartItems);
        if (cartItems.isEmpty()) return;
        handleCartOptions(in, orderService, cartItems);
    }

    private List<CartItem> getCartItems() {
        List<CartItem> cartItems = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.cart_id, p.product_id, p.name, p.price, c.quantity " +
                             "FROM cart c JOIN product p ON c.product_id = p.product_id " +
                             "WHERE c.user_id = ?")) {
            stmt.setInt(1, session.getUserId());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cartItems.add(new CartItem(
                            rs.getInt("cart_id"),
                            rs.getInt("product_id"),
                            rs.getString("name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity")));
                }
            }
        } catch (SQLException e) {
            out.println("Error retrieving cart from database.");
        }
        return cartItems;
    }

    private void displayCart(List<CartItem> cartItems) {
        out.println("\n=== Your Shopping Cart ===");
        double totalPrice = 0;
        out.println("-------------------------------------------");
        out.printf("%-5s %-20s %-10s %-10s %-10s%n", "No.", "Product", "Price", "Quantity", "Subtotal");
        out.println("-------------------------------------------");
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            double subtotal = item.getPrice() * item.getQuantity();
            totalPrice += subtotal;
            out.printf("%-5d %-20s $%-9.2f %-10d $%-10.2f%n", (i + 1), item.getName(), item.getPrice(), item.getQuantity(), subtotal);
        }
        out.println("-------------------------------------------");
        out.printf("%-36s $%-10.2f%n", "TOTAL:", totalPrice);
        out.println("-------------------------------------------");
    }

    private void handleCartOptions(BufferedReader in, NetworkOrderService orderService, List<CartItem> cartItems) throws IOException {
        out.println("\n1. Checkout");
        out.println("2. Remove Item");
        out.println("3. Return to Main Menu");
        out.println("\nSelect an option: ");
        String choice = in.readLine();
        switch (choice) {
            case "1":
                orderService.checkout(cartItems, cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum());
                break;
            case "2":
                removeFromCart(in, cartItems);
                break;
        }
    }

    public void addToCart(int productId, String productName, int quantity) {
        try (Connection conn = dbManager.getConnection()) {
            if (updateExistingCartItem(conn, productId, quantity)) return;
            insertNewCartItem(conn, productId, quantity);
            out.println("Added to cart: " + productName + " x" + quantity);
        } catch (SQLException e) {
            out.println("Error adding item to cart.");
        }
    }

    private boolean updateExistingCartItem(Connection conn, int productId, int quantity) throws SQLException {
        try (PreparedStatement checkStmt = conn.prepareStatement(
                "SELECT quantity FROM cart WHERE user_id = ? AND product_id = ?")) {
            checkStmt.setInt(1, session.getUserId());
            checkStmt.setInt(2, productId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    int newQuantity = rs.getInt("quantity") + quantity;
                    try (PreparedStatement updateStmt = conn.prepareStatement(
                            "UPDATE cart SET quantity = ? WHERE user_id = ? AND product_id = ?")) {
                        updateStmt.setInt(1, newQuantity);
                        updateStmt.setInt(2, session.getUserId());
                        updateStmt.setInt(3, productId);
                        updateStmt.executeUpdate();
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private void insertNewCartItem(Connection conn, int productId, int quantity) throws SQLException {

        try (PreparedStatement insertStmt = conn.prepareStatement(
                "INSERT INTO cart (user_id, product_id, quantity) VALUES (?, ?, ?)")) {
            insertStmt.setInt(1, session.getUserId());
            insertStmt.setInt(2, productId);
            insertStmt.setInt(3, quantity);
            insertStmt.executeUpdate();
        }
    }

    private void removeFromCart(BufferedReader in, List<CartItem> cartItems) throws IOException {
        out.println("Enter item number to remove: ");
        try {
            int itemIndex = Integer.parseInt(in.readLine()) - 1;
            if (itemIndex >= 0 && itemIndex < cartItems.size()) {
                executeRemoveCartItem(cartItems.get(itemIndex));
            } else {
                out.println("Invalid item number.");
            }
        } catch (NumberFormatException e) {
            out.println("Please enter a valid number.");
        }
    }

    private void executeRemoveCartItem(CartItem selectedItem) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM cart WHERE cart_id = ? AND user_id = ?")) {
            stmt.setInt(1, selectedItem.getCartId());
            stmt.setInt(2, session.getUserId());
            stmt.executeUpdate();
            out.println("Removed from cart: " + selectedItem.getName());
        } catch (SQLException e) {
            out.println("Error removing item from cart.");
        }
    }

    public void clearCart() {
        String query = queryManager.getQuery("QUERY_CLEAR_CART");
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, session.getUserId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            out.println("Error clearing cart.");
        }
    }
}
