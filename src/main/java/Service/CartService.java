package Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import DAO.CartDAO;
import ECommerceObjects.CartItem;
import Server.NetworkOrderService;
import Session.UserSession;
import UI.CartUI;

public class CartService {
    private final CartDAO cartDAO;
    private final CartUI cartUI;
    private final UserSession session;
    private final PrintWriter out;

    public CartService(CartDAO cartDAO, CartUI cartUI, UserSession session, PrintWriter out) {
        this.cartDAO = cartDAO;
        this.cartUI = cartUI;
        this.session = session;
        this.out = out;
    }

    public void viewCart(BufferedReader in, NetworkOrderService orderService) throws IOException {
        List<CartItem> cartItems = cartDAO.getCartItems(session.getUserId());
        cartUI.displayCart(cartItems);

        if (cartItems.isEmpty()) return;

        out.println("\n1. Checkout");
        out.println("2. Remove Item");
        out.println("3. Return to Main Menu");
        out.println("\nSelect an option: ");
        String choice = in.readLine();

        switch (choice) {
            case "1":
                double total = cartItems.stream().mapToDouble(item -> item.getPrice() * item.getQuantity()).sum();
                orderService.checkout(cartItems, total);
                break;
            case "2":
                cartUI.removeItemPrompt(in, cartItems, cartDAO, session.getUserId());
                break;
        }
    }

    public void addToCart(int productId, String productName, int quantity) {
        if (cartDAO.updateCartItem(session.getUserId(), productId, quantity)) {
            out.println("Updated quantity in cart: " + productName + " x" + quantity);
        } else {
            cartDAO.insertCartItem(session.getUserId(), productId, quantity);
            out.println("Added to cart: " + productName + " x" + quantity);
        }
    }

    public void clearCart() {
        cartDAO.clearCart(session.getUserId());
    }
}

