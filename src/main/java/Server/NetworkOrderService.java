package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import ECommerceDB.DatabaseManager;
import ECommerceObjects.CartItem;
import Session.UserSession;
import DAO.OrderDAO;
import Service.OrderService;
import UI.OrderUI;

public class NetworkOrderService {
    private final OrderUI orderUI;
    private final OrderService orderService;

    public NetworkOrderService(DatabaseManager dbManager, UserSession session, PrintWriter out, BufferedReader in) {
        OrderDAO orderDAO = new OrderDAO(dbManager);
        this.orderService = new OrderService(orderDAO, session);
        this.orderUI = new OrderUI(orderService, out, in);
    }

    public void viewOrderHistory() throws IOException {
        orderUI.displayOrderHistory();
    }

    public void checkout(List<CartItem> cartItems, double totalPrice) throws IOException {
        orderUI.checkout(cartItems, totalPrice);
    }
}