package Server;

import java.io.BufferedReader;
import java.io.PrintWriter;

import DAO.CartDAO;
import ECommerceDB.DatabaseManager;
import Service.CartService;
import Session.UserSession;
import UI.CartUI;

public class NetworkCartService {
    private final CartService cartService;

    public NetworkCartService(DatabaseManager dbManager, UserSession session, PrintWriter out) {
        CartDAO cartDAO = new CartDAO(dbManager);
        CartUI cartUI = new CartUI(out);
        this.cartService = new CartService(cartDAO, cartUI, session, out);
    }

    public void viewCart(BufferedReader in, NetworkOrderService orderService) throws Exception {
        cartService.viewCart(in, orderService);
    }

    public void addToCart(int productId, String productName, int quantity) {
        cartService.addToCart(productId, productName, quantity);
    }

    public void clearCart() {
        cartService.clearCart();
    }
}

