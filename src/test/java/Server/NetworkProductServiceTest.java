package Server;

import DAO.ProductDAO;
import ECommerceDB.DatabaseManager;
import ECommerceObjects.Product;
import UI.ProductUI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.Mockito.*;

class NetworkProductServiceTest {

    private DatabaseManager dbManager;
    private PrintWriter out;
    private BufferedReader in;
    private ProductDAO productRepo;
    private ProductUI productUI;
    private NetworkProductService networkProductService;

    @BeforeEach
    void setUp() throws Exception {
        dbManager = mock(DatabaseManager.class);
        out = mock(PrintWriter.class);
        in = mock(BufferedReader.class);

        networkProductService = spy(new NetworkProductService(dbManager, out, in));
        productRepo = mock(ProductDAO.class);
        productUI = mock(ProductUI.class);

        java.lang.reflect.Field repoField = NetworkProductService.class.getDeclaredField("productRepo");
        repoField.setAccessible(true);
        repoField.set(networkProductService, productRepo);

        java.lang.reflect.Field uiField = NetworkProductService.class.getDeclaredField("productUI");
        uiField.setAccessible(true);
        uiField.set(networkProductService, productUI);
    }

    @Test
    void testViewProducts_EmptyList() throws Exception {
        when(productRepo.getAllProducts()).thenReturn(List.of());

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(productUI).displayProducts(List.of());
        verify(out, never()).println(contains("1. Add to Cart"));
    }

    @Test
    void testViewProducts_AddToCartOption() throws Exception {
        Product product = mock(Product.class);
        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(1);
        when(product.getName()).thenReturn("TestProduct");
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("1", "1", "2");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(productUI).displayProducts(anyList());
        verify(cartService).addToCart(1, "TestProduct", 2);
    }

    @Test
    void testViewProducts_ReturnToMainMenu() throws Exception {
        Product product = mock(Product.class);
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("2");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(productUI).displayProducts(anyList());
        verify(cartService, never()).addToCart(anyInt(), anyString(), anyInt());
    }

    @Test
    void testViewProducts_SQLException() throws Exception {
        when(productRepo.getAllProducts()).thenThrow(new SQLException("DB error"));

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(out).println(contains("Error retrieving products: DB error"));
    }

    @Test
    void testAddToCart_InvalidProductNumber() throws Exception {
        Product product = mock(Product.class);
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("1", "99");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(out).println(contains("Invalid product number."));
    }

    @Test
    void testAddToCart_InvalidQuantity() throws Exception {
        Product product = mock(Product.class);
        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(1);
        when(product.getName()).thenReturn("TestProduct");
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("1", "1", "-1");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(out).println(contains("Quantity must be greater than zero."));
    }

    @Test
    void testAddToCart_NotEnoughStock() throws Exception {
        Product product = mock(Product.class);
        when(product.getStock()).thenReturn(2);
        when(product.getId()).thenReturn(1);
        when(product.getName()).thenReturn("TestProduct");
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("1", "1", "5");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(out).println(contains("Not enough stock. Available: 2"));
    }

    @Test
    void testAddToCart_NumberFormatException() throws Exception {
        Product product = mock(Product.class);
        when(product.getStock()).thenReturn(10);
        when(product.getId()).thenReturn(1);
        when(product.getName()).thenReturn("TestProduct");
        when(productRepo.getAllProducts()).thenReturn(List.of(product));
        when(in.readLine()).thenReturn("1", "abc");

        NetworkCartService cartService = mock(NetworkCartService.class);
        networkProductService.viewProducts(cartService);

        verify(out).println(contains("Please enter a valid number."));
    }
}