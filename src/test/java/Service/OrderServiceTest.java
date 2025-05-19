package Service;

import DAO.OrderDAO;
import ECommerceObjects.CartItem;
import ECommerceObjects.Order;
import Session.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.Calendar;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
public class OrderServiceTest {
    private OrderDAO orderDAO;
    private UserSession session;
    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderDAO = mock(OrderDAO.class);
        session = mock(UserSession.class);
        orderService = new OrderService(orderDAO, session);
    }

    @Test
    void testGetOrderHistory() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2025, Calendar.MAY, 14); // Month is 0-based: Calendar.JANUARY = 0
        Date orderDate = calendar.getTime();
        List<Order> mockOrderHistory = List.of(new Order(1, 400, orderDate));
        when(session.getUserId()).thenReturn(1);
        when(orderDAO.fetchOrderHistory(1)).thenReturn(mockOrderHistory);

        List<Order> orderHistory = orderService.getOrderHistory();

        assertEquals(mockOrderHistory, orderHistory, "Order history should match the mock data.");
        verify(orderDAO).fetchOrderHistory(1);
    }

    @Test
    void testPrintOrderDetails() {
        PrintWriter out = mock(PrintWriter.class);
        int orderId = 1;

        orderService.printOrderDetails(orderId, out);

        verify(orderDAO).displayOrderDetails(orderId, out);
    }

    @Test
    void testProcessOrder() {
        List<CartItem> cartItems = List.of(new CartItem(1, 1, "T-Shirt", 100.0,2));
        double totalPrice = 200.0;

        when(session.getUserId()).thenReturn(1);

        orderService.processOrder(cartItems, totalPrice);

        verify(orderDAO).checkoutOrder(1, cartItems, totalPrice);
    }
}
