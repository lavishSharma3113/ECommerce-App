package UI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import DAO.CartDAO;
import ECommerceObjects.CartItem;

public class CartUI {
    private final PrintWriter out;

    public CartUI(PrintWriter out) {
        this.out = out;
    }

    public void displayCart(List<CartItem> cartItems) {
        out.println("\n=== Your Shopping Cart ===");
        if (cartItems.isEmpty()) {
            out.println("Your cart is empty.");
            return;
        }

        double totalPrice = 0;
        out.println("-------------------------------------------");
        out.printf("%-5s %-20s %-10s %-10s %-10s%n", "No.", "Product", "Price", "Quantity", "Subtotal");
        out.println("-------------------------------------------");

        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            double subtotal = item.getPrice() * item.getQuantity();
            totalPrice += subtotal;
            out.printf("%-5d %-20s $%-9.2f %-10d $%-10.2f%n",
                    (i + 1), item.getName(), item.getPrice(), item.getQuantity(), subtotal);
        }

        out.println("-------------------------------------------");
        out.printf("%-36s $%-10.2f%n", "TOTAL:", totalPrice);
        out.println("-------------------------------------------");
    }

    public void removeItemPrompt(BufferedReader in, List<CartItem> cartItems, CartDAO cartDAO, int userId) throws IOException {
        out.println("Enter item number to remove: ");
        try {
            int itemIndex = Integer.parseInt(in.readLine()) - 1;
            if (itemIndex >= 0 && itemIndex < cartItems.size()) {
                CartItem selectedItem = cartItems.get(itemIndex);
                cartDAO.removeCartItem(userId, selectedItem.getCartId());
                out.println("Removed from cart: " + selectedItem.getName());
            } else {
                out.println("Invalid item number.");
            }
        } catch (NumberFormatException e) {
            out.println("Please enter a valid number.");
        }
    }
}

