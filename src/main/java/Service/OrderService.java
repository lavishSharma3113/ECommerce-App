package Service;

import DAO.OrderDAO;
import ECommerceObjects.CartItem;
import ECommerceObjects.Order;
import Session.UserSession;

import java.io.PrintWriter;
import java.util.List;

public class OrderService {
    private final OrderDAO orderDAO;
    private final UserSession session;

    public OrderService(OrderDAO orderDAO, UserSession session) {
        this.orderDAO = orderDAO;
        this.session = session;
    }

    public List<Order> getOrderHistory() {
        return orderDAO.fetchOrderHistory(session.getUserId());
    }

    public void printOrderDetails(int orderId, PrintWriter out) {
        orderDAO.displayOrderDetails(orderId, out);
    }

    public void processOrder(List<CartItem> cartItems, double totalPrice) {
        orderDAO.checkoutOrder(session.getUserId(), cartItems, totalPrice);
    }
}

