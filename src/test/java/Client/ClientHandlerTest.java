package Client;

import static org.junit.jupiter.api.Assertions.*;

import ECommerceDB.DatabaseManager;
import Server.NetworkAuthenticationService;
import Server.NetworkCartService;
import Server.NetworkOrderService;
import Server.NetworkProductService;
import Session.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.mockito.Mockito.*;
class ClientHandlerTest {

    private Socket mockSocket;
    private DatabaseManager mockDbManager;
    private PrintWriter mockOut;
    private BufferedReader mockIn;
    private UserSession mockSession;
    private ClientHandler clientHandler;

    @BeforeEach
    void setup() throws IOException {
        mockSocket = mock(Socket.class);
        mockDbManager = mock(DatabaseManager.class);
        mockOut = mock(PrintWriter.class);
        mockIn = mock(BufferedReader.class);
        mockSession = mock(UserSession.class);

        when(mockSocket.getOutputStream()).thenReturn(new ByteArrayOutputStream());
        when(mockSocket.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));

        clientHandler = new ClientHandler(mockSocket, mockDbManager);
        clientHandler.out = mockOut;
        clientHandler.in = mockIn;
        clientHandler.session = mockSession;
    }

    @Test
    void testHandleAuthentication_LoginSuccess() throws IOException {
        when(mockIn.readLine()).thenReturn("1", "testUser", "testPass");
        NetworkAuthenticationService mockAuthService = mock(NetworkAuthenticationService.class);
        when(mockAuthService.login("testUser", "testPass")).thenReturn(true);
        when(mockAuthService.getCurrentSession()).thenReturn(mockSession);

        clientHandler.handleAuthentication();

        verify(mockOut).println(contains("Login successful!"));
        verify(mockSession).isAuthenticated();
    }

    @Test
    void testHandleAuthentication_SignUpSuccess() throws IOException {
        when(mockIn.readLine()).thenReturn("2", "newUser", "newPass", "newEmail");
        NetworkAuthenticationService mockAuthService = mock(NetworkAuthenticationService.class);
        when(mockAuthService.signUp("newUser", "newPass", "newEmail"))
                .thenReturn(NetworkAuthenticationService.SignUpResult.SUCCESS);
        when(mockAuthService.getCurrentSession()).thenReturn(mockSession);

        clientHandler.handleAuthentication();

        verify(mockOut).println(contains("Sign up successful!"));
        verify(mockSession).isAuthenticated();
    }

    @Test
    void testHandleAuthentication_Exit() throws IOException {
        when(mockIn.readLine()).thenReturn("3");

        clientHandler.handleAuthentication();

        verify(mockOut).println(contains("Thank you for visiting. Goodbye!"));
    }

    @Test
    void testHandleMainMenu_ViewProducts() throws IOException {
        when(mockIn.readLine()).thenReturn("1");
        NetworkProductService mockProductService = mock(NetworkProductService.class);
        NetworkCartService mockCartService = mock(NetworkCartService.class);

        clientHandler.handleViewProducts();

        verify(mockProductService).viewProducts(mockCartService);
    }

    @Test
    void testHandleMainMenu_ViewCart() throws Exception {
        when(mockIn.readLine()).thenReturn("2");
        NetworkCartService mockCartService = mock(NetworkCartService.class);
        NetworkOrderService mockOrderService = mock(NetworkOrderService.class);

        clientHandler.handleViewCart();

        verify(mockCartService).viewCart(mockIn, mockOrderService);
    }

    @Test
    void testHandleMainMenu_ViewOrderHistory() throws IOException {
        when(mockIn.readLine()).thenReturn("3");
        NetworkOrderService mockOrderService = mock(NetworkOrderService.class);

        clientHandler.handleViewOrderHistory();

        verify(mockOrderService).viewOrderHistory();
    }

    @Test
    void testHandleMainMenu_Logout() throws Exception {
        when(mockIn.readLine()).thenReturn("4");
        when(mockSession.getUsername()).thenReturn("testUser");

        clientHandler.handleMainMenu();

        verify(mockOut).println(contains("Logging out. Goodbye, testUser!"));
        verify(mockSession).clearSession();
    }

    @Test
    void testRun() throws IOException {
        when(mockIn.readLine()).thenReturn("1", "testUser", "testPass", "4");
        NetworkAuthenticationService mockAuthService = mock(NetworkAuthenticationService.class);
        when(mockAuthService.login("testUser", "testPass")).thenReturn(true);
        when(mockAuthService.getCurrentSession()).thenReturn(mockSession);
        when(mockSession.isAuthenticated()).thenReturn(true);

        clientHandler.run();

        verify(mockOut).println(contains("Welcome to the Online Shop"));
        verify(mockOut).println(contains("Logging out. Goodbye"));
    }
}