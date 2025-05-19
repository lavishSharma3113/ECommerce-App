package Server;

import Service.CartService;
import Session.UserSession;
import ECommerceDB.DatabaseManager;
import Server.NetworkOrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

class NetworkCartServiceTest {

    private DatabaseManager dbManager;
    private UserSession session;
    private PrintWriter out;
    private NetworkCartService networkCartService;
    private CartService cartService;

    @BeforeEach
    void setUp() {
        dbManager = mock(DatabaseManager.class);
        session = mock(UserSession.class);
        out = mock(PrintWriter.class);

        networkCartService = spy(new NetworkCartService(dbManager, session, out));
        cartService = mock(CartService.class);
        try {
            java.lang.reflect.Field field = NetworkCartService.class.getDeclaredField("cartService");
            field.setAccessible(true);
            field.set(networkCartService, cartService);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testViewCart() throws Exception {
        BufferedReader in = mock(BufferedReader.class);
        NetworkOrderService orderService = mock(NetworkOrderService.class);

        networkCartService.viewCart(in, orderService);

        verify(cartService).viewCart(in, orderService);
    }

    @Test
    void testAddToCart() {
        networkCartService.addToCart(1, "Product", 2);

        verify(cartService).addToCart(1, "Product", 2);
    }

    @Test
    void testClearCart() {
        networkCartService.clearCart();

        verify(cartService).clearCart();
    }
}