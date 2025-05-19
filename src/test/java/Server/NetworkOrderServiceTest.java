package Server;

import ECommerceDB.DatabaseManager;
import ECommerceObjects.CartItem;
import Session.UserSession;
import UI.OrderUI;
import Service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import static org.mockito.Mockito.*;

class NetworkOrderServiceTest {

    private DatabaseManager dbManager;
    private UserSession session;
    private PrintWriter out;
    private BufferedReader in;
    private NetworkOrderService networkOrderService;
    private OrderUI orderUI;

    @BeforeEach
    void setUp() {
        dbManager = mock(DatabaseManager.class);
        session = mock(UserSession.class);
        out = mock(PrintWriter.class);
        in = mock(BufferedReader.class);

        networkOrderService = spy(new NetworkOrderService(dbManager, session, out, in));
        orderUI = mock(OrderUI.class);
        try {
            java.lang.reflect.Field field = NetworkOrderService.class.getDeclaredField("orderUI");
            field.setAccessible(true);
            field.set(networkOrderService, orderUI);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testViewOrderHistory() throws IOException {
        networkOrderService.viewOrderHistory();
        verify(orderUI).displayOrderHistory();
    }

    @Test
    void testCheckout() throws IOException {
        List<CartItem> cartItems = mock(List.class);
        double totalPrice = 123.45;

        networkOrderService.checkout(cartItems, totalPrice);
        verify(orderUI).checkout(cartItems, totalPrice);
    }
}