package Server;

import ECommerceDB.DatabaseManager;
import Client.ClientHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ShopServerTest {

    private DatabaseManager mockDbManager;
    private ExecutorService mockThreadPool;
    private ShopServer shopServer;

    @BeforeEach
    void setup() {
        mockDbManager = mock(DatabaseManager.class);
        mockThreadPool = mock(ExecutorService.class);
        shopServer = spy(new ShopServer(mockDbManager));
        shopServer.threadPool = mockThreadPool;
    }

    @Test
    void testStart_ServerSocket() throws IOException {
        try (MockedStatic<ServerSocket> mockedServerSocket = mockStatic(ServerSocket.class)) {
            ServerSocket mockServerSocket = mock(ServerSocket.class);
            mockedServerSocket.when(() -> new ServerSocket(5000)).thenReturn(mockServerSocket);

            doNothing().when(shopServer).handleClientConnection(mockServerSocket);
            doNothing().when(shopServer).shutdownThreadPool();

            shopServer.start();

            verify(mockServerSocket).close();
            verify(shopServer).shutdownThreadPool();
        }
    }

    @Test
    void testHandleClientConnection() throws IOException {
        ServerSocket mockServerSocket = mock(ServerSocket.class);
        Socket mockClientSocket = mock(Socket.class);

        when(mockServerSocket.accept()).thenReturn(mockClientSocket);
        when(mockClientSocket.getInetAddress().getHostAddress()).thenReturn("127.0.0.1");

        shopServer.handleClientConnection(mockServerSocket);

        verify(mockThreadPool).submit(any(ClientHandler.class));
    }

    @Test
    void testShutdownThreadPool() {
        shopServer.shutdownThreadPool();

        verify(mockThreadPool).shutdown();
    }

    @Test
    void testLoadDatabaseConfig_ValidFile() throws IOException {
        Properties mockProperties = new Properties();
        mockProperties.setProperty("db.url", "jdbc:mysql://localhost:3306/testdb");
        mockProperties.setProperty("db.user", "root");
        mockProperties.setProperty("db.password", "password");

        try (MockedStatic<FileInputStream> mockedFileInputStream = mockStatic(FileInputStream.class)) {
            FileInputStream mockInputStream = mock(FileInputStream.class);
            mockedFileInputStream.when(() -> new FileInputStream("src/main/java/Configurations/db-config.properties"))
                    .thenReturn(mockInputStream);

            Properties config = ShopServer.loadDatabaseConfig();

            assertNotNull(config, "Config should not be null for a valid file.");
            assertEquals("jdbc:mysql://localhost:3306/testdb", config.getProperty("db.url"));
            assertEquals("root", config.getProperty("db.user"));
            assertEquals("password", config.getProperty("db.password"));
        }
    }

    @Test
    void testLoadDatabaseConfig_FileNotFound() {
        Properties config = ShopServer.loadDatabaseConfig();

        assertNull(config, "Config should be null if the file is not found.");
    }

    @Test
    void testInitializeDatabaseManager_ValidConfig() {
        Properties mockProperties = new Properties();
        mockProperties.setProperty("db.url", "jdbc:mysql://localhost:3306/testdb");
        mockProperties.setProperty("db.user", "root");
        mockProperties.setProperty("db.password", "password");

        when(mockDbManager.testConnection()).thenReturn(true);

        DatabaseManager dbManager = ShopServer.initializeDatabaseManager(mockProperties);

        assertNotNull(dbManager, "DatabaseManager should not be null for valid configuration.");
    }

    @Test
    void testInitializeDatabaseManager_InvalidConfig() {
        Properties mockProperties = new Properties();
        mockProperties.setProperty("db.url", "jdbc:mysql://localhost:3306/testdb");
        mockProperties.setProperty("db.user", "root");
        mockProperties.setProperty("db.password", "password");

        when(mockDbManager.testConnection()).thenReturn(false);

        DatabaseManager dbManager = ShopServer.initializeDatabaseManager(mockProperties);

        assertNull(dbManager, "DatabaseManager should be null if the connection fails.");
    }
}