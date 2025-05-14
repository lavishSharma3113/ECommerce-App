package Service;

import DAO.CartDAO;
import ECommerceObjects.CartItem;
import Server.NetworkOrderService;
import Session.UserSession;
import UI.CartUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
public class CartServiceTest {
    private CartDAO cartDAO;
    private CartUI cartUI;
    private UserSession session;
    private PrintWriter out;
    private CartService cartService;

    @BeforeEach
    void setup() {
        cartDAO = mock(CartDAO.class);
        cartUI = mock(CartUI.class);
        session = mock(UserSession.class);
        out = mock(PrintWriter.class);
        cartService = new CartService(cartDAO, cartUI, session, out);
    }

    @Test
    void testViewCart_EmptyCart() throws IOException {
        when(session.getUserId()).thenReturn(1);
        when(cartDAO.getCartItems(1)).thenReturn(new ArrayList<>());

        BufferedReader in = new BufferedReader(new StringReader("3"));
        NetworkOrderService orderService = mock(NetworkOrderService.class);

        cartService.viewCart(in, orderService);

        verify(cartUI).displayCart(new ArrayList<>());
        verify(out, never()).println(contains("1. Checkout"));
    }

    @Test
    void testViewCart_Checkout() throws IOException {
        List<CartItem> cartItems = List.of(new CartItem(1, 1, "T-shirt", 100.0,2));
        when(session.getUserId()).thenReturn(1);
        when(cartDAO.getCartItems(1)).thenReturn(cartItems);

        BufferedReader in = new BufferedReader(new StringReader("1"));
        NetworkOrderService orderService = mock(NetworkOrderService.class);

        cartService.viewCart(in, orderService);

        verify(cartUI).displayCart(cartItems);
        verify(orderService).checkout(cartItems, 200.0);
    }

    @Test
    void testViewCart_RemoveItem() throws IOException {
        List<CartItem> cartItems = List.of(new  CartItem(1, 1, "T-shirt", 100.0,2));
        when(session.getUserId()).thenReturn(1);
        when(cartDAO.getCartItems(1)).thenReturn(cartItems);

        BufferedReader in = new BufferedReader(new StringReader("2"));
        NetworkOrderService orderService = mock(NetworkOrderService.class);

        cartService.viewCart(in, orderService);

        verify(cartUI).removeItemPrompt(in, cartItems, cartDAO, 1);
    }

    @Test
    void testAddToCart_NewItem() {
        when(session.getUserId()).thenReturn(1);
        when(cartDAO.updateCartItem(1, 1, 2)).thenReturn(false);

        cartService.addToCart(1, "Product1", 2);

        verify(cartDAO).insertCartItem(1, 1, 2);
        verify(out).println("Added to cart: Product1 x2");
    }

    @Test
    void testAddToCart_UpdateItem() {
        when(session.getUserId()).thenReturn(1);
        when(cartDAO.updateCartItem(1, 1, 2)).thenReturn(true);

        cartService.addToCart(1, "Product1", 2);

        verify(cartDAO, never()).insertCartItem(1, 1, 2);
        verify(out).println("Updated quantity in cart: Product1 x2");
    }

    @Test
    void testClearCart() {
        when(session.getUserId()).thenReturn(1);

        cartService.clearCart();

        verify(cartDAO).clearCart(1);
    }
}
